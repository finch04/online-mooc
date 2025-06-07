//package com.tianji.chat.service.impl;
//
//import cn.hutool.core.util.ObjectUtil;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.tianji.chat.domain.dto.MarkdownDocs;
//import com.tianji.chat.domain.po.MarkdownChunk;
//import com.tianji.chat.mapper.MarkdownDocsMapper;
//import com.tianji.chat.service.IMarkdownDocsService;
//import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
//import dev.langchain4j.data.embedding.Embedding;
//import dev.langchain4j.data.segment.TextSegment;
//import dev.langchain4j.store.embedding.EmbeddingStore;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static com.tianji.chat.utils.MarkdownSplitter.getMarkdownChunksByH;
//import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;
//
///**
// * <p>
// * 用户上传的 Markdown 文档表 服务实现类
// * </p>
// *
// * @author lusy
// * @since 2025-05-07
// */
//@RequiredArgsConstructor
//@Service
//public class MarkdownDocsServiceImpl extends ServiceImpl<MarkdownDocsMapper, MarkdownDocs> implements IMarkdownDocsService {
//
//    private final QwenEmbeddingModel embeddingModel;
//
//    private final EmbeddingStore<TextSegment> embeddingStore;
//    @Override
//    public String upload(MultipartFile file, Integer level) {
//
//        // 判断是否为md文件
//        String originalFilename = file.getOriginalFilename();
//        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".md")) {
//            return "只支持上传 .md 格式的文件";
//        }
//
//        try {
//            // 将 MultipartFile 转换为字符串（使用 UTF-8 编码）
//            StringBuilder contentBuilder = new StringBuilder();
//            try (BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    contentBuilder.append(line).append("\n");
//                }
//            }
//
//            String markdownContent = contentBuilder.toString();
//            long userId = 37L;
//
//            // markdownDocsMapper.insert(...)
//            MarkdownDocs markdownDocs = new MarkdownDocs();
//            markdownDocs.setUserId(userId);
//            markdownDocs.setFileName(originalFilename);
//            markdownDocs.setContent(markdownContent);
//            markdownDocs.setLevel(level);
//            markdownDocs.setCreatedAt(LocalDateTime.now());
//            markdownDocs.setUpdatedAt(LocalDateTime.now());
//
//            save(markdownDocs);
//            // 此处可以将内容保存到数据库或进行向量分割
//            saveSegment(markdownDocs, userId, level, markdownContent);
//
//            return "上传成功，字数：" + markdownContent.length();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "文件解析失败：" + e.getMessage();
//        }
//
//    }
//
//    @Override
//    public String getMarkdown(Long fileId) {
//
//        long userId = 37L;
//
//        MarkdownDocs markdownDocs = lambdaQuery()
//                .eq(MarkdownDocs::getId, fileId)
//                .eq(MarkdownDocs::getUserId, userId)
//                .one();
//
//        if (ObjectUtil.isEmpty(markdownDocs)) {
//            throw new RuntimeException("文件不存在");
//        }
//
//        return markdownDocs.getContent();
//    }
//
//    @Override
//    public String updateMarkdown(MarkdownDocs markdownDocs) {
//
//        // 获取用户id
//        Long userId = 37L;
//        // 判断前端是否传送了文件id
//        Long id = markdownDocs.getId();
//        if (ObjectUtil.isEmpty(id)) {
//            throw new RuntimeException("文件id不能为空");
//        }
//
//        MarkdownDocs docs = lambdaQuery()
//                .eq(MarkdownDocs::getId, id)
//                .eq(MarkdownDocs::getUserId, userId)
//                .oneOpt()
//                .orElseThrow(() -> new RuntimeException("文件不存在"));
//
//        // 获取文档切割等级, 更新数据库文档
//        int level = docs.getLevel();
//        String markdownContent = docs.getContent();
//        updateById(docs);
//
//        // 删除 Qdrant 的旧数据
//        embeddingStore.removeAll(metadataKey("userId").isEqualTo(userId)
//                .and(metadataKey("docId").isEqualTo(markdownDocs.getId())));
//
//        saveSegment(markdownDocs, userId, level, markdownContent);
//
//        return "更新成功";
//    }
//
//    @Override
//    public String removeMarkdown(Long fileId) {
//
//        // 获取当前用户id
//        Long userId = 37L;
//        // 查看文档
//        MarkdownDocs docs = lambdaQuery()
//                .eq(MarkdownDocs::getId, fileId)
//                .eq(MarkdownDocs::getUserId, userId)
//                .oneOpt()
//                .orElseThrow(() -> new RuntimeException("文件不存在"));
//        // 删除 Qdrant 的旧数据
//        embeddingStore.removeAll(metadataKey("userId").isEqualTo(userId));
//                //.and(metadataKey("docId").isEqualTo(fileId)));
//        // 删除文档
//        return removeById(docs.getId()) ? "删除成功" :  "删除失败";
//
//    }
//
//    private void saveSegment(MarkdownDocs markdownDocs, Long userId, int level, String markdownContent) {
//        List<MarkdownChunk> markdownChunks = getMarkdownChunksByH(markdownContent, level);
//        for (MarkdownChunk markdownChunk : markdownChunks) {
//            TextSegment segment = TextSegment.from(markdownChunk.toString());
//            segment.metadata().put("userId", userId);
//            segment.metadata().put("docId", markdownDocs.getId());
//            Embedding embedding = embeddingModel.embed(segment).content();
//            embeddingStore.add(embedding, segment);
//        }
//    }
//}
