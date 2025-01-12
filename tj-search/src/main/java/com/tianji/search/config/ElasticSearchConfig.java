package com.tianji.search.config;

import cn.hutool.core.collection.CollUtil;
import jakarta.annotation.Resource;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

    @Resource
    private ElasticsearchProperties elasticsearchProperties;

    @Bean
    public RestHighLevelClient restClient() {
        String uri = CollUtil.getFirst(this.elasticsearchProperties.getUris());
        return new RestHighLevelClient(RestClient.builder(HttpHost.create(uri)));
    }
}
