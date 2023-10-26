package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author fsq
 * @since 2023-10-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("points_board_season")
@ApiModel(value="PointsBoardSeason对象", description="")
public class PointsBoardSeason implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增长id，season标示")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "赛季名称，例如：第1赛季")
    private String name;

    @ApiModelProperty(value = "赛季开始时间")
    private LocalDate beginTime;

    @ApiModelProperty(value = "赛季结束时间")
    private LocalDate endTime;


}
