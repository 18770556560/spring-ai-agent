package com.cc.springaiagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.cc.springaiagent"
        ,exclude = {org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration.class})
@MapperScan("com.cc.springaiagent.mapper")
public class SpringAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiAgentApplication.class, args);
    }

}
