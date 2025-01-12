package com.tianji.media.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "视频文件信息")
public class MediaVO {
    @Schema(description = "id", example = "1")
    private Long id;
    @Schema(description = "文件名称", example = "文件名.avi")
    private String filename;
    @Schema(description = "视频封面", example = "default-cover-url.jpg")
    private String coverUrl;
    @Schema(description = "视频时长，单位秒", example = "57.23")
    private Float duration;
    @Schema(description = "视频大小，单位字节", example = "1024")
    private Long size;
    @Schema(description = "被引用次数", example = "10")
    private Integer useTimes;
    @Schema(description = "视频状态：1-上传中，2-已上传，3-处理中", example = "2")
    private Integer status;
    @Schema(description = "创建时间", example = "2022-7-18 16:54:30")
    private LocalDateTime createTime;
    @Schema(description = "创建者名称", example = "张三")
    private String creater;
}
