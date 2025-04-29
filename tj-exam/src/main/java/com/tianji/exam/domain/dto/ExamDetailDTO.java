package com.tianji.exam.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "考试提交的答案条目信息")
public class ExamDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("问题id")
    private Long questionId;

    @ApiModelProperty("问题类型，1：单选题，2：多选题，3：不定向选择题，4：判断题，5：主观题")
    private Integer questionType;

    @ApiModelProperty("考生答案，选择题：答案0~9对应A~J，多选以,隔开；判断题：0和1分别是错和对；文件则保存文件地址")
    private String answer;
}
