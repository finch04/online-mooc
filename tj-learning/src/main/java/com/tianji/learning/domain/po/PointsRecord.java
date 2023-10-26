package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.tianji.learning.enums.PointsRecordType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 学习积分记录，每个月底清零
 * </p>
 *
 * @author fsq
 * @since 2023-10-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("points_record")
@ApiModel(value="PointsRecord对象", description="学习积分记录，每个月底清零")
public class PointsRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "积分记录表id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "积分方式：1-课程学习，2-每日签到，3-课程问答， 4-课程笔记，5-课程评价")
    private PointsRecordType type;

    @ApiModelProperty(value = "积分值")
    private Integer points;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


}
