package com.tianji.chat;
import java.util.Arrays;
import java.util.stream.Collectors;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;
import org.apache.pdfbox.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static io.qdrant.client.ConditionFactory.matchKeyword;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class QdrantTest {

    @Autowired
    private QdrantClient qdrantClient;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private EmbeddingModel embeddingModel;
    private String testCollectionName;
    private static final String AI_CHAT_COLLECTION = "ai-chat";
    private static final int VECTOR_SIZE = 1536; // 根据您的嵌入模型调整向量维度
    @BeforeEach
    public void setUp() {
        // 生成唯一的测试集合名称
        testCollectionName = "test_collection_" + UUID.randomUUID().toString().substring(0, 8);
        // 创建 ai-chat 集合
        createAiChatCollection();
    }

    @AfterEach
    public void tearDown() {
        // 清理测试集合
        try {
            qdrantClient.deleteCollectionAsync(testCollectionName);
        } catch (Exception e) {
            // 忽略清理错误
        }
    }
    private void createAiChatCollection() {
        try {
            // 获取所有集合列表，检查 ai-chat 集合是否存在
            List<String> existingCollections = new ArrayList<>();
            List<String> strings = qdrantClient.listCollectionsAsync().get();

            existingCollections.addAll(strings);

            if (!existingCollections.contains(AI_CHAT_COLLECTION)) {

                Collections.VectorParams build = Collections.VectorParams.newBuilder()
                        .setDistance(Collections.Distance.Cosine)
                        .setSize(VECTOR_SIZE).build();

                // 定义向量配置
                Collections.VectorsConfig vectorsConfig = Collections.VectorsConfig.newBuilder()
                        .setParams(build)
                        .build();

                // 创建集合
                Collections.CreateCollection request = Collections.CreateCollection.newBuilder()
                        .setCollectionName(AI_CHAT_COLLECTION)
                        .setVectorsConfig(vectorsConfig)
                        .build();

                qdrantClient.createCollectionAsync(request);

                // 等待集合创建完成
                Thread.sleep(1000);
                System.out.println("成功创建集合: " + AI_CHAT_COLLECTION);
            } else {
                System.out.println("集合已存在: " + AI_CHAT_COLLECTION);
            }
        } catch (Exception e) {
            fail("创建集合失败: " + e.getMessage());
        }
    }
    @Test
    public void testEmbeddingModel() {
        String text = "这是一个测试文本。";
        Embedding embedding = embeddingModel.embed(text).content();
        float[] vector = embedding.vector();
        System.out.println("文本嵌入向量: " + vector);
    }
    @Test
    public void testQdrantClientCreation() {
        assertNotNull(qdrantClient, "QdrantClient should not be null");
    }

    @Test
    public void testEmbeddingStoreCreation() {
        assertNotNull(embeddingStore, "EmbeddingStore should not be null");
    }

    @Test
    public void testBasicQdrantOperations() throws Exception {
        // 1. 准备测试数据
        List<String> testTexts = List.of(
                "人工智能是计算机科学的一个分支",
                "机器学习是人工智能的一个子领域",
                "深度学习是机器学习的一个子领域",
                "are you ok?"
        );

        // 2. 创建文本片段
        List<TextSegment> segments = testTexts.stream()
                .map(TextSegment::from)
                .collect(Collectors.toList());
        // 3. 计算嵌入向量
        List<dev.langchain4j.data.embedding.Embedding> embeddings = segments.stream()
                .map(segment ->{
                        segment.metadata("tj"+UUID.randomUUID().toString());
                        segment.metadata().put("userId", "8");
                        segment.metadata().put("docId", UUID.randomUUID().toString());
                        return embeddingModel.embed(segment).content();
                })
                .collect(Collectors.toList());

        // 4. 存储嵌入向量
        embeddingStore.addAll(embeddings, segments);
        // 构建过滤条件
//        var metadataFilter = metadataKey("userId").isEqualTo(targetUserId);
        // 5. 执行相似度搜索
        String query = "什么是机器学习?";
        dev.langchain4j.data.embedding.Embedding queryEmbedding = embeddingModel.embed(query).content();
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(queryEmbedding, 2 );

        // 6. 验证结果
        assertFalse(matches.isEmpty(), "搜索结果不应为空");
        assertEquals(2, matches.size(), "应返回2个匹配项");

        // 打印结果
        System.out.println("\n===== 相似度搜索结果 =====");
        for (EmbeddingMatch<TextSegment> match : matches) {
            System.out.printf("相似度: %.4f, 内容: %s\n",
                    match.score(), match.embedded().text());
        }
    }

    @Test
    public void testCreatePoint() {
        // 创建一个1536维的向量
        double[] vectorData = new double[1536];

        // 填充示例数据（使用double值）
        Arrays.fill(vectorData, 0.5); // 注意：0.5默认是double类型

        // 构建向量对象
        Points.Vector vector = Points.Vector.newBuilder()
                .addAllData(Arrays.stream(vectorData)
                        .mapToObj(d -> (float) d) // 将double转换为Float对象
                        .collect(Collectors.toList()))
                .build();

        // 设置向量到Points.Vectors对象
        Points.Vectors vectors = Points.Vectors.newBuilder()
                .setVector(vector)
                .build();
        qdrantClient.upsertAsync(AI_CHAT_COLLECTION,
                List.of(
                        Points.PointStruct.newBuilder()
                                .setId(Points.PointId.newBuilder().setNum(22L).build())
                                .setVectors(vectors)
                                .build()));
    }

    @Test
    public void testWithCleanCollection() {
        try {
            // 检查集合是否存在
            Collections.CollectionInfo collectionInfo = qdrantClient.getCollectionInfoAsync(AI_CHAT_COLLECTION).get();

            if (collectionInfo!=null) {
                qdrantClient.deleteAsync(AI_CHAT_COLLECTION,
                        Points.Filter.newBuilder()
                                .addMust(matchKeyword("doc_id", "1933081651337330690"))
                                .build());

                // 等待操作完成
                Thread.sleep(500);
                System.out.println("成功清空集合: " + AI_CHAT_COLLECTION);
            } else {
                System.out.println("集合不存在: " + AI_CHAT_COLLECTION);
            }
        } catch (Exception e) {
            fail("清空集合失败: " + e.getMessage());
        }
    }
}
