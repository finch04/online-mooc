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
@Schema(description = "视频文件信息")
public class MediaDTO {
    @Schema(description = "数据库mediaId", example = "1")
    private Long id;

    @Schema(description = "文件名称", example = "Redis实战课.mp4")
    private String filename;

    @Schema(description = "视频时长，单位秒", example = "57.23")
    private Float duration;

    @Schema(description = "视频大小，单位字节", example = "1024")
    private Long size;
}
