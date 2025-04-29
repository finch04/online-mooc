package com.tianji.exam.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(description = "考试提交的答案信息")
public class ExamCommitDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("考试记录id，考试开始时")
    private Long id;
    private List<ExamDetailDTO> examDetails;
}
