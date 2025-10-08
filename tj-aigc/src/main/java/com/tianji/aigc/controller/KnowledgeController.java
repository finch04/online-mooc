package com.tianji.aigc.controller;

import com.tianji.aigc.domain.dto.KnowledgeDocsDTO;
import com.tianji.aigc.domain.vo.KnowledgeDocsDetailVO;
import com.tianji.aigc.domain.vo.KnowledgeDocsVO;
import com.tianji.aigc.service.KnowledgeDocsService;
import com.tianji.common.domain.query.PageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/kb")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeDocsService knowledgeDocsService;

    /**
     * 新增知识文件
     */
    //注意：@RequestBody 与 MultipartFile 无法共存
    // @RequestBody 要求请求体是 JSON/XML 等 “纯数据格式”，而 multipart/form-data 是 “表单 + 文件” 格式
    // Spring 无法同时解析这两种格式，导致抛出 Content-Type 不支持 异常。
    @PostMapping("/upload")
    public void uploadKnowledgeDoc(@RequestParam("file") MultipartFile file,
                                   @ModelAttribute KnowledgeDocsDTO dto) {
        knowledgeDocsService.uploadKnowledgeDoc(file,dto);
    }

    /**
     * 分页查询所有知识文件
     */
    @GetMapping("/docs")
    public List<KnowledgeDocsVO> queryDocsPage(PageQuery query) {
        List<KnowledgeDocsVO> docs = knowledgeDocsService.queryDocsPage(query);
        return docs;
    }

    /**
     * 根据文档ID查询详情
     */
    @GetMapping("/docs/{id}")
    public KnowledgeDocsDetailVO queryDocDetailById(@PathVariable("id") Long docId) {
        KnowledgeDocsDetailVO detailVO = knowledgeDocsService.queryDocDetailById(docId);
        return detailVO;
    }

    /**
     * 根据文档ID删除文档
     */
    @DeleteMapping("/docs/{id}")
    public void deleteDocById(@PathVariable("id") Long docId) {
        knowledgeDocsService.deleteDocById(docId);
    }
}
