package com.tianji.media.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "媒体上传的结果")
public class MediaUploadResultDTO {

    @Schema(description = "文件在云端的唯一标示", example = "387702302659783576")
    private String fileId;

    /*
    @Schema(description = "媒体播放地址", example = "http://xxx.mp4")
    private String mediaUrl;

    @Schema(description = "媒体封面地址", example = "http://xxx.jpg")
    private String coverUrl;

    @Schema(description = "文件名称", example = "Redis最佳实践.mp4")
    // TODO 限制文件名长度
    private String filename;
    */
}
