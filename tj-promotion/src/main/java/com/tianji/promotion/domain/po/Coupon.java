package com.tianji.promotion.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.tianji.promotion.enums.CouponStatus;
import com.tianji.promotion.enums.DiscountType;
import com.tianji.promotion.enums.ObtainType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 优惠券的规则信息
 * </p>
 *
 * @author fsq
 * @since 2023-10-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("coupon")
@ApiModel(value="Coupon对象", description="优惠券的规则信息")
public class Coupon implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "优惠券id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "优惠券名称，可以和活动名称保持一致")
    private String name;

    @ApiModelProperty(value = "优惠券类型，1：普通券。目前就一种，保留字段")
    private Integer type;

    @ApiModelProperty(value = "折扣类型，1：每满减，2：折扣，3：无门槛，4：普通满减")
    private DiscountType discountType;

    @ApiModelProperty(value = "是否限定作用范围，false：不限定，true：限定。默认false")
    @TableField("`specific`")
    private Boolean specific;

    @ApiModelProperty(value = "折扣值，如果是满减则存满减金额，如果是折扣，则存折扣率，8折就是存80")
    private Integer discountValue;

    @ApiModelProperty(value = "使用门槛，0：表示无门槛，其他值：最低消费金额")
    private Integer thresholdAmount;

    @ApiModelProperty(value = "最高优惠金额，满减最大，0：表示没有限制，不为0，则表示该券有金额的限制")
    private Integer maxDiscountAmount;

    @ApiModelProperty(value = "获取方式：1：手动领取，2：兑换码")
    private ObtainType obtainWay;

    @ApiModelProperty(value = "开始发放时间")
    private LocalDateTime issueBeginTime;

    @ApiModelProperty(value = "结束发放时间")
    private LocalDateTime issueEndTime;

    @ApiModelProperty(value = "优惠券有效期天数，0：表示有效期是指定有效期的")
    private Integer termDays;

    @ApiModelProperty(value = "优惠券有效期开始时间")
    private LocalDateTime termBeginTime;

    @ApiModelProperty(value = "优惠券有效期结束时间")
    private LocalDateTime termEndTime;

    @ApiModelProperty(value = "优惠券配置状态，1：待发放，2：未开始   3：进行中，4：已结束，5：暂停")
    private CouponStatus status;

    @ApiModelProperty(value = "总数量，不超过5000")
    private Integer totalNum;

    @ApiModelProperty(value = "已发行数量，用于判断是否超发")
    private Integer issueNum;

    @ApiModelProperty(value = "已使用数量")
    private Integer usedNum;

    @ApiModelProperty(value = "每个人限领的数量，默认1")
    private Integer userLimit;

    @ApiModelProperty(value = "拓展参数字段，保留字段")
    private String extParam;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建人")
    private Long creater;

    @ApiModelProperty(value = "更新人")
    private Long updater;


}
