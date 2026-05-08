package com.tianji.media.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "视频播放的签名信息")
public class VideoPlayVO {
    @ApiModelProperty(value = "云点播 FileId，播放唯一标识", example = "12412534535143242")
    private String fileId;
    @ApiModelProperty(value = "播放器签名 psign（JWT）")
    private String signature;
    @ApiModelProperty(value = "云点播 AppId，对应 TCPlayer 的 appID（仅媒资存储为腾讯云时返回）", example = "1312394356")
    private Long appId;
}
