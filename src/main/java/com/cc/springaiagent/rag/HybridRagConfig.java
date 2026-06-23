package com.cc.springaiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 混合检索 RAG 配置 —— 基于 Elasticsearch 向量 + BM25 融合的完整 RAG 管线
 * <p>
 * 与 {@link RagFullConfig} 平行的配置类，使用 ES 混合检索器替代纯 PGVector 检索器。
 * <p>
 * 注意：本配置不依赖 PostgreSQL/pgvector，仅依赖 Elasticsearch + DashScope Embedding。
 * 所有依赖均自给自足，无需 PostgreSQL。
 * <p>
 * 管线流程：
 * 1. 预检索：Query 翻译 + 重写
 * 2. 检索：ES 混合检索（kNN 向量 + BM25 文本 + RRF 融合）
 * 3. 生成：上下文增强
 * <p>
 * 适配 Spring AI 1.1.2 + ES 9.x (elasticsearch-java 9.4.2)。
 */
@Configuration
public class HybridRagConfig {

    // ==================== 预检索：Query 改写 ====================

    @Bean
    public RewriteQueryTransformer hybridRewriteQueryTransformer(ChatModel chatModel) {
        return RewriteQueryTransformer.builder()
                .chatClientBuilder(ChatClient.builder(chatModel))
                .build();
    }

    // ==================== 预检索：Query 翻译 ====================

    @Bean
    public TranslationQueryTransformer hybridTranslationQueryTransformer(ChatModel chatModel) {
        return TranslationQueryTransformer.builder()
                .chatClientBuilder(ChatClient.builder(chatModel))
                .targetLanguage("中文")
                .build();
    }

    // ==================== 检索：使用 ES 混合检索器 ====================
    // HybridSearchDocumentRetriever 已通过 @Component 注册为 Bean，直接注入即可

    // ==================== 后检索：重排序（精排） ====================
    // 注意：以下 Bean 依赖 pgVectorVectorStore，如果未启动 PostgreSQL 会导致启动失败。
    // 如需启用精排，请确保 PostgreSQL 可用并取消以下注释：
    //
    // @Bean
    // public RetrievalRerankAdvisor hybridRetrievalRerankAdvisor(VectorStore pgVectorVectorStore,
    //                                                            RerankModel rerankModel) {
    //     return new RetrievalRerankAdvisor(
    //             pgVectorVectorStore,
    //             rerankModel,
    //             SearchRequest.builder()
    //                     .topK(1)
    //                     .similarityThreshold(0.5)
    //                     .build());
    // }

    // ==================== 生成：上下文增强 ====================

    @Bean
    public QueryAugmenter hybridQueryAugmenter() {
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .emptyContextPromptTemplate(new PromptTemplate(
                        "你直接回答'当前暂无该问题的相关解答，请您咨询专业人员'"))
                .build();
    }

    // ==================== 组装：完整混合 RAG Advisor ====================

    /**
     * 完整的混合检索 RAG 顾问 Bean
     * <p>
     * 集成：翻译 → 重写 → ES混合检索 → 上下文增强
     * 注入到 {@link com.cc.springaiagent.app.ForLove} 中调用。
     * <p>
     * 注意：queryTransformers 接受 varargs，多次调用会覆盖而非追加，
     * 因此需要在一次调用中传入所有 transformer。
     */
    @Bean
    public Advisor fullHybridRetrievalAugmentationAdvisor(
            //查询翻译
//            TranslationQueryTransformer hybridTranslationQueryTransformer,
            //查询重写
            RewriteQueryTransformer hybridRewriteQueryTransformer,
            // 检索：ES 混合检索（kNN 向量 + BM25 文本 + RRF 融合）
            HybridSearchDocumentRetriever hybridSearchDocumentRetriever,
            // 生成：上下文增强
            QueryAugmenter hybridQueryAugmenter) {

        return RetrievalAugmentationAdvisor.builder()
                // 预检索：翻译（确保查询为中文）+ 重写（优化查询表达）
                .queryTransformers(
//                        hybridTranslationQueryTransformer,
                        hybridRewriteQueryTransformer
                )
                // 检索：ES 混合检索（kNN 向量 + BM25 文本 + RRF 融合）
                .documentRetriever(hybridSearchDocumentRetriever)
                // 生成：上下文增强
                .queryAugmenter(hybridQueryAugmenter)
                .build();
    }
}
