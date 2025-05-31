package com.tianji.learning.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 课程评价视图对象
 */
@Data
@ApiModel("课程评价信息")
public class EvaluationVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("评价ID")
    private Long id;

    @ApiModelProperty("课程ID")
    private Long courseId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("评价者昵称")
    private String userName;

    @ApiModelProperty("评价者头像")
    private String userIcon;

    @ApiModelProperty("教师ID")
    private Long teacherId;

    @ApiModelProperty("教师名称")
    private String teacherName;

//    @ApiModelProperty("教师头像")
//    private String teacherIcon;

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

    @ApiModelProperty("评价内容")
    private String comment;

    @ApiModelProperty("是否匿名")
    private Boolean anonymous;

    @ApiModelProperty("是否点击有用")
    private Boolean isHelpful;

    @ApiModelProperty("有用次数")
    private Integer helpCount;

    @ApiModelProperty("是否隐藏")
    private Boolean hidden;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private Date createTime;
}    