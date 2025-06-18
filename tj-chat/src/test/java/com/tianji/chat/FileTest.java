package com.tianji.chat;/**
 * @author fsq
 * @date 2025/6/18 16:53
 */

import com.tianji.chat.utils.QdrantEmbeddingUtils;
import com.tianji.common.utils.UserContext;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.WithVectorsSelectorFactory;
import io.qdrant.client.grpc.Points;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static com.tianji.chat.constants.AiConstants.QDRANT_COLLECTION;
import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;

/**
 * @Author: fsq
 * @Date: 2025/6/18 16:53
 * @Version: 1.0
 */
@SpringBootTest
@Slf4j
public class FileTest {

    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private QdrantClient qdrantClient;

    @Test
    public void test() throws Exception {

        String message = "为什么天空是蓝色的";
        Long userId = 2L;
            // 1. 向量化问题
            Embedding queryEmbedding = embeddingModel.embed(message).content();

            // 2. 查询向量数据库
            Points.Filter filter = Points.Filter.newBuilder().addMust(matchKeyword("user_id", userId.toString())).build();
            List<Points.ScoredPoint> results = qdrantClient.searchAsync(Points.SearchPoints.newBuilder()
                    .setCollectionName(QDRANT_COLLECTION)
                    .addAllVector(queryEmbedding.vectorAsList())
                    .setLimit(3)
                    .setWithPayload(enable(true))
                    .setWithVectors(WithVectorsSelectorFactory.enable(true))
                    .setFilter(filter)
                    .build()).get();

            List<EmbeddingMatch<TextSegment>> matches = results.stream()
                    .map(point -> QdrantEmbeddingUtils.toEmbeddingMatch(point, queryEmbedding, "text_segment"))
                    .collect(Collectors.toList());

            // 打印匹配结果用于调试
            for (EmbeddingMatch<TextSegment> match : matches) {
                log.info("匹配得分: {}", match.score());
                log.info("匹配内容:\n{}", match.embedded().text());
            }

            // 3. 拼接 context 参考材料
            String context = matches.stream()
                    .map(match -> "- " + match.embedded().text())
                    .collect(Collectors.joining("\n"));

            Response<List<Embedding>> listResponse = embeddingModel.embedAll(matches.stream().map(i -> i.embedded()).collect(Collectors.toList()));
            List<Embedding> content = listResponse.content();

            System.out.println( content);


//        model.generate(message,new ToolspecificPromptBuilder )

    }

}
