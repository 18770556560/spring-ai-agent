package com.cc.springaiagent.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

/**
 * Re-reading Advisor (重读顾问)
 * <p>
 * 该 Advisor 实现了一种简单的提示工程策略：在发送用户请求给 LLM 之前，
 * 将用户的问题重复一遍，并加上 "Read the question again:" 的指令。
 * <p>
 * 目的：
 * 1. 强调用户的问题，防止模型忽略关键信息。
 * 2. 通过“重读”指令，引导模型进行更深入的思考或自我校验。
 * 3. 适用于逻辑复杂、容易出错的问答场景。
 *
 * @author YourName
 */
public class ReReadingAdvisor implements BaseAdvisor {

    /**
     * 默认的 Re-reading 提示模板
     * <p>
     * 结构：
     * 1. 原始问题
     * 2. 指令："Read the question again: " + 原始问题
     */
    private static final String DEFAULT_RE2_ADVISE_TEMPLATE = """
            {re2_input_query}
            Read the question again: {re2_input_query}
            """;

    /**
     * 当前使用的提示模板
     */
    private final String re2AdviseTemplate;

    /**
     * Advisor 的执行顺序，数值越小优先级越高
     */
    private int order = 0;

    /**
     * 默认构造函数，使用默认的 Re-reading 模板
     */
    public ReReadingAdvisor() {
        this(DEFAULT_RE2_ADVISE_TEMPLATE);
    }

    /**
     * 自定义模板构造函数
     *
     * @param re2AdviseTemplate 自定义的 Re-reading 提示模板，需包含 {re2_input_query} 占位符
     */
    public ReReadingAdvisor(String re2AdviseTemplate) {
        this.re2AdviseTemplate = re2AdviseTemplate;
    }

    /**
     * 在请求发送给 LLM 之前执行
     * <p>
     * 主要逻辑：
     * 1. 获取用户原始消息。
     * 2. 使用模板引擎将原始消息填充到 Re-reading 模板中，生成增强后的提示词。
     * 3. 使用增强后的提示词替换原始用户消息。
     *
     * @param chatClientRequest 当前的聊天请求
     * @param advisorChain      顾问链
     * @return 修改后的聊天请求
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        // 1. 获取用户原始输入文本
        String originalUserText = chatClientRequest.prompt().getUserMessage().getText();

        // 2. 使用 PromptTemplate 渲染增强后的提示词
        // 例如：如果用户问 "1+1=?", 渲染后变成:
        // "1+1=?
        //  Read the question again: 1+1=?"
        String augmentedUserText = PromptTemplate.builder()
                .template(this.re2AdviseTemplate)
                .variables(Map.of("re2_input_query", originalUserText))
                .build()
                .render();

        // 3. 构建新的请求，替换用户消息为增强后的消息
        // mutate() 创建一个新的不可变请求对象的副本，允许修改部分字段
        return chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentUserMessage(augmentedUserText))
                .build();
    }

    /**
     * 在收到 LLM 响应之后执行
     * <p>
     * 当前实现不做任何处理，直接返回原始响应。
     * 如果需要后处理（如解析、过滤），可以在此处添加逻辑。
     *
     * @param chatClientResponse 当前的聊天响应
     * @param advisorChain       顾问链
     * @return 原始聊天响应
     */
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    /**
     * 获取执行顺序
     *
     * @return 顺序值
     */
    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * 设置执行顺序并返回当前实例（链式调用）
     *
     * @param order 新的顺序值
     * @return 当前 ReReadingAdvisor 实例
     */
    public ReReadingAdvisor withOrder(int order) {
        this.order = order;
        return this;
    }
}
