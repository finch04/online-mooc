package com.tianji.exam.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(description = "教师评语")
public class ExamDetailCommentDTO {

    @ApiModelProperty("考试明细id")
    private Long id;
    @ApiModelProperty("教师评语")
    private String comment;
}
