package com.tianji.chat.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.chat.domain.po.MarkdownDocs;
import com.tianji.chat.domain.vo.MarkdownChunk;
import com.tianji.chat.mapper.MarkdownDocsMapper;
import com.tianji.chat.service.IMarkdownDocsService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.UserContext;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.tianji.chat.constants.AiConstants.QDRANT_COLLECTION;
import static com.tianji.chat.utils.MarkdownSplitter.getMarkdownChunksByH;
import static com.tianji.chat.utils.MarkdownSplitter.smartSplitByHeading;
import static io.qdrant.client.ConditionFactory.matchKeyword;

/**
 * <p>
 * 用户上传的 Markdown 文档表 服务实现类
 * </p>
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class MarkdownDocsServiceImpl extends ServiceImpl<MarkdownDocsMapper, MarkdownDocs> implements IMarkdownDocsService {
    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private final EmbeddingStore<TextSegment> embeddingStore;
    @Autowired
    private final QdrantClient qdrantClient;

    @Override
    public MarkdownDocs upload(MultipartFile file, Integer level) {
        // 判断是否为md文件
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".md")) {
           throw new BadRequestException("只支持上传 .md 格式的文件") ;
        }
        //文件如果大于2MB拒绝上传
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BadRequestException("文件大小不能超过2MB");
        }

        try {
            // 将 MultipartFile 转换为字符串（使用 UTF-8 编码）
            StringBuilder contentBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    contentBuilder.append(line).append("\n");
                }
            }

            String markdownContent = contentBuilder.toString();
            long userId = UserContext.getUser();

            // markdownDocsMapper.insert(...)
            MarkdownDocs markdownDocs = new MarkdownDocs();
            markdownDocs.setUserId(userId);
            markdownDocs.setFileName(originalFilename);
            markdownDocs.setContent(markdownContent);
            markdownDocs.setLevel(level);
            markdownDocs.setCreateTime(LocalDateTime.now());
            markdownDocs.setUpdateTime(LocalDateTime.now());


            this.save(markdownDocs);
            // 此处可以将内容保存到数据库或进行向量分割
            saveSegment(markdownDocs, userId, level, markdownContent);
            return markdownDocs;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件解析失败：" + e.getMessage());
        }

    }

    @Override
    public PageDTO<MarkdownDocs> queryMarkdownPage(PageQuery query) {
        Page<MarkdownDocs> page = this.lambdaQuery()
                .eq(MarkdownDocs::getUserId, UserContext.getUser())
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        page.getRecords().stream().forEach(i->i.setContent(null));
        return PageDTO.of(page);
    }

    @Override
    public MarkdownChunk chatByMarkdownDoc(String message) {
        //TODO 每个用户只能看到自己上传知识库的内容并总结
        dev.langchain4j.data.embedding.Embedding queryEmbedding = embeddingModel.embed(message).content();
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(queryEmbedding, 5 );

        // 6. 验证结果
        Assert.notEmpty(matches, "搜索结果不应为空");

        // 打印结果
        log.info("\n===== 问题：{} =====",message);
        log.info("\n===== 相似度搜索结果 =====");
        for (EmbeddingMatch<TextSegment> match : matches) {
            log.info("相似度: {}\n内容: {}\n",
                    match.score(), match.embedded().text());
        }
        String text = matches.get(0).embedded().text();
        return  MarkdownChunk.fromString(text);
    }

    @Override
    public String getMarkdown(Long fileId) {
        long userId =UserContext.getUser();

        MarkdownDocs markdownDocs = lambdaQuery()
                .eq(MarkdownDocs::getId, fileId)
                .eq(MarkdownDocs::getUserId, userId)
                .one();

        if (ObjectUtil.isEmpty(markdownDocs)) {
            throw new RuntimeException("文件不存在");
        }

        return markdownDocs.getContent();
    }

    @Override
    public void updateMarkdown(MarkdownDocs markdownDocs) {
        // 获取用户id
        Long userId = UserContext.getUser();
        // 判断前端是否传送了文件id
        Long id = markdownDocs.getId();
        if (ObjectUtil.isEmpty(id)) {
            throw new RuntimeException("文件id不能为空");
        }

        MarkdownDocs docs = lambdaQuery()
                .eq(MarkdownDocs::getId, id)
                .eq(MarkdownDocs::getUserId, userId)
                .oneOpt()
                .orElseThrow(() -> new RuntimeException("文件不存在"));

        // 获取文档切割等级, 更新数据库文档
        int level = docs.getLevel();
        //这里应该传前端传入的内容
        String markdownContent = markdownDocs.getContent();
        docs.setContent(markdownContent);
        updateById(docs);

        deleteSegment(docs);

        saveSegment(markdownDocs, userId, level, markdownContent);

        log.info("更新成功");
    }

    @Override
    public void removeMarkdown(Long fileId) {
        // 获取当前用户id
        Long userId =UserContext.getUser();
        // 查看文档
        MarkdownDocs docs = lambdaQuery()
                .eq(MarkdownDocs::getId, fileId)
                .eq(MarkdownDocs::getUserId, userId)
                .oneOpt()
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        deleteSegment(docs);
        // 删除文档
        if(!removeById(docs.getId())){
            throw new BadRequestException("删除失败");
        }
    }



    private void saveSegment(MarkdownDocs markdownDocs, Long userId, int level, String markdownContent) {
        List<MarkdownChunk> markdownChunks = getMarkdownChunksByH(markdownContent, level);
        if(markdownChunks.size()==0){
            smartSplitByHeading(markdownContent);
        }
        for (MarkdownChunk markdownChunk : markdownChunks) {
            TextSegment segment = TextSegment.from(markdownChunk.toString());
            segment.metadata().put("user_id", userId);
            segment.metadata().put("doc_id", markdownDocs.getId());
            Embedding embedding = embeddingModel.embed(segment).content();
            String add = embeddingStore.add(embedding, segment);
            log.info("添加成功:{}",add);
        }
    }

    public void deleteSegment(MarkdownDocs docs) {
        try {
            // 检查集合是否存在
            Collections.CollectionInfo collectionInfo = qdrantClient.getCollectionInfoAsync(QDRANT_COLLECTION).get();
            if (collectionInfo!=null) {
                List<Points.Condition> conditions = new ArrayList<>();
                //必须这两个都匹配才可以！
//                conditions.add(matchKeyword("user_id", docs.getUserId().toString()));
                conditions.add(matchKeyword("doc_id", docs.getId().toString()));

                Points.UpdateResult updateResult = qdrantClient.deleteAsync(QDRANT_COLLECTION,
                        Points.Filter.newBuilder()
                                .addAllMust(conditions)
                                .build()).get();
                System.out.println("成功删除文档: " + updateResult);
            } else {
                throw new BadRequestException("集合不存在: ");
            }
        } catch (Exception e) {
            throw new BadRequestException("删除文档失败: {}" + e.getMessage());
        }
    }
}
