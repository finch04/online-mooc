package com.tianji.media.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "视频播放的签名信息")
public class VideoPlayVO {
    @Schema(description = "视频唯一标示", example = "12412534535143242")
    private String fileId;
    @Schema(description = "视频封面", example = "xxx.xxx.xxx")
    private String signature;
}
