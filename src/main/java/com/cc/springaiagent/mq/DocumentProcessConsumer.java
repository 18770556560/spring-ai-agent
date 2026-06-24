package com.cc.springaiagent.mq;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.cc.springaiagent.config.RabbitConfig;
import com.cc.springaiagent.entity.DocumentTask;
import com.cc.springaiagent.service.IDocumentTaskService;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 文档处理消息消费者
 * <p>
 * 核心职责：从 RabbitMQ 消费文档处理任务，执行完整的 RAG 数据预处理流程：
 * <ol>
 *   <li>读取上传的文件内容</li>
 *   <li>解析文档结构（支持 Markdown / JSON / 纯文本）</li>
 *   <li>基于 Token 进行文本分块（Chunking）</li>
 *   <li>调用 Embedding 模型生成向量</li>
 *   <li>批量索引到 Elasticsearch</li>
 * </ol>
 * <p>
 * 可靠性保障：
 * <ul>
 *   <li><b>手动 ACK</b>：确保只有处理成功后才确认消息，防止数据丢失。</li>
 *   <li><b>重试机制</b>：最多重试 3 次（重试次数持久化在数据库）。失败后重新入队。</li>
 *   <li><b>死信队列</b>：超过最大重试次数后，消息被拒绝且不重新入队，进入 DLQ 供人工排查。</li>
 * </ul>
 */
@Slf4j
@Component
public class DocumentProcessConsumer {

    /** 最大重试次数，超过此次数将进入死信队列 */
    private static final int MAX_RETRIES = 3;

    @Value("${spring.es.hybrid.index-name:knowledge_base}")
    private String indexName;

    @Resource
    private IDocumentTaskService documentTaskService;

    @Resource
    private ElasticsearchClient esClient;

    @Resource
    private EmbeddingModel dashscopeEmbeddingModel;

