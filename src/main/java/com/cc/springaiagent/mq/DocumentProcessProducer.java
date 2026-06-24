package com.cc.springaiagent.mq;

import com.cc.springaiagent.config.RabbitConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 文档处理消息生产者
 * <p>
 * 文档上传后，通过此生产者将任务投递到 RabbitMQ，
 * 由消费者异步执行分块→向量化→ES索引等耗时操作。
 * <p>
 * 使用 Publisher Confirm 机制确保消息可靠投递。
 */
@Slf4j
@Component
public class DocumentProcessProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送文档处理任务到队列
     *
     * @param taskId 文档任务唯一ID
     */
    public void sendDocumentProcessTask(String taskId) {
        // 构建 CorrelationData，用于 publisher confirm 回调
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(
                RabbitConfig.DOCUMENT_DIRECT_EXCHANGE,
                RabbitConfig.DOCUMENT_PROCESS_ROUTE_KEY,
                taskId,
                correlationData
        );

        log.info("文档处理任务已投递: taskId={}, correlationId={}", taskId, correlationData.getId());
    }
}
