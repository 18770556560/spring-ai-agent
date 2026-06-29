package com.cc.springaiagent.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    // ==================== Agent Task 队列配置 ====================

    /** Agent 任务队列名 */
    public static final String AGENT_TASK_QUEUE = "agent.task.queue";
    /** Agent 交换机名 */
    public static final String AGENT_DIRECT_EXCHANGE = "agent.direct.exchange";
    /** Agent 路由 key */
    public static final String AGENT_TASK_ROUTE_KEY = "agent.task";

    // ==================== 文档处理队列配置 ====================

    /** 文档处理队列名 */
    public static final String DOCUMENT_PROCESS_QUEUE = "document.process.queue";
    /** 文档处理死信队列名 */
    public static final String DOCUMENT_PROCESS_DLQ = "document.process.dlq";
    /** 文档处理交换机名 */
    public static final String DOCUMENT_DIRECT_EXCHANGE = "document.direct.exchange";
    /** 文档处理死信交换机名 */
    public static final String DOCUMENT_DLX_EXCHANGE = "document.dlx.exchange";
    /** 文档处理路由 key */
    public static final String DOCUMENT_PROCESS_ROUTE_KEY = "document.process";
    /** 文档处理死信路由 key */
    public static final String DOCUMENT_PROCESS_DLQ_ROUTE_KEY = "document.process.dlq";

    // ==================== Agent Task Beans ====================

    /** 声明 Agent 任务队列 并且设置持久化 durable:true*/
    @Bean
    public Queue agentTaskQueue() {
        return new Queue(AGENT_TASK_QUEUE, true);
    }

    /** 声明 Agent Direct 交换机 */
    @Bean
    public DirectExchange agentDirectExchange() {
        return new DirectExchange(AGENT_DIRECT_EXCHANGE, true, false);
    }

    /** 绑定 Agent 队列 → 交换机 */
    @Bean
    public Binding bindingAgentTask() {
        return BindingBuilder
                .bind(agentTaskQueue())
                .to(agentDirectExchange())
                .with(AGENT_TASK_ROUTE_KEY);
    }

    // ==================== 文档处理 Beans ====================

    /** 声明死信交换机 */
    @Bean
    public DirectExchange documentDlxExchange() {
        return new DirectExchange(DOCUMENT_DLX_EXCHANGE, true, false);
    }

    /** 声明死信队列（消息重试3次后进入DLQ，用于人工排查） */
    @Bean
    public Queue documentProcessDlq() {
        return new Queue(DOCUMENT_PROCESS_DLQ, true);
    }

    /** 绑定死信队列 → 死信交换机 */
    @Bean
    public Binding bindingDocumentDlq() {
        return BindingBuilder
                .bind(documentProcessDlq())
                .to(documentDlxExchange())
                .with(DOCUMENT_PROCESS_DLQ_ROUTE_KEY);
    }

    /**
     * 声明文档处理主队列（配置死信）
     * <p>
     * 消息被 reject（requeue=false）或 TTL 超时后，自动路由到死信交换机。
     * 消费者处理失败3次后手动 reject → 进入 DLQ → 管理员排查。
     */
    @Bean
    public Queue documentProcessQueue() {
        Map<String, Object> args = new HashMap<>();
        // 绑定死信交换机
        args.put("x-dead-letter-exchange", DOCUMENT_DLX_EXCHANGE);
        // 死信路由 key
        args.put("x-dead-letter-routing-key", DOCUMENT_PROCESS_DLQ_ROUTE_KEY);
        // 消息 TTL：30 分钟（超时未处理则进入 DLQ）
        args.put("x-message-ttl", 30 * 60 * 1000);
        return QueueBuilder.durable(DOCUMENT_PROCESS_QUEUE)
                .withArguments(args)
                .build();
    }

    /** 声明文档处理 Direct 交换机 */
    @Bean
    public DirectExchange documentDirectExchange() {
        return new DirectExchange(DOCUMENT_DIRECT_EXCHANGE, true, false);
    }

    /** 绑定文档处理队列 → 文档交换机 */
    @Bean
    public Binding bindingDocumentProcess() {
        return BindingBuilder
                .bind(documentProcessQueue())
                .to(documentDirectExchange())
                .with(DOCUMENT_PROCESS_ROUTE_KEY);
    }
}
