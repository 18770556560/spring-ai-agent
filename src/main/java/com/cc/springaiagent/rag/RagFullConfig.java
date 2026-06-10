package com.cc.springaiagent.rag;


import com.alibaba.cloud.ai.advisor.RetrievalRerankAdvisor;
import com.alibaba.cloud.ai.model.RerankModel;
import dev.langchain4j.agent.tool.P;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class RagFullConfig {

    // 3. 预检索模块：Query 改写（优化用户问题，提升检索效果）
    @Bean
    public RewriteQueryTransformer myRewriteQueryTransformer(ChatModel chatModel) {
        return RewriteQueryTransformer.builder()
                .chatClientBuilder(ChatClient.builder(chatModel))
//                .promptTemplate(new PromptTemplate("重写问题：{question}"))//可自定义重写模板
                .build();
    }
    @Bean
    public TranslationQueryTransformer myTranslationQueryTransformer(ChatModel chatModel) {

        return TranslationQueryTransformer.builder()
                .chatClientBuilder(ChatClient.builder(chatModel))
                .targetLanguage("中文")//可自定义目标语言
                .build();
    }


    // 4. 检索模块：向量检索器（配置相似度阈值、返回数量）
    @Bean
    public VectorStoreDocumentRetriever documentRetriever(VectorStore pgVectorVectorStore) {
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(pgVectorVectorStore)
                .similarityThreshold(0.50)  // 相似度阈值，低于该值的文档会被过滤
                .topK(10)                    // 最多返回 10 条相关文档
//                .filterExpression(new FilterExpressionBuilder().eq("filename", "单身人员册.md").build())// 可自定义过滤条件
                .build();
    }

    // 5. 后检索模块：文档去重 + 重排序
    //去重
    @Bean
    public DocumentPostProcessor myDocumentPostProcessor() {
        // 使用HashSet记录已见过的文档ID，以实现基于ID的去重逻辑
        return (query, documents) -> {
            Set<String> seenIds = new HashSet<>();
            return documents.stream()
                    .filter(doc -> seenIds.add(doc.getId()))
                    .toList();
        };
    }

    //重排序（精排）
    @Bean
    public RetrievalRerankAdvisor myRetrievalRerankAdvisor(VectorStore pgVectorVectorStore,
                                                RerankModel rerankModel) {
        return new RetrievalRerankAdvisor(
                pgVectorVectorStore,
                rerankModel,
                SearchRequest.builder()
                        .topK(5)
                        .similarityThreshold(0.5)
                        .build());
    }

    // 6. 生成模块：上下文增强器（把检索到的文档拼接到prompt中）
    @Bean
    public QueryAugmenter myQueryAugmenter() {
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)  // 不允许无上下文回答，避免模型瞎编
                .emptyContextPromptTemplate(new PromptTemplate("你直接回答'我是恋爱专家，只能回答相关问题'"))//可自定义拼接模板
                .build();
    }

    // 7. 组装全流程 RetrievalAugmentationAdvisor
    @Bean
    public Advisor fullRetrievalAugmentationAdvisor(
            RewriteQueryTransformer myRewriteQueryTransformer,
            QueryRewriter queryRewriter,
            TranslationQueryTransformer myTranslationQueryTransformer,
            VectorStoreDocumentRetriever documentRetriever,
            DocumentPostProcessor myDocumentPostProcessor,
            RetrievalRerankAdvisor myRetrievalRerankAdvisor,
            QueryAugmenter myQueryAugmenter) {

        return RetrievalAugmentationAdvisor.builder()
                // 预检索：Query 改写
                .queryTransformers(myTranslationQueryTransformer)//翻译
//                .queryTransformers(myRewriteQueryTransformer)//重写
                .queryTransformers(queryRewriter)//重写
                // 检索：向量检索
                .documentRetriever(documentRetriever)
                // 后检索：去重 + 重排序
//                .documentPostProcessors(myDocumentPostProcessor, myRetrievalRerankAdvisor)
                // 生成：上下文增强
                .queryAugmenter(myQueryAugmenter)
                .build();
    }
}