package com.tianji.aigc.service.impl;

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

    /**
     * 增加：保存文件分片到向量库（含Markdown分片、向量生成、元数据关联）
     * @param doc 原始文件（KnowledgeDocs，含ID、内容、用户ID等）
     */
    @Override
    public void saveSegments(KnowledgeDocs doc) {
        // 校验入参
        if (doc == null || doc.getId() == null || doc.getContent() == null) {
            log.error("保存文件分片失败：入参无效（doc={}）", doc);
            throw new IllegalArgumentException("文件信息或内容不能为空");
        }

        String markdownContent = doc.getContent();
        Integer splitLevel = doc.getLevel(); // Markdown分片层级（如1=按H1，2=按H2）

        // 2. Markdown分片处理：优先按指定层级分片，无则默认按标题智能分片
        List<MarkdownChunk> markdownChunks = new ArrayList<>();
        if (splitLevel != null && splitLevel >= 1 && splitLevel <= 6) {
            markdownChunks = MarkdownSplitter.getMarkdownChunksByH(markdownContent, splitLevel);
            log.info("文档[{}]按H{}层级分片，得到{}个片段", doc.getId(), splitLevel, markdownChunks.size());
        }
        // 若指定层级无结果，使用默认智能分片（按所有标题拆分）
        if (CollectionUtils.isEmpty(markdownChunks)) {
            markdownChunks = MarkdownSplitter.smartSplitByHeading(markdownContent);
            log.info("文档[{}]使用默认智能分片，得到{}个片段", doc.getId(), markdownChunks.size());
        }
        // 若仍无分片（如纯文本），直接作为1个片段处理
        if (CollectionUtils.isEmpty(markdownChunks)) {
            markdownChunks.add(new MarkdownChunk("全文", markdownContent));
            log.info("文档[{}]无标题，作为1个全文片段处理", doc.getId());
        }

        // 3. 转换为Spring AI Document（含元数据，便于后续查询过滤）
        List<Document> documents = markdownChunks.stream()
                .map(chunk -> new Document(
                        chunk.getContent(), // 分片文本内容
                        Map.of(
                                "doc_id", doc.getId().toString(), // 关联原始文件ID（关键：用于按文件查询）
                                "user_id", doc.getUserId().toString(), // 关联上传用户ID
                                "title", chunk.getTitle(), // 分片标题（如H1标题）
                                "create_time", new Date().toString()// 创建时间
                        )
                ))
                .collect(Collectors.toList());

        // 4. 保存到Qdrant向量库（Spring AI自动生成向量并写入）
        vectorStore.add(documents);
        log.info("文档[{}]分片保存完成：向量库新增{}个片段",
                doc.getId(), documents.size());
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