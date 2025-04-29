package com.tianji.promotion.domain.po;

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
 * 促销活动，形式多种多样，例如：优惠券
 * </p>
 *
 * @author fsq
 * @since 2023-10-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("promotion")
@ApiModel(value="Promotion对象", description="促销活动，形式多种多样，例如：优惠券")
public class  Promotion implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "促销活动id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "活动名称")
    private String name;

    @ApiModelProperty(value = "促销活动类型：1-优惠券，2-分销")
    private Integer type;

    @ApiModelProperty(value = "是否是热门活动：true或false，默认false")
    private Integer hot;

    @ApiModelProperty(value = "活动开始时间")
    private LocalDateTime beginTime;

    @ApiModelProperty(value = "活动结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建人")
    private Long creater;

    @ApiModelProperty(value = "更新人")
    private Long updater;


}
