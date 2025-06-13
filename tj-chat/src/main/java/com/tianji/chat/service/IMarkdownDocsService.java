package com.tianji.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.chat.domain.po.MarkdownDocs;
import com.tianji.chat.domain.vo.MarkdownChunk;
import com.tianji.chat.domain.vo.MarkdownChunkVO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 用户上传的 Markdown 文档表 服务类
 * </p>
 *
 * */
public interface IMarkdownDocsService extends IService<MarkdownDocs> {

    MarkdownDocs upload(MultipartFile file, Integer level);

    String getMarkdown(Long fileId);

    PageDTO<MarkdownDocs> queryMarkdownPage(PageQuery query);

    List<MarkdownChunkVO> chatByMarkdownDoc(String message);

    void updateMarkdown(MarkdownDocs markdownDocs);

    void removeMarkdown(Long fileId);

}
