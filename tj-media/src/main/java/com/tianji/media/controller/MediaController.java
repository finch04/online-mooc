package com.tianji.media.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.media.domain.dto.MediaDTO;
import com.tianji.media.domain.dto.MediaUploadResultDTO;
import com.tianji.media.domain.query.MediaQuery;
import com.tianji.media.domain.vo.MediaVO;
import com.tianji.media.domain.vo.VideoPlayVO;
import com.tianji.media.service.IMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 媒资表，主要是视频文件 前端控制器
 */
@RestController
@RequestMapping("/medias")
@RequiredArgsConstructor
@Tag(name = "媒资管理相关接口")
public class MediaController {

    private final IMediaService mediaService;

    @Operation(summary = "分页搜索已上传媒资信息")
    @GetMapping
    public PageDTO<MediaVO> queryMediaPage(MediaQuery query) {
        return mediaService.queryMediaPage(query);
    }

    @Operation(summary = "上传视频后保存媒资信息")
    @PostMapping
    public MediaDTO saveMedia(@RequestBody MediaUploadResultDTO result) {
        return mediaService.save(result);
    }

    @Operation(summary = "获取上传视频的授权签名")
    @GetMapping("/signature/upload")
    public String getUploadSignature() {
        return mediaService.getUploadSignature();
    }

    @Operation(summary = "获取播放视频的授权签名")
    @GetMapping("/signature/play")
    public VideoPlayVO getPlaySignature(
            @Parameter(description = "小节id", example = "1", required = true) @RequestParam("sectionId") Long sectionId) {
        return mediaService.getPlaySignatureBySectionId(sectionId);
    }

    @Operation(summary = "管理端获取预览视频的授权签名")
    @GetMapping("/signature/preview")
    public VideoPlayVO getPreviewSignature(
            @Parameter(description = "媒资id", example = "1", required = true) @RequestParam("mediaId") Long mediaId) {
        return mediaService.getPlaySignatureByMediaId(mediaId);
    }

    @Operation(summary = "删除媒资视频")
    @DeleteMapping("{mediaId}")
    public void deleteMedia(
            @Parameter(description = "媒资id", example = "1", required = true) @PathVariable("mediaId") Long mediaId) {
        mediaService.removeById(mediaId);
    }

    @Operation(summary = "批量删除媒资视频")
    @DeleteMapping
    public void deleteMedias(
            @Parameter(description = "媒资id集合，例如1,2,3", required = true) @RequestParam("ids") List<Long> mediaIds) {
        mediaService.removeByIds(mediaIds);
    }
}
