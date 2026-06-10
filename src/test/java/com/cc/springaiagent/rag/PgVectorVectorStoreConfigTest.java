package com.cc.springaiagent.rag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PgVectorVectorStoreConfigTest {

    @Autowired
    VectorStore vectorStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Test
    void checkEmbDim(){
        float[] vec = embeddingModel.embed("随便一句测试文本");
        System.out.println("向量维度 = "+vec.length);
    }

    @Test
    void pgVectorVectorStore() {
        List<Document> documents = List.of(
                new Document("如果你是单身，碰到喜欢的就去追吧", Map.of("status", "单身")),
                new Document("如果你正在恋爱，就好好享受过程珍惜当下", Map.of("status", "恋爱")),
                new Document("如果你结婚了，就挑起大梁节俭持家吧", Map.of("status", "结婚")));

        vectorStore.add(documents);

        //查询
        List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
        Assertions.assertNotNull( results);
    }
}