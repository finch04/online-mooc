package com.tianji.chat.controller;

import com.tianji.chat.domain.po.MarkdownDocs;
import com.tianji.chat.domain.vo.MarkdownChunk;
import com.tianji.chat.domain.vo.MarkdownChunkVO;
import com.tianji.chat.service.IMarkdownDocsService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@Api(tags = "知识库接口")
public class MarkdownController {

    private final IMarkdownDocsService markdownDocsService;

    @ApiOperation("上传文件到知识库")
    @PostMapping("/upload")
    public MarkdownDocs uploadMarkdown(@RequestParam MultipartFile file,
                                            @RequestParam(defaultValue="2") Integer level) {
        return markdownDocsService.upload(file, level);
    }

    @ApiOperation("根据知识库内容对话")
    @GetMapping("/chat")
    public List<MarkdownChunkVO> chatByMarkdownDoc(@RequestParam String message) {
        return markdownDocsService.chatByMarkdownDoc(message);
    }

    @ApiOperation("分页查询用户知识库文件列表")
    @GetMapping("/page")
    public PageDTO<MarkdownDocs> queryMarkdownPage(PageQuery query) {
        return markdownDocsService.queryMarkdownPage(query);
    }


    @ApiOperation("根据文件id查看文件内容")
    @GetMapping("/{id}")
    public String getMarkdown(@PathVariable("id") Long fileId) {
        return markdownDocsService.getMarkdown(fileId);

    }

    @ApiOperation("更新文件内容")
    @PutMapping("/update")
    public void updateMarkdown(@RequestBody MarkdownDocs markdownDocs) {
         markdownDocsService.updateMarkdown(markdownDocs);
    }

    @ApiOperation("根据文件id删除文件")
    @DeleteMapping("/{id}")
    public void deleteMarkdown(@PathVariable("id") Long fileId) {
         markdownDocsService.removeMarkdown(fileId);
    }

}
