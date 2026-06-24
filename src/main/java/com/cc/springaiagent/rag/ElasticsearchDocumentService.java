package com.cc.springaiagent.rag;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorSimilarity;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Elasticsearch 文档索引服务
 * <p>
 * 负责：创建索引、加载知识库 Markdown 文档、向量化、存入 ES。
 * 启动时自动检查索引是否存在，若不存在则创建并导入数据。
 * <p>
 */
@Slf4j
@Service
public class ElasticsearchDocumentService {

    @Value("${spring.es.hybrid.index-name:knowledge_base}")
    private String indexName;

    @Resource
    private ElasticsearchClient esClient;

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private EmbeddingModel dashscopeEmbeddingModel;

    /**
     * 启动时自动初始化 ES 索引和数据
     * <p>
     * 如果 ES 未启动或不可用，仅打印错误日志，不影响应用启动。
     */
    @PostConstruct
    public void init() {
        try {
            ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(indexName));
            boolean exists = esClient.indices().exists(existsRequest).value();

            if (exists) {
                log.info("ES 索引 [{}] 已存在, 跳过初始化", indexName);
                return;
            }

            createIndex();

            List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
            if (documents.isEmpty()) {
                log.warn("未加载到任何知识库文档, 索引将为空");
                return;
            }

            indexDocuments(documents);
            log.info("ES 索引 [{}] 初始化完成, 共索引 {} 条文档", indexName, documents.size());

        } catch (Exception e) {
            log.error("ES 索引初始化失败 (ES 可能未启动): {}", e.getMessage());
        }
    }

    /**
     * 创建 ES 索引（定义 mapping）
     * <p>
     * ES 9.x: dense_vector 使用 dims + index，similarity 可选。
     *
     * @throws Exception 当索引创建请求执行失败时抛出异常
     */
    private void createIndex() throws Exception {
        CreateIndexRequest request = CreateIndexRequest.of(c -> c
                .index(indexName)
                .settings(s -> s
                        .numberOfShards("1")
                        .numberOfReplicas("0")
                        // ES 9.x: refreshInterval 接受 Time 对象
                        .refreshInterval(t -> t.time("1s"))
                )
                .mappings(m -> m
                        .properties("id", p -> p.keyword(k -> k))
                        .properties("content", p -> p
                                .text(t -> t
//                                        .analyzer("standard")
//                                        .searchAnalyzer("standard")
                                        //中文必须使用ik分词器
                                        .analyzer("ik_max_word")
                                        .searchAnalyzer("ik_smart")
//                                        .termVector(TermVectorOption.Yes)//高亮
                                )
                        )
                        .properties("embedding", p -> p
                                .denseVector(dv -> dv
                                        .dims(1024)
                                        .index(true)
                                        .similarity(DenseVectorSimilarity.Cosine)
                                )
                        )
                        .properties("metadata", p -> p
                                .object(o -> o.enabled(true))
                        )
                        .properties("create_time", p -> p
                                .date(d -> d)
                        )
                )
        );

        esClient.indices().create(request);
        log.info("ES 索引 [{}] 创建成功 (mapping: id+content+embedding[1024]+metadata+create_time)", indexName);
    }

    /**
     * 批量索引文档（分批，每批 10 条）
     *
     * @param documents 待索引的文档列表，包含文本内容及元数据
     * @throws Exception 当批量写入 ES 发生错误时抛出异常
     */
    public void indexDocuments(List<Document> documents) throws Exception {
        int batchSize = 10;
        int totalIndexed = 0;

        for (int i = 0; i < documents.size(); i += batchSize) {
            int end = Math.min(i + batchSize, documents.size());
            List<Document> batch = documents.subList(i, end);

            // 1. 批量生成向量（逐条 embedding）
            List<String> texts = batch.stream()
                    .map(Document::getText)
                    .toList();

            List<float[]> embeddings = texts.stream()
                    .map(text -> dashscopeEmbeddingModel.embed(text))
                    .toList();

            // 2. 构建 BulkOperation 列表
            List<BulkOperation> operations = new ArrayList<>();
            for (int j = 0; j < batch.size(); j++) {
                Document doc = batch.get(j);
                float[] embedding = embeddings.get(j);

                Map<String, Object> docMap = new LinkedHashMap<>();
                docMap.put("id", doc.getId());
                docMap.put("content", doc.getText());
                docMap.put("embedding", embedding);
                docMap.put("metadata", doc.getMetadata());
                docMap.put("create_time", new Date());

                operations.add(BulkOperation.of(op -> op
                        .index(idx -> idx
                                .index(indexName)
                                .id(doc.getId())
                                .document(docMap)
                        )
                ));
            }

            // 3. 执行批量索引
            BulkResponse bulkResponse = esClient.bulk(BulkRequest.of(b -> b.operations(operations)));
            totalIndexed += batch.size();

            if (bulkResponse.errors()) {
                log.warn("批量索引存在部分错误, 已成功 {} 条", totalIndexed);
            }
        }

        log.info("共索引 {} 条文档到 ES 索引 [{}]", totalIndexed, indexName);
    }

    /**
     * 清空并重建索引（用于知识库更新）
     *
     * @throws Exception 当删除旧索引、创建新索引或重新导入数据过程中发生错误时抛出异常
     */
    public void rebuildIndex() throws Exception {
        ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(indexName));
        boolean exists = esClient.indices().exists(existsRequest).value();
        if (exists) {
            esClient.indices().delete(d -> d.index(indexName));
            log.info("已删除旧索引 [{}]", indexName);
        }

        createIndex();
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        if (!documents.isEmpty()) {
            indexDocuments(documents);
        }
        log.info("索引 [{}] 重建完成", indexName);
    }
}