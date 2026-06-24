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
 * Elasticsearch 客户端配置类
 * <p>
 * 负责创建并配置 {@link ElasticsearchClient} Bean，用于与 Elasticsearch 集群进行交互。
 * 适配 ES 8.x+ 版本，使用官方推荐的 elasticsearch-java 客户端库。
 * <p>
 * 主要功能：
 * 1. 解析配置文件中的 ES 连接地址、用户名和密码。
 * 2. 构建带有基本认证（Basic Auth）的 Apache HttpClient。
 * 3. 配置 Jackson 作为 JSON 数据序列化/反序列化的映射器。
 * 4. 创建线程安全的 ElasticsearchClient 实例供全应用使用。
 *
 * @author Lingma
 */
@Configuration
public class ElasticsearchConfig {

    /**
     * Elasticsearch 集群连接地址
     * 对应配置文件项: spring.elasticsearch.uris
     * 格式示例: http://localhost:9200
     */
    @Value("${spring.elasticsearch.uris}")
    private String uri;

    /**
     * Elasticsearch 集群访问用户名
     * 对应配置文件项: spring.elasticsearch.username
     */
    @Value("${spring.elasticsearch.username}")
    private String username;

    /**
     * Elasticsearch 集群访问密码
     * 对应配置文件项: spring.elasticsearch.password
     */
    @Value("${spring.elasticsearch.password}")
    private String password;

    /**
     * 创建 ElasticsearchClient Bean
     * <p>
     * 构建流程：
     * 1. 解析 URI 获取 Host 和 Port。
     * 2. 创建 RestClient 并配置 Basic Auth 认证提供者。
     * 3. 基于 RestClient 创建 RestClientTransport，指定 JacksonJsonpMapper 处理 JSON。
     * 4. 实例化 ElasticsearchClient。
     *
     * @return 配置好的 ElasticsearchClient 实例
     */
    @Bean(destroyMethod = "close")
    public ElasticsearchClient elasticsearchClient() {
        // 解析 URI: 移除协议头并分割 host 和 port
        String[] hostPort = uri.replace("http://", "").split(":");
        String host = hostPort[0];
        int port = Integer.parseInt(hostPort[1]);

        // 构建底层 RestClient (基于 Apache HTTP Client 4)
        RestClient restClient = RestClient.builder(new HttpHost(host, port, "http"))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    // 配置基本认证 (Basic Auth)
                    BasicCredentialsProvider creds = new BasicCredentialsProvider();
                    creds.setCredentials(
                            AuthScope.ANY,
                            new UsernamePasswordCredentials(username, password)
                    );
                    return httpClientBuilder.setDefaultCredentialsProvider(creds);
                })
                .build();

        // 创建传输层，使用 Jackson 处理 JSON 映射
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        // 返回高层客户端实例
        return new ElasticsearchClient(transport);
    }
}
