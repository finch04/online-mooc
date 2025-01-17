package com.tianji.aigc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/embedding")
@RequiredArgsConstructor
public class EmbeddingController {

    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    @GetMapping
    public EmbeddingResponse embed(@RequestParam("message") String message) {
        return this.embeddingModel.embedForResponse(List.of(message));
    }

    @PostMapping
    public void saveVectorStore(@RequestParam("message") String message) {
        //构建文档
        Document document = Document.builder()
                .withContent(message)
                .build();
        //存储到向量数据库中
        this.vectorStore.add(List.of(document));
    }

    @DeleteMapping
    public void deleteVectorStore(@RequestParam("ids") List<String> ids) {
        // 删除向量数据库中的数据
        this.vectorStore.delete(ids);
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam("message") String message) {
        return this.vectorStore.similaritySearch(SearchRequest.query(message).withTopK(5));
    }

    @GetMapping("/search/all")
    public List<Document> searchAll() {
        return this.vectorStore.similaritySearch(SearchRequest.query("*"));
    }
}
