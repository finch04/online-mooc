package com.tianji.chat.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.chat.domain.po.MarkdownDocs;
import com.tianji.chat.domain.vo.MarkdownChunk;
import com.tianji.chat.domain.vo.MarkdownChunkVO;
import com.tianji.chat.mapper.MarkdownDocsMapper;
import com.tianji.chat.service.IMarkdownDocsService;
import com.tianji.chat.utils.QdrantEmbeddingUtils;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.UserContext;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.WithVectorsSelectorFactory;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.tianji.chat.constants.AiConstants.QDRANT_COLLECTION;
import static com.tianji.chat.utils.MarkdownSplitter.getMarkdownChunksByH;
import static com.tianji.chat.utils.MarkdownSplitter.smartSplitByHeading;
import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;

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

    @Transactional
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
            throw new BadRequestException("文件解析失败：" + e.getMessage());
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
    public List<MarkdownChunkVO> chatByMarkdownDoc(String message) {
        // 获取当前用户ID
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        try {
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


            // 3. 验证结果
            if (matches.isEmpty()) {
                throw new BadRequestException("当前您知识库似乎还没有相关内容，快去上传吧~");
            }
            // 4. 打印结果用于调试
            log.info("\n===== 问题：{} =====", message);
            log.info("\n===== 相似度搜索结果 =====");
            for (EmbeddingMatch<TextSegment> match : matches) {
                System.out.println("匹配得分: " + match.score());
                System.out.println("匹配内容:\n" + match.embedded().text());
            }


            // 3. 拼接 context 参考材料
            List<MarkdownChunkVO> vos = matches.stream()
                    .map(match -> {
                        MarkdownChunkVO vo = new MarkdownChunkVO();
                        vo.setScore(match.score());
                        MarkdownChunk markdownChunk = MarkdownChunkVO.fromString(match.embedded().text());
                        vo.setContent(markdownChunk.content);
                        vo.setTitle(markdownChunk.title);
                        return vo;
                    })
                    .collect(Collectors.toList());

            return vos;
        } catch (Exception e) {
            log.error("处理用户问题时发生错误", e);
            throw new RuntimeException("处理用户问题时发生错误", e);
        }
    }



    @Override
    public String getMarkdown(Long fileId) {
        long userId =UserContext.getUser();

        MarkdownDocs markdownDocs = lambdaQuery()
                .eq(MarkdownDocs::getId, fileId)
                .eq(MarkdownDocs::getUserId, userId)
                .one();

        if (ObjectUtil.isEmpty(markdownDocs)) {
            throw new BadRequestException("文件不存在");
        }

        return markdownDocs.getContent();
    }

    @Transactional
    @Override
    public void updateMarkdown(MarkdownDocs markdownDocs) {
        // 获取用户id
        Long userId = UserContext.getUser();
        // 判断前端是否传送了文件id
        Long id = markdownDocs.getId();
        if (ObjectUtil.isEmpty(id)) {
            throw new BadRequestException("文件id不能为空");
        }

        MarkdownDocs docs = lambdaQuery()
                .eq(MarkdownDocs::getId, id)
                .eq(MarkdownDocs::getUserId, userId)
                .oneOpt()
                .orElseThrow(() -> new BadRequestException("文件不存在"));

        // 获取文档切割等级, 更新数据库文档
        int level = markdownDocs.getLevel();
        //这里应该传前端传入的内容
        String markdownContent = markdownDocs.getContent();
        docs.setContent(markdownContent);
        docs.setLevel(level);
        updateById(docs);

        deleteSegment(docs);

        saveSegment(markdownDocs, userId, level, markdownContent);

        log.info("更新成功");
    }
    @Transactional
    @Override
    public void removeMarkdown(Long fileId) {
        // 获取当前用户id
        Long userId =UserContext.getUser();
        // 查看文档
        MarkdownDocs docs = lambdaQuery()
                .eq(MarkdownDocs::getId, fileId)
                .eq(MarkdownDocs::getUserId, userId)
                .oneOpt()
                .orElseThrow(() -> new BadRequestException("文件不存在"));
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
            segment.metadata().put("doc_id", markdownDocs.getId().toString());
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
