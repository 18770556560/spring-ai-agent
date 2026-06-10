package com.cc.springaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 向量本地存储配置类，用于初始化和管理应用所需的向量数据库实例。
 */
@Configuration
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Resource
    private MyDocumentEnricher myDocumentEnricher;



    /**
     * 创建并配置向量存储Bean。
     * <p>
     * 该方法基于提供的嵌入模型构建一个简单的向量存储实例，
     * 加载Markdown文档并将其添加到向量存储中，最后返回配置好的向量存储对象。
     *
     * @param dashscopeEmbeddingModel 用于生成文本嵌入的嵌入模型
     * @return 配置完成且已加载文档的向量存储实例
     */
    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {

        // 加载Markdown文档并添加到向量存储中
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        // 添加关键词元信息
//        documents=myDocumentEnricher.enrichDocumentsByKeyword( documents);
        //添加摘要元信息
//        documents=myDocumentEnricher.enrichDocumentsBySummary( documents);
        //框架中的simpleVectorStore无知识库数据，所以编写这个类进行读取知识库数据 存入内部向量库中
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();
        simpleVectorStore.add(documents);
        return simpleVectorStore;
    }
}
