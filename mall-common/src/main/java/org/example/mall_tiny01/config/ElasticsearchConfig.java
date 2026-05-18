package org.example.mall_tiny01.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 客户端配置。
 * 新版 ES Java Client (co.elastic.clients)，Spring Boot 3.x 配套。
 */
@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris:http://192.168.158.130:9200}")
    private String esUri;

    @Bean
    public RestClient restClient() {
        return RestClient.builder(HttpHost.create(esUri)).build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClientTransport transport = new RestClientTransport(
                restClient(), new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
