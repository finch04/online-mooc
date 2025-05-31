package com.tianji.learning.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@ApiModel(description = "课程收藏表信息")
public class LessonCollectVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键Id")
    private Long id;

    @ApiModelProperty("课程id")
    private Long courseId;

    @ApiModelProperty("课程名称")
    private String courseName;

    @ApiModelProperty("课程封面")
    private String courseCoverUrl;

    @ApiModelProperty("课程章节数量")
    private Integer sections;

    @ApiModelProperty("收藏时间")
    private LocalDateTime createTime;
}    