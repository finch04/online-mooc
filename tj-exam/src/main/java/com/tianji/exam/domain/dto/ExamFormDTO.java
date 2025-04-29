package com.tianji.exam.domain.dto;


import com.tianji.exam.enums.ExamType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "考试记录表单信息")
public class ExamFormDTO {
    @ApiModelProperty("课程id")
    private Long courseId;
    @ApiModelProperty("节id")
    private Long sectionId;
    @ApiModelProperty("类型，1-练习，2-考试")
    private ExamType type;
}
