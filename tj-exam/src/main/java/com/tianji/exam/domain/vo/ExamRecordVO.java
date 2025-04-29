package com.tianji.exam.domain.vo;

import com.tianji.exam.enums.ExamType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 考试记录表
 * </p>
 *
 * @author 虎哥
 * @since 2022-07-18
 */
@Data
@ApiModel(description = "考试记录")
public class ExamRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("考试记录id")
    private Long id;

    @ApiModelProperty("课程名称")
    private String courseName;

    @ApiModelProperty("节名称")
    private String sectionName;

    @ApiModelProperty("类型，1-练习，2-考试")
    private ExamType type;

    @ApiModelProperty("分数")
    private Integer score;

    @ApiModelProperty("试用时，如果中间暂停，先记录暂停时的用时，单位秒")
    private Integer duration;

    @ApiModelProperty("提交时间")
    private LocalDateTime finishTime;
}
