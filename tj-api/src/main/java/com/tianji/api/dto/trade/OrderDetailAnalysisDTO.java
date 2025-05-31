package com.tianji.api.dto.trade;/**
 * @author fsq
 * @date 2025/5/25 10:56
 */

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: fsq
 * @Date: 2025/5/25 10:56
 * @Version: 1.0
 */
@Data
@Builder
public class OrderDetailAnalysisDTO {
    /**订单明细id*/
    private Long id;
    /**订单id*/
    private Long orderId;
    /**用户id*/
    private Long userId;
    /**课程id*/
    private Long courseId;
    /** 课程价格*/
    private Integer price;
    /**课程名称*/
    private String name;
    /**封面地址*/
    private String coverUrl;
    /**课程学习有效期，单位：月。从付款时间开始算*/
    private Integer validDuration;
    /**课程学习过期时间*/
    private LocalDateTime courseExpireTime;
    /**折扣金额*/
    private Integer discountAmount;
    /**实付金额*/
    private Integer realPayAmount;
    /**订单详情状态，1：待支付，2：已支付，3：已关闭，4：已完成，5：已报名*/
    private Integer status;
    /**1：待审批，2：取消退款，3：同意退款，4：拒绝退款，5：退款成功，6：退款失败*/
    private Integer refundStatus;
    /**支付渠道名称*/
    private String payChannel;
    /**创建时间*/
    private LocalDateTime createTime;
    /** 更新时间*/
    private LocalDateTime updateTime;
    /**创建人*/
    private Long creater;
    /** 更新人*/
    private Long updater;
}
