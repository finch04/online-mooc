package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 学习记录表
 * </p>
 *
 * @author fsq
 * @since 2023-10-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("learning_record")
@ApiModel(value="LearningRecord对象", description="学习记录表")
public class LearningRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "学习记录的id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "对应课表的id")
    private Long lessonId;

    @ApiModelProperty(value = "对应小节的id")
    private Long sectionId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "视频的当前观看时间点，单位秒")
    private Integer moment;

    @ApiModelProperty(value = "是否完成学习，默认false")
    private Boolean finished;

    @ApiModelProperty(value = "第一次观看时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "完成学习的时间")
    private LocalDateTime finishTime;

    @ApiModelProperty(value = "更新时间（最近一次观看时间）")
    private LocalDateTime updateTime;


}
