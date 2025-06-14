package com.tianji.api.dto.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName TodayDataVO
 * @Author wusongsong
 * @Date 2022/10/13 9:23
 * @Version
 **/
@Data
public class OrderDataVO {
    @ApiModelProperty("累计订单金额，万元单位")
    private Double allOrderAmount;
    @ApiModelProperty("待支付金额,万元单位")
    private Double noPayAmount;
    @ApiModelProperty("已关闭金额，万元单位")
    private Double closeAmount;
    @ApiModelProperty("已退款金额，万元单位")
    private Double refundAmount;
    @ApiModelProperty("实收金额，万元单位")
    private Double realAmount;
}