    /**
     * 监听文档处理队列，执行异步文档索引任务
     * <p>
     * 采用手动 ACK 模式，确保消息处理的原子性和可靠性。
     *
     * @param taskId      消息体，即文档任务的主键 ID
     * @param message     AMQP 原始消息对象
     * @param channel     RabbitMQ 通道，用于发送 ACK/Reject 指令
     * @param deliveryTag 当前消息的投递标签，用于确认消息
     */
    @RabbitListener(
            queues = RabbitConfig.DOCUMENT_PROCESS_QUEUE,
            ackMode = "MANUAL" // 开启手动确认模式
    )
    public void onDocumentProcessTask(
            String taskId,
            Message message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        log.info("收到文档处理任务: taskId={}, deliveryTag={}", taskId, deliveryTag);

        try {
            // 1. 查询任务详情
            DocumentTask task = documentTaskService.getByTaskId(taskId);
            if (task == null) {
                log.error("任务不存在: taskId={}, 直接ACK丢弃", taskId);
                // 任务不存在，无需重试，直接确认移除消息
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 2. 更新状态为 PROCESSING，并递增重试计数
            // 注意：retryCount 表示当前是第几次尝试（包含本次）
            int retryCount = task.getRetryCount() != null ? task.getRetryCount() + 1 : 1;
            task.setStatus(DocumentTask.STATUS_PROCESSING);
            task.setRetryCount(retryCount);
            documentTaskService.updateById(task);
            log.info("第 {} 次处理尝试: taskId={}", retryCount, taskId);

            // 3. 读取文件内容
            Path filePath = Path.of(task.getFilePath());
            if (!Files.exists(filePath)) {
                throw new RuntimeException("文件不存在: " + task.getFilePath());
            }
            String fileContent = Files.readString(filePath);
            String fileType = task.getFileType();

            // 4. 解析文档：根据文件类型转换为 Spring AI Document 对象列表
            List<Document> documents = parseDocument(task.getFileName(), fileContent, fileType);

            // 5. 文本分块：将长文档切分为适合 Embedding 的小片段
            List<Document> chunks = splitDocuments(documents);

            // 6. 向量化并索引到 ES
            int indexedCount = indexToElasticsearch(chunks);

            // 7. 任务成功完成，更新数据库状态
            task.setStatus(DocumentTask.STATUS_COMPLETED);
            task.setChunkCount(indexedCount);
            documentTaskService.updateById(task);

            log.info("文档处理完成: taskId={}, 文件名={}, 分块数={}, 尝试次数={}",
                    taskId, task.getFileName(), indexedCount, retryCount);

            // 成功 → 手动发送 ACK，消息从队列中移除
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("文档处理异常: taskId={}, 错误={}", taskId, e.getMessage(), e);

            try {
                // 从数据库获取最新的重试次数（防止并发修改导致的不一致）
                DocumentTask task = documentTaskService.getByTaskId(taskId);
                int currentRetry = task != null && task.getRetryCount() != null ? task.getRetryCount() : 0;

                if (currentRetry < MAX_RETRIES) {
                    // 未超重试上限 → 重新入队（Requeue）
                    // basicReject(requeue=true): 消息回到队列尾部，等待下次消费
                    log.warn("重试 {}/{} → 消息重新入队: taskId={}", currentRetry, MAX_RETRIES, taskId);
                    channel.basicReject(deliveryTag, true);
                } else {
                    // 重试耗尽 → 进入死信队列（DLQ）
                    log.error("重试次数已耗尽 ({}/{}) → 消息进入 DLQ: taskId={}",
                            currentRetry, MAX_RETRIES, taskId);

                    // 标记任务为失败，记录错误信息
                    markTaskFailed(taskId, e.getMessage());

                    // basicReject(requeue=false): 消息不再重新入队，根据 RabbitMQ 配置进入死信交换机
                    channel.basicReject(deliveryTag, false);
                }
            } catch (Exception ioException) {
                log.error("消息确认/重试处理失败: taskId={}", taskId, ioException);
                // 兜底策略：如果连 Reject 都失败，强制不重入队，避免无限循环消耗资源
                try {
                    channel.basicReject(deliveryTag, false);
                } catch (Exception ignored) {
                    // 放弃处理
                }
            }
        }
    }

    // ==================== 文档解析逻辑 ====================

    /**
     * 根据文件类型路由到具体的解析方法
     *
     * @param fileName 文件名
     * @param content  文件内容字符串
     * @param fileType 文件扩展名 (md, json, txt)
     * @return 解析后的 Document 列表
     */
    private List<Document> parseDocument(String fileName, String content, String fileType) {
        return switch (fileType.toLowerCase()) {
            case "md" -> parseMarkdown(fileName, content);
            case "json" -> parseJson(fileName, content);
            case "txt" -> parsePlainText(fileName, content);
            default -> {
                log.warn("未知文件类型: {}, 按纯文本处理", fileType);
                yield parsePlainText(fileName, content);
            }
        };
    }

    /**
     * 解析 Markdown 文档
     * <p>
     * 使用 Spring AI 的 MarkdownDocumentReader，支持提取元数据和结构化内容。
     * 若解析失败，自动降级为纯文本解析。
     *
     * @param fileName 文件名
     * @param content  Markdown 内容
     * @return Document 列表
     */
    private List<Document> parseMarkdown(String fileName, String content) {
        // 配置 Markdown 解析规则
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true) // 水平线分隔符创建新文档
                .withIncludeCodeBlock(false)            // 排除代码块（可选，视需求而定）
                .withIncludeBlockquote(true)            // 包含引用块
                .withAdditionalMetadata("filename", fileName) // 附加文件名元数据
                .build();

        try {
            // Spring AI Reader 通常需要 Resource 输入，因此创建临时文件
            Path tempFile = Files.createTempFile("upload-", ".md");
            Files.writeString(tempFile, content);
            FileSystemResource resource = new FileSystemResource(tempFile);

            MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
            List<Document> docs = reader.get();

            // 清理临时文件
            Files.deleteIfExists(tempFile);
            return docs;
        } catch (Exception e) {
            log.warn("Markdown 解析失败，回退到纯文本模式: {}", e.getMessage());
            return parsePlainText(fileName, content);
        }
    }

    /**
     * 解析 JSON 文档
     * <p>
     * 简单将整个 JSON 内容作为一个 Document 处理，实际场景中可能需要更复杂的 JSON 路径提取。
     *
     * @param fileName 文件名
     * @param content  JSON 内容
     * @return 单个 Document 列表
     */
    private List<Document> parseJson(String fileName, String content) {
        Document doc = new Document(content);
        doc.getMetadata().put("filename", fileName);
        doc.getMetadata().put("fileType", "json");
        return List.of(doc);
    }

