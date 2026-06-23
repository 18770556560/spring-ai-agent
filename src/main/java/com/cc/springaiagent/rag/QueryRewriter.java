package com.cc.springaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.lang.Nullable;

/**
 * 查询重写器 —— 扩展 {@link RewriteQueryTransformer}，增加重写日志
 * <p>
 * 注意：不直接作为 {@code @Component} 注册，由 {@link RagFullConfig} 通过 Builder 创建。
 * 如需使用，请在配置类中通过 {@link RewriteQueryTransformer#builder()} 构建。
 */
@Slf4j
public class QueryRewriter extends RewriteQueryTransformer {

    public QueryRewriter(ChatClient.Builder chatClientBuilder,
                         @Nullable PromptTemplate promptTemplate,
                         @Nullable String targetSearchSystem) {
        super(chatClientBuilder, promptTemplate, targetSearchSystem);
    }

    /**
     * 便捷工厂方法：通过 ChatModel 创建 QueryRewriter
     */
    public static QueryRewriter create(ChatModel chatModel) {
        return new QueryRewriter(ChatClient.builder(chatModel), null, null);
    }

    @Override
    public Query transform(Query query) {
        log.info("原始查询：{}", query.text());
        Query transformed = super.transform(query);
        log.info("重写后的查询：{}", transformed.text());
        return transformed;
    }
}
