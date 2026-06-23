package com.cc.springaiagent.rag;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.KnnSearch;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 混合检索器 —— ES 向量检索 + BM25 文本检索 + RRF 融合
 * <p>
 * 核心流程：
 * 1. 将用户查询向量化 → kNN 向量检索
 * 2. 将用户查询原文 → BM25 全文检索
 * 3. 使用 Reciprocal Rank Fusion (RRF) 融合两路结果
 * 4. 返回 TopK 文档
 * <p>
 */
@Slf4j
@Component
public class HybridSearchDocumentRetriever implements DocumentRetriever {

    @Value("${spring.es.hybrid.index-name:knowledge_base}")
    private String indexName;

    @Value("${spring.es.hybrid.similarity-threshold:0.5}")
    private double similarityThreshold;

    @Value("${spring.es.hybrid.top-k:10}")
    private int topK;

    @Value("${spring.es.hybrid.num-candidates:20}")
    private int numCandidates;

    @Value("${spring.es.hybrid.rank-window-size:10}")
    private int rankWindowSize;

    @Value("${spring.es.hybrid.rank-constant:60}")
    private int rankConstant;

    @Resource
    private ElasticsearchClient esClient;

    @Resource
    private EmbeddingModel dashscopeEmbeddingModel;

    /**
     * 混合检索入口
     */
    @Override
    public List<Document> retrieve(Query query) {
        String queryText = query.text();
        log.info("混合检索开始, 查询: {}", queryText);

        try {
            // 1. 生成查询向量 (DashScope text-embedding-v3, 1024维)
            float[] queryEmbedding = dashscopeEmbeddingModel.embed(queryText);

            // 2. kNN 向量检索
            List<ScoredDoc> vectorResults = executeKnnSearch(queryEmbedding);
            log.info("向量检索返回 {} 条结果", vectorResults.size());

            // 3. BM25 文本检索
            List<ScoredDoc> textResults = executeBm25Search(queryText);
            log.info("BM25 检索返回 {} 条结果", textResults.size());

            // 4. RRF 融合
            List<Document> fusedDocs = reciprocalRankFusion(vectorResults, textResults);
            log.info("RRF 融合后 {} 条结果", fusedDocs.size());

            // 5. 相似度阈值过滤 + 截取 TopK
            List<Document> topDocs = fusedDocs.stream()
                    .filter(doc -> {
                        Double rrfScore = (Double) doc.getMetadata().get("es_score");
                        return rrfScore != null && rrfScore >= similarityThreshold;
                    })
                    .limit(topK)
                    .collect(Collectors.toList());

            log.info("混合检索完成, 最终返回 {} 条文档", topDocs.size());
            return topDocs;

        } catch (Exception e) {
            log.error("混合检索失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // ==================== kNN 向量检索 ====================

    /**
     * 使用 KnnSearch（非 KnnQuery）作为 kNN 检索入口
     */
    private List<ScoredDoc> executeKnnSearch(float[] queryEmbedding) throws Exception {
        List<Float> vectorList = embeddingToFloatList(queryEmbedding);

        SearchRequest request = SearchRequest.of(s -> s
                .index(indexName)
                .knn(List.of(KnnSearch.of(k -> k
                        .field("embedding")
                        .queryVector(vectorList)
                        .k(topK)
                        .numCandidates(numCandidates)
                )))
                .source(src -> src.filter(f -> f.includes(List.of("id", "content", "metadata"))))
        );

        SearchResponse<Map> response = esClient.search(request, Map.class);
        return hitsToScoredDocs(response);
    }

    // ==================== BM25 文本检索 ====================

    private List<ScoredDoc> executeBm25Search(String queryText) throws Exception {
        SearchRequest request = SearchRequest.of(s -> s
                .index(indexName)
                .query(q -> q
                        .match(m -> m
                                .field("content")
                                .query(queryText)
                        )
                )
                .size(topK)
                // ES 9.x: SourceConfig 使用 filter(SourceFilter)
                .source(src -> src.filter(f -> f.includes(List.of("id", "content", "metadata"))))
        );

        SearchResponse<Map> response = esClient.search(request, Map.class);
        return hitsToScoredDocs(response);
    }

    // ==================== RRF 融合 ====================

    /**
     * Reciprocal Rank Fusion（倒数排名融合）
     * <p>
     * 公式: RRF(doc) = Σ 1 / (k + rank_i(doc))
     * 其中 k 为 rankConstant（默认 60），rank_i 为文档在第 i 路检索结果中的排名（从 1 开始）。
     */
    private List<Document> reciprocalRankFusion(List<ScoredDoc> vectorResults, List<ScoredDoc> textResults) {
        Map<String, Double> rrfScores = new LinkedHashMap<>();
        Map<String, ScoredDoc> docMap = new LinkedHashMap<>();

        // 向量检索结果路
        for (int i = 0; i < Math.min(vectorResults.size(), rankWindowSize); i++) {
            ScoredDoc doc = vectorResults.get(i);
            double rrfScore = 1.0 / (rankConstant + i + 1);
            rrfScores.merge(doc.id, rrfScore, Double::sum);
            docMap.putIfAbsent(doc.id, doc);
        }

        // BM25 文本检索结果路
        for (int i = 0; i < Math.min(textResults.size(), rankWindowSize); i++) {
            ScoredDoc doc = textResults.get(i);
            double rrfScore = 1.0 / (rankConstant + i + 1);
            rrfScores.merge(doc.id, rrfScore, Double::sum);
            docMap.putIfAbsent(doc.id, doc);
        }

        // 按 RRF 分数降序排列
        return rrfScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(entry -> {
                    ScoredDoc scoredDoc = docMap.get(entry.getKey());
                    Document document = Document.builder()
                            .id(scoredDoc.id)
                            .text(scoredDoc.content)
                            .metadata(scoredDoc.metadata)
                            .build();
                    document.getMetadata().put("rrf_score", entry.getValue());
                    return document;
                })
                .collect(Collectors.toList());
    }

    // ==================== 工具方法 ====================

    /**
     * float[] → List&lt;Float&gt;（ES KnnSearch.queryVector 接受 List&lt;Float&gt;）
     */
    private List<Float> embeddingToFloatList(float[] embedding) {
        List<Float> list = new ArrayList<>(embedding.length);
        for (float v : embedding) {
            list.add(v);
        }
        return list;
    }

    /**
     * ES 搜索结果 → ScoredDoc 列表
     */
    @SuppressWarnings("unchecked")
    private List<ScoredDoc> hitsToScoredDocs(SearchResponse<Map> response) {
        return response.hits().hits().stream()
                .map(hit -> {
                    Map<String, Object> source = hit.source();
                    String id = hit.id();
                    double score = hit.score() != null ? hit.score() : 0.0;
                    String content = source != null
                            ? (String) source.getOrDefault("content", "")
                            : "";
                    Map<String, Object> metadata = new HashMap<>();
                    if (source != null && source.get("metadata") instanceof Map) {
                        metadata.putAll((Map<String, Object>) source.get("metadata"));
                    }
                    metadata.put("es_score", score);
                    return new ScoredDoc(id, content, metadata, score);
                })
                .collect(Collectors.toList());
    }

    /**
     * 带分数的文档内部类
     */
    record ScoredDoc(String id, String content, Map<String, Object> metadata, double score) {
    }
}
