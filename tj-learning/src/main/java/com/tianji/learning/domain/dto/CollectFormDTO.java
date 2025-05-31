package com.tianji.learning.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "收藏记录表单实体")
public class CollectFormDTO {
    @ApiModelProperty("收藏的课程id")
    @NotNull(message = "课程id不能为空")
    private Long courseId;

    @ApiModelProperty("是否收藏，true：收藏；false：取消收藏")
    @NotNull(message = "是否收藏不能为空")
    private Boolean collected;
}
