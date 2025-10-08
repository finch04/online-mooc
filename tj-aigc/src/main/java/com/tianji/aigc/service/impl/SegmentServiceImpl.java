package com.tianji.aigc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.domain.po.KnowledgeDocs;
import com.tianji.aigc.domain.po.MarkdownChunk;
import com.tianji.aigc.service.SegmentService;
import com.tianji.aigc.utils.MarkdownSplitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SegmentServiceImpl implements SegmentService {
    // 阿里云 DashScope 嵌入模型（生成向量）
    private final EmbeddingModel embeddingModel;
    // Qdrant 向量存储（Spring AI 适配实现，支持原生查询）
    private final QdrantVectorStore qdrantVectorStore;
    // Spring AI 标准向量存储接口（通用增删查操作）
    private final VectorStore vectorStore;
    private final MarkdownSplitter markdownSplitter;

    /**
     * 增加：保存文件分片到向量库（含Markdown分片、向量生成、元数据关联）
     * @param doc 原始文件（KnowledgeDocs，含ID、内容、用户ID等）
     */
    @Override
    public Integer saveSegments(KnowledgeDocs doc) {
        // 1. 入参校验
        if (doc == null ||StrUtil.isEmpty(doc.getContent())) {
            log.error("保存文件分片失败：入参无效（docId={}, contentLength={}",
                    doc.getId(), doc.getContent() == null ? 0 : doc.getContent().length());
            throw new IllegalArgumentException("文件ID和内容不能为空");
        }

        String markdownContent = doc.getContent();
        Integer splitLevel = doc.getLevel();
        Long docId = doc.getId();
        Long userId = doc.getUserId();

        // 2. Markdown 分片（使用改造后的分片器，自动限制单个分片大小）
        List<MarkdownChunk> markdownChunks;
        if (splitLevel != null && splitLevel >= 1 && splitLevel <= 6) {
            // 按指定级别分片（如 H2）
            markdownChunks = markdownSplitter.getMarkdownChunksByH(markdownContent, splitLevel);
            log.info("文档[{}]按指定级别 H{} 分片，初始分片数：{}",
                    doc.getFileName(), splitLevel, markdownChunks.size());
        } else {
            // 智能分片（自动找最大标题级别 + 长度限制）
            markdownChunks = markdownSplitter.smartSplitByHeading(markdownContent);
            log.info("文档[{}]智能分片，初始分片数：{}",  doc.getFileName(), markdownChunks.size());
        }

        // 3. 兜底：若分片为空（极端情况，如纯空白内容）
        if (CollectionUtils.isEmpty(markdownChunks)) {
            markdownChunks = new ArrayList<>();
            markdownChunks.add(new MarkdownChunk("全文", markdownContent.trim()));
            log.warn("文档[{}]分片为空，兜底为1个全文分片", docId);
        }

        // 4. 转换为 Spring AI Document（含元数据）
        List<Document> documents = markdownChunks.stream()
                .map(chunk -> new Document(
                        chunk.getContent(), // 分片内容（已确保 ≤ maxChunkSize）
                        Map.of(
                                "user_id", userId.toString(), // 关联上传用户
                                "chunk_title", chunk.getTitle(), // 分片标题
                                "create_time", new Date().toString(), // 创建时间
                                "chunk_size", chunk.getContent().length() // 分片大小（用于日志和监控）
                        )
                ))
                .collect(Collectors.toList());

        // 5. 校验分片大小（兜底检查，确保无超大分片）
        int maxChunkSize = markdownSplitter.getSplitterProperties().getMaxChunkSize();
        List<Document> validDocs = documents.stream()
                .filter(document -> {
                    int contentLength = document.getText().length();
                    boolean valid = contentLength <= maxChunkSize;
                    if (!valid) {
                        log.error("文档[{}]存在超大分片，已过滤（chunkTitle={}, contentLength={}, maxSize={}",
                                docId, document.getMetadata().get("chunk_title"), contentLength, maxChunkSize);
                    }
                    return valid;
                })
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(validDocs)) {
            log.error("文档[{}]所有分片均超限制，无法保存", docId);
            throw new RuntimeException("分片大小超过限制，无法保存");
        }

        // 6. 分批保存到向量库（避免批量过大触发 Embedding 400 错误，之前实现的分批逻辑）
        int batchSize = 10; // 对应 Embedding 模型的批量限制
        for (int i = 0; i < validDocs.size(); i += batchSize) {
            int end = Math.min(i + batchSize, validDocs.size());
            List<Document> batchDocs = validDocs.subList(i, end);
            vectorStore.add(batchDocs);
            log.info("文档[{}]分批保存向量库：第{}批，大小{}", docId, (i / batchSize) + 1, batchDocs.size());
        }

        log.info("文档[{}]分片保存完成：最终有效分片数{}，总大小{}字符",
                docId, validDocs.size(), validDocs.stream().mapToInt(d -> d.getText().length()).sum());
        return validDocs.size();
    }

    /**
     * 查询单个：根据原始文件ID，查询其所有向量分片
     * @param docId 原始文件ID（KnowledgeDocs.id）
     * @return 该文件的所有分片Document
     */
    @Override
    public List<Document> getSegmentsByDocId(Long docId) {
        if (docId == null) {
            log.error("查询文件分片失败：docId为空");
            return Collections.emptyList();
        }

        // 核心：通过Qdrant的元数据过滤（filter by doc_id），避免全量搜索
        // 构造过滤条件：元数据中"doc_id"等于目标ID
        Map<String, Object> filter = Collections.singletonMap("doc_id", docId.toString());
        // 执行带过滤的相似性搜索（query用空字符串，topK设为最大合理值）
        SearchRequest searchRequest = SearchRequest.builder()
                .query("") // 空查询（仅过滤，不按相似度排序）
                .topK(1000) // 单个文件最大分片数（可根据业务调整）
                .filterExpression(filter.toString()) // 关键：按doc_id过滤
                .build();

        // 执行查询并提取Document
        List<Document> segments = vectorStore.similaritySearch(searchRequest);
        if (CollectionUtils.isEmpty(segments)) {
            log.info("文件[{}]未查询到分片", docId);
            return Collections.emptyList();
        }

        log.info("文件[{}]查询到{}个分片", docId, segments.size());
        return segments;
    }


    /**
     * 删除：根据原始文件ID，级联删除其所有向量分片
     * @param docId 原始文件ID（KnowledgeDocs.id）
     */
    @Override
    public void deleteSegmentsByDocId(Long docId) {
        if (docId == null) {
            log.error("删除文件分片失败：docId为空");
            return;
        }

        // 步骤1：先查询该文件的所有分片，获取vectorId列表
        List<Document> segments = getSegmentsByDocId(docId);
        if (CollectionUtils.isEmpty(segments)) {
            log.info("文件[{}]无分片可删除", docId);
            return;
        }

        // 步骤2：提取vectorId列表（Document的id属性即Qdrant的vector_id）
        List<String> vectorIds = segments.stream()
                .map(Document::getId)
                .collect(Collectors.toList());

        // 步骤3：批量删除向量库中的分片
        deleteSegmentsByVectorIds(vectorIds);
        log.info("文件[{}]级联删除完成：共删除{}个分片", docId, vectorIds.size());
    }

    /**
     * 删除：根据向量ID列表，删除指定分片
     * @param vectorIds Qdrant向量ID列表（Document.id列表）
     */
    @Override
    public void deleteSegmentsByVectorIds(List<String> vectorIds) {
        if (CollectionUtils.isEmpty(vectorIds)) {
            log.error("删除分片失败：vectorIds为空列表");
            return;
        }

        try {
            // 调用VectorStore批量删除（Spring AI 1.0.0标准方法）
            vectorStore.delete(vectorIds);
            log.info("批量删除分片完成：共删除{}个向量ID，IDs={}", vectorIds.size(), vectorIds);
        } catch (Exception e) {
            log.error("批量删除分片失败：vectorIds={}", vectorIds, e);
            throw new RuntimeException("删除向量分片失败", e);
        }
    }

    /**
     * 工具方法：生成文本的嵌入向量（复用你原有embed逻辑）
     * @param message 待生成向量的文本
     * @return 嵌入向量响应（含向量数组）
     */
    @Override
    public EmbeddingResponse generateEmbedding(String message) {
        if (message == null || message.trim().isEmpty()) {
            log.error("生成向量失败：文本为空");
            throw new IllegalArgumentException("待生成向量的文本不能为空");
        }
        log.info("生成文本向量：长度={}字符", message.length());
        return embeddingModel.embedForResponse(List.of(message));
    }
}