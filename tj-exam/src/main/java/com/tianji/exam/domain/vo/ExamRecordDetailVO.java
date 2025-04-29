package com.tianji.exam.domain.vo;

import com.tianji.api.dto.exam.QuestionDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 考试记录表
 * </p>
 *
 * @author 虎哥
 * @since 2022-07-18
 */
@Data
@ApiModel(description = "考试记录详情")
public class ExamRecordDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("详情id")
    private Long id;

    @ApiModelProperty("考生答案，选择题：答案0~9对应A~J；判断题：0和1分别是错和对；文件则保存文件地址")
    private String answer;

    @ApiModelProperty("老师评语")
    private String comment;

    @ApiModelProperty("考试记录id")
    private Long examId;

    @ApiModelProperty("是否正确")
    private Boolean correct;

    @ApiModelProperty("本题得分")
    private Integer score;

    @ApiModelProperty("考生id")
    private Long userId;

    private QuestionDTO question;
}
