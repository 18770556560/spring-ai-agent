package com.cc.springaiagent.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 客户端配置 —— 适配 ES 8.x (elasticsearch-java 8.18.8)
 * <p>
 * Spring Boot 3.5.14 管理 elasticsearch-java 8.18.8，传输层使用
 * {@link RestClientTransport} (基于 Apache HTTP Client 4)。
 */
@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris}")
    private String uri;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Bean(destroyMethod = "close")
    public ElasticsearchClient elasticsearchClient() {
        String[] hostPort = uri.replace("http://", "").split(":");
        String host = hostPort[0];
        int port = Integer.parseInt(hostPort[1]);

        // ES 8.x: 使用 RestClient (Apache HTTP Client 4) + RestClientTransport
        RestClient restClient = RestClient.builder(new HttpHost(host, port, "http"))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    BasicCredentialsProvider creds = new BasicCredentialsProvider();
                    creds.setCredentials(
                            AuthScope.ANY,
                            new UsernamePasswordCredentials(username, password)
                    );
                    return httpClientBuilder.setDefaultCredentialsProvider(creds);
                })
                .build();

        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
