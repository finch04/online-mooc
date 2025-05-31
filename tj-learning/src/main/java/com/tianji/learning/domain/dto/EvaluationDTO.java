package com.tianji.learning.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * 课程评价数据传输对象
 */
@Data
@ApiModel(description = "课程评价表单实体")
public class EvaluationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("评价id")
    private Long id;

    private Long courseId;

    private Long teacherId;

    private Integer contentRating;

    private Integer teachingRating;

    private Integer difficultyRating;

    private Integer valueRating;

    private String comment;

    private Boolean anonymity;
}    