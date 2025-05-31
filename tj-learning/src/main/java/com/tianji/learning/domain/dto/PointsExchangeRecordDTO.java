package com.tianji.learning.domain.dto;/**
 * @author fsq
 * @date 2025/5/25 18:54
 */

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @Author: fsq
 * @Date: 2025/5/25 18:54
 * @Version: 1.0
 */
@Data
@ApiModel(description = "兑换信息")
public class PointsExchangeRecordDTO {
    /**
     * 兑换项id
     */
    private Long itemId;
    /**
     * 收货地址
     */
    private String address;
    /**
     * 联系电话
     */
    private String phone;
}
