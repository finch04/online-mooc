package com.tianji.aigc.service;


import com.tianji.aigc.domain.po.KnowledgeDocs;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.List;

public interface SegmentService {

    // 保存文件分片到向量库（已有基础逻辑，补充完善）
    void saveSegments(KnowledgeDocs doc);

    // 根据文档ID查询其所有分片
    List<Document> getSegmentsByDocId(Long docId);

    // 根据文档ID删除其所有分片（级联删除）
    void deleteSegmentsByDocId(Long docId);

    // 根据向量ID列表删除分片（已有基础，补充完善）
    void deleteSegmentsByVectorIds(List<String> vectorIds);

    // 生成文本嵌入向量（复用已有embed逻辑）
    EmbeddingResponse generateEmbedding(String message);

}
