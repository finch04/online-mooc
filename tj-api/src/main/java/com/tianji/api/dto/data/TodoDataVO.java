package com.tianji.api.dto.data;/**
 * @author fsq
 * @date 2025/5/18 19:03
 */

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: fsq
 * @Date: 2025/5/18 19:03
 * @Version: 1.0
 */
@Data
public class TodoDataVO {

    @ApiModelProperty("待审批退款数")
    private Integer todoRefundNum;
    @ApiModelProperty("待发布优惠券数")
    private Integer todoCouponNum;
    
}
