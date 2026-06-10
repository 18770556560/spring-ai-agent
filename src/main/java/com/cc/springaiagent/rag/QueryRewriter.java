package com.cc.springaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 查询重写器
 */
@Slf4j
@Component
public class QueryRewriter extends RewriteQueryTransformer{


    public QueryRewriter(ChatClient.Builder chatClientBuilder, @Nullable PromptTemplate promptTemplate, @Nullable String targetSearchSystem) {
        super(chatClientBuilder, promptTemplate, targetSearchSystem);
    }

    @Override
    public Query transform(Query query) {
        log.info("原始查询：{}", query.text());
        Query transform = super.transform(query);
        log.info("重写后的查询：{}", transform.text());
        return transform;
    }
}
