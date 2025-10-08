package com.tianji.aigc.controller;

import cn.hutool.core.collection.CollStreamUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/embedding")
@Tag(name = "知识库管理[旧][纯文本]")
@RequiredArgsConstructor
public class EmbeddingController {

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    @Operation(summary = "保存数据到向量数据库")
    @PostMapping
    public void saveVectorStore(@RequestParam("messages") List<String> messages) {
        log.info("保存到向量数据库中，消息数据：{}", messages);
        //构建文档
        List<Document> documents = CollStreamUtil.toList(messages, message -> Document.builder()
                .text(message)
                .build());
        //存储到向量数据库中
        this.vectorStore.add(documents);
        log.info("保存到向量数据库成功, 数量：{}", messages.size());
    }

    @Operation(summary = "将文本向量化")
    @GetMapping
    public EmbeddingResponse embed(@RequestParam("message") String message) {
        return this.embeddingModel.embedForResponse(List.of(message));
    }

    @Operation(summary = "删除向量数据库中的数据")
    @DeleteMapping
    public void deleteVectorStore(@RequestParam("ids") List<String> ids) {
        // 删除向量数据库中的数据
        this.vectorStore.delete(ids);
    }

    @Operation(summary = "搜索向量数据库中的数据")
    @GetMapping("/search")
    public List<Document> search(@RequestParam("message") String message) {
        return this.vectorStore.similaritySearch(SearchRequest.builder().query(message).topK(5).build());
    }

    @Operation(summary = "搜索向量数据库中的所有数据")
    @GetMapping("/search/all")
    public List<Document> searchAll() {
        // 搜索全部数据
        return this.vectorStore.similaritySearch(SearchRequest.builder().query("").topK(999).build());
    }

}
