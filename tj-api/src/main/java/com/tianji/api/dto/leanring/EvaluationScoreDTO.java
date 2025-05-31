package com.tianji.api.dto.leanring;/**
 * @author fsq
 * @date 2025/5/22 16:32
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/5/22 16:32
 * @Version: 1.0
 */

@Data
@ApiModel(description = "课程各维度评分信息")
public class EvaluationScoreDTO {
    @ApiModelProperty("课程id")
    private Long courseId;

    @ApiModelProperty("内容评分")
    private Integer contentRating;

    @ApiModelProperty("教学评分")
    private Integer teachingRating;

    @ApiModelProperty("难度评分")
    private Integer difficultyRating;

    @ApiModelProperty("价值评分")
    private Integer valueRating;

    @ApiModelProperty("综合评分")
    private Double overallRating;

    @ApiModelProperty("评价数量")
    private Integer commentCount;
}