    /**
     * 解析纯文本文档
     *
     * @param fileName 文件名
     * @param content  文本内容
     * @return 单个 Document 列表，若内容为空则返回空列表
     */
    private List<Document> parsePlainText(String fileName, String content) {
        if (content == null || content.isBlank()) {
            return Collections.emptyList();
        }
        Document doc = new Document(content);
        doc.getMetadata().put("filename", fileName);
        doc.getMetadata().put("fileType", "txt");
        return List.of(doc);
    }

    // ==================== 分块与索引逻辑 ====================

    /**
     * 对文档列表进行 Token 级别的分块
     * <p>
     * 使用 TokenTextSplitter 确保分块不会切断单词或语义单元，适合 Embedding 模型输入限制。
     *
     * @param documents 原始文档列表
     * @return 分块后的 Document 列表
     */
    private List<Document> splitDocuments(List<Document> documents) {
        if (documents.isEmpty()) {
            return Collections.emptyList();
        }
        // 默认配置：通常 chunkSize=800, overlap=100，可根据模型上下文窗口调整
        TokenTextSplitter splitter = new TokenTextSplitter();
        return splitter.apply(documents);
    }

    /**
     * 批量向量化并索引到 Elasticsearch
     * <p>
     * 流程：
     * 1. 分批处理（每批 10 条），避免内存溢出和请求过大。
     * 2. 调用 Embedding 模型生成向量。
     * 3. 构建 BulkRequest 批量写入 ES。
     *
     * @param chunks 待索引的文档分块列表
     * @return 成功索引的文档数量
     * @throws Exception 当 ES 写入发生严重错误时抛出
     */
    private int indexToElasticsearch(List<Document> chunks) throws Exception {
        if (chunks.isEmpty()) {
            log.warn("没有文档块需要索引");
            return 0;
        }

        int batchSize = 10;
        int totalIndexed = 0;

        for (int i = 0; i < chunks.size(); i += batchSize) {
            int end = Math.min(i + batchSize, chunks.size());
            List<Document> batch = chunks.subList(i, end);

            // 1. 批量生成向量
            List<float[]> embeddings = batch.stream()
                    .map(doc -> dashscopeEmbeddingModel.embed(doc.getText()))
                    .toList();

            // 2. 构建 Bulk 操作列表
            List<BulkOperation> operations = new ArrayList<>();
            for (int j = 0; j < batch.size(); j++) {
                Document doc = batch.get(j);
                float[] embedding = embeddings.get(j);

                // 构建 ES 文档结构
                Map<String, Object> docMap = new LinkedHashMap<>();
                docMap.put("id", UUID.randomUUID().toString()); // 生成唯一 ID
                docMap.put("content", doc.getText());           // 原始文本
                docMap.put("embedding", embedding);             // 向量数据
                docMap.put("metadata", doc.getMetadata());      // 元数据（文件名等）
                docMap.put("create_time", new Date());          // 入库时间

                operations.add(BulkOperation.of(op -> op
                        .index(idx -> idx.index(indexName).document(docMap))
                ));
            }

            // 3. 执行批量写入
            BulkResponse bulkResponse = esClient.bulk(BulkRequest.of(b -> b.operations(operations)));
            totalIndexed += batch.size();

            // 检查是否有部分失败
            if (bulkResponse.errors()) {
                log.warn("ES 批量索引存在部分错误, 已索引总数: {}", totalIndexed);
                // 生产环境建议进一步解析 bulkResponse.items() 记录具体失败的 ID
            }
        }

        log.info("共索引 {} 条文档到 ES 索引 [{}]", totalIndexed, indexName);
        return totalIndexed;
    }

    /**
     * 标记任务为失败状态，并记录错误信息
     *
     * @param taskId  任务 ID
     * @param errorMsg 错误描述
     */
    private void markTaskFailed(String taskId, String errorMsg) {
        try {
            DocumentTask task = documentTaskService.getByTaskId(taskId);
            if (task != null) {
                task.setStatus(DocumentTask.STATUS_FAILED);
                // 截断过长的错误信息，避免数据库字段溢出
                task.setErrorMsg(errorMsg != null && errorMsg.length() > 500
                        ? errorMsg.substring(0, 500) : errorMsg);
                documentTaskService.updateById(task);
            }
        } catch (Exception e) {
            log.error("更新任务失败状态时异常: taskId={}", taskId, e);
        }
    }
}
