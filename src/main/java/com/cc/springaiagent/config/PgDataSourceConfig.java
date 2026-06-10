package com.cc.springaiagent.config;

import com.cc.springaiagent.rag.LoveAppDocumentLoader;
import com.cc.springaiagent.rag.MyTokenTextSplitter;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.Resource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

//@Configuration
// 扫描 PG 向量 Mapper
@MapperScan(basePackages = "com.cc.springaiagent.mapper.vector",
        sqlSessionFactoryRef = "pgSqlSessionFactory")
public class PgDataSourceConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.postgres")
    public DataSource pgDataSource() {
        return new HikariDataSource();
    }

    @Bean(name = "pgJdbcTemplate")
    public JdbcTemplate pgJdbcTemplate(@Qualifier("pgDataSource") DataSource pgDataSource) {
        return new JdbcTemplate(pgDataSource);
    }

    @Bean(name = "pgSqlSessionFactory")
    public SqlSessionFactory pgSqlSessionFactory(@Qualifier("pgDataSource") DataSource pgDataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(pgDataSource);
        // 加载 PG 向量库 Mapper.xml
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mapper/vector/*.xml")
        );
        return factoryBean.getObject();
    }

    @Bean
    public VectorStore pgVectorVectorStore(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(1024)                    // 不要盲目设置
                .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                .indexType(HNSW)                     // Optional: defaults to HNSW
                .initializeSchema(true)              // Optional: defaults to false
                .schemaName("public")                // Optional: defaults to "public"
                .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                .maxDocumentBatchSize(10000)         // Optional: defaults to 10000
                .build();
        try {
            //清空表
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'vector_store'",
                    Integer.class
            );
            if (count > 0) {
                jdbcTemplate.execute("TRUNCATE TABLE public.vector_store");
            }

            List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
            int batchSize = 10;
            for (int i = 0; i < documents.size(); i += batchSize) {
                int end = Math.min(i + batchSize, documents.size());
                List<Document> batch = documents.subList(i, end);
                vectorStore.add(batch);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vectorStore;
    }
}