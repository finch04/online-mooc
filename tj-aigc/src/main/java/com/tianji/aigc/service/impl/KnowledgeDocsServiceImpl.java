package com.tianji.aigc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianji.aigc.domain.dto.KnowledgeDocsDTO;
import com.tianji.aigc.domain.po.KnowledgeDocs;
import com.tianji.aigc.domain.vo.KnowledgeDocsDetailVO;
import com.tianji.aigc.domain.vo.KnowledgeDocsVO;
import com.tianji.aigc.mapper.KnowledgeDocsMapper;
import com.tianji.aigc.service.KnowledgeDocsService;
import com.tianji.aigc.service.SegmentService;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeDocsServiceImpl  implements KnowledgeDocsService {

    private final KnowledgeDocsMapper knowledgeDocsMapper;
    private final SegmentService segmentService;

    @Override
    @Transactional
    public void uploadKnowledgeDoc(MultipartFile file, KnowledgeDocsDTO dto) {

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
            Long userId = UserContext.getUser();

            KnowledgeDocs doc = new KnowledgeDocs();
            BeanUtils.copyProperties(dto, doc);
            doc.setUserId(userId);
            doc.setStatus(1); // 默认为正常状态
            doc.setSegmentCount(0); // 初始片段数为0
            doc.setCreateTime(LocalDateTime.now());
            doc.setUpdateTime(LocalDateTime.now());

            knowledgeDocsMapper.insert(doc);
            segmentService.saveSegments(doc);
        }catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("文件解析失败：" + e.getMessage());
        }

    }

    @Override
    public List<KnowledgeDocsVO> queryDocsPage(PageQuery query) {
        LambdaQueryWrapper<KnowledgeDocs> queryWrapper = new LambdaQueryWrapper<>();
        Long userId = UserContext.getUser();
        queryWrapper.eq(KnowledgeDocs::getUserId, userId)
                    .eq(KnowledgeDocs::getStatus, 1) // 只查询正常状态的文档
                    .orderByDesc(KnowledgeDocs::getCreateTime);
        
        List<KnowledgeDocs> docs = knowledgeDocsMapper.selectList(queryWrapper);
        
        // 转换为VO
        return docs.stream().map(doc -> {
            KnowledgeDocsVO vo = new KnowledgeDocsVO();
            BeanUtils.copyProperties(doc, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public KnowledgeDocsDetailVO queryDocDetailById(Long docId) {
        Long userId = UserContext.getUser();
        
        LambdaQueryWrapper<KnowledgeDocs> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeDocs::getId, docId)
                    .eq(KnowledgeDocs::getUserId, userId)
                    .eq(KnowledgeDocs::getStatus, 1);
        
        KnowledgeDocs doc = knowledgeDocsMapper.selectOne(queryWrapper);
        if (doc == null) {
            return null; // 或抛出异常
        }
        
        KnowledgeDocsDetailVO detailVO = new KnowledgeDocsDetailVO();
        BeanUtils.copyProperties(doc, detailVO);
        return detailVO;
    }

    @Override
    public void deleteDocById(Long docId) {
        Long userId = UserContext.getUser();
        
        // 逻辑删除，更新状态为2（已删除）
        KnowledgeDocs doc = new KnowledgeDocs();
        doc.setId(docId);
        doc.setStatus(2);
        doc.setUpdateTime(LocalDateTime.now());
        
        LambdaQueryWrapper<KnowledgeDocs> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(KnowledgeDocs::getId, docId)
                    .eq(KnowledgeDocs::getUserId, userId);

        knowledgeDocsMapper.delete(updateWrapper);
    }

}