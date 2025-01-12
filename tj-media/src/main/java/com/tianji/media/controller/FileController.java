package com.tianji.media.controller;

import com.tianji.media.domain.dto.FileDTO;
import com.tianji.media.service.IFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件表，可以是普通文件、图片等 前端控制器
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "媒资管理相关接口")
public class FileController {

    private final IFileService fileService;

    @Operation(summary = "上传文件")
    @PostMapping
    public FileDTO uploadFile(
            @Parameter(description = "文件数据") @RequestParam("file") MultipartFile file) {
        return fileService.uploadFile(file);
    }

    @Operation(summary = "获取文件信息")
    @GetMapping("/{id}")
    public FileDTO getFileInfo(
            @Parameter(description = "文件id", example = "1") @PathVariable("id") Long id) {
        return fileService.getFileInfo(id);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{id}")
    public void deleteFileById(
            @Parameter(description = "文件id", example = "1") @PathVariable("id") Long id) {
        fileService.removeById(id);
    }
}
