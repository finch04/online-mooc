package com.tianji.api.dto.trade;/**
 * @author fsq
 * @date 2025/5/25 10:44
 */

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/5/25 10:44
 * @Version: 1.0
 */
@Data
@Builder
public class OrderAnalysisDTO {
    private Long id;
    /**支付交易流水单*/
    private Long payOrderNo;
    /** 用户id*/
    private Long userId;
    /**订单状态，1：待支付，2：已支付，3：已关闭，4：已完成，5：已报名，6：已申请退款*/
    private Integer status;
    /**状态备注*/
    private String message;
    /**订单总金额，单位分*/
    private Integer totalAmount;
    /**实付金额，单位分*/
    private Integer realAmount;
    /**优惠金额，单位分*/
    private Integer discountAmount;
    /**支付渠道*/
    private String payChannel;
    /**优惠券id*/
    private List<Long> couponIds;
    /**创建订单时间*/
    private LocalDateTime createTime;
    /**支付时间*/
    private LocalDateTime payTime;
    /**订单关闭时间*/
    private LocalDateTime closeTime;
    /**订单完成时间，支付后30天*/
    private LocalDateTime finishTime;
    /** 申请退款时间*/
    private LocalDateTime refundTime;
    /**更新时间*/
    private LocalDateTime updateTime;
    /**创建人*/
    private Long creater;
    /**更新人*/
    private Long updater;
    /**逻辑删除*/
    private Integer deleted;
}
