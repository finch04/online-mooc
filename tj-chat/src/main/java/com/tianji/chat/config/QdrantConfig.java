package com.tianji.chat.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static com.tianji.chat.constants.AiConstants.QDRANT_COLLECTION;

@Data
@Configuration
@ConfigurationProperties(prefix = "qdrant")
public class QdrantConfig {

    private String host;
    private int port;
    private boolean secure;

    @PostConstruct
    public void debugPrint() {
        System.out.printf("[QdrantConfig] host=%s, port=%d, secure=%b%n", host, port, secure);
    }

    @Bean
    public QdrantClient qdrantClient() {
        QdrantGrpcClient.Builder grpcClientBuilder = QdrantGrpcClient.newBuilder(host, port, secure);
        return new QdrantClient(grpcClientBuilder.build());
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return QdrantEmbeddingStore.builder()
                .host(host)
                .port(port)
                .collectionName(QDRANT_COLLECTION)
                .build();
    }
}
