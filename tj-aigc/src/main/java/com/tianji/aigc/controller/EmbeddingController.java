package com.tianji.aigc.controller;

import cn.hutool.core.collection.CollStreamUtil;
import jakarta.annotation.Resource;
import jakarta.annotation.Resources;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/embedding")
@RequiredArgsConstructor
public class EmbeddingController {

    @Resource(name = "RedisVectorStore")
    private final VectorStore vectorStore;

    private final EmbeddingModel embeddingModel;

    @PostMapping
    public void saveVectorStore(@RequestParam(value = "messages") List<String> messages) {
        log.info("保存到向量库中，数据为:{}",messages);
        //构建文档
        List<Document> documents = CollStreamUtil.toList(messages, message -> Document.builder().text(message).build());
        //存储向量到向量库中
        vectorStore.add(documents);
        log.info("保存到向量数据库成功，数量{}",messages.size());
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam("message")String message ){
        return this.vectorStore.similaritySearch(SearchRequest.builder().query(message).similarityThreshold(0.6f).topK(5).build());
    }

    @GetMapping
    public EmbeddingResponse embed(@RequestParam("message")String message){
        return this.embeddingModel.embedForResponse(List.of(message));

    }

    @DeleteMapping
    public void deleteVectorStore(@RequestParam("ids")List<String> ids) {
        this.vectorStore.delete(ids);
    }

    @GetMapping("/search/all")
    public List<Document> searchAll(){
        return this.vectorStore.similaritySearch(SearchRequest.builder().query("").topK(999).build());
    }

}
