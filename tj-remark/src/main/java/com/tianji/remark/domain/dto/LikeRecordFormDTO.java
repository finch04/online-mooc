package com.tianji.remark.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = "点赞记录表单实体")
public class LikeRecordFormDTO {
    @Schema(description = "点赞业务id")
    @NotNull(message = "业务id不能为空")
    private Long bizId;

    @Schema(description = "点赞业务类型")
    @NotNull(message = "业务类型不能为空")
    private String bizType;

    @Schema(description = "是否点赞，true：点赞；false：取消点赞")
    @NotNull(message = "是否点赞不能为空")
    private Boolean liked;
}
