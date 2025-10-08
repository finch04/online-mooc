package com.tianji.aigc.service;

import com.tianji.aigc.domain.dto.KnowledgeDocsDTO;
import com.tianji.aigc.domain.vo.KnowledgeDocsDetailVO;
import com.tianji.aigc.domain.vo.KnowledgeDocsVO;
import com.tianji.common.domain.query.PageQuery;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KnowledgeDocsService {

    /**
     * 新增知识文件
     * @param file
     * @param dto
     */
    void uploadKnowledgeDoc(MultipartFile file, KnowledgeDocsDTO dto);
    /**
     * 分页查询所有知识文件
     * * @param query
     * * @return 知识文件列表VO
     */
    List<KnowledgeDocsVO> queryDocsPage(PageQuery query);

    /**
     * 根据文档ID查询详情
     * @param docId 文档ID
     * @return 文档详情VO
     */
    KnowledgeDocsDetailVO queryDocDetailById(Long docId);

    /**
     * 根据文档ID删除文档
     * @param docId 文档ID
     * @return 是否删除成功
     */
    void deleteDocById(Long docId);

}