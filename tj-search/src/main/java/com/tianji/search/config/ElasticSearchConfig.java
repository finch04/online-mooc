package com.tianji.search.config;

import cn.hutool.core.collection.CollUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.Resource;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {
    @Resource
    ObjectMapper objectMapper;

    @Bean
    public RestClient restClient(ElasticsearchProperties properties) {
        String uri = CollUtil.getFirst(properties.getUris());
        return RestClient.builder(HttpHost.create(uri)).build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {

        // 创建JsonpMapper
        JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(objectMapper);

        // 创建Transport和Client
        ElasticsearchTransport transport = new RestClientTransport(restClient, jsonpMapper);
        return new ElasticsearchClient(transport);
    }
}