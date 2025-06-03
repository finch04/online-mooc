package com.tianji.pay.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 对账记录
 * </p>
 *
 * @author YourName
 * @since 2024-XX-XX
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("reconciliation_record")
public class ReconciliationRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 业务订单号
     */
    private Long bizOrderNo;

    /**
     * 支付单号
     */
    private Long payOrderNo;

    /**
     * 退款单号
     */
    private Long refundOrderNo;

    /**
     * 支付渠道代码
     */
    private String payChannelCode;

    /**
     * 支付金额，单位为分
     */
    private Integer amount;

    /**
     * 退款金额，单位为分
     */
    private Integer refundAmount;

    /**
     * 对账状态，0：未对账，1：对账成功，2：对账失败
     */
    private Integer reconciliationStatus;

    /**
     * 对账时间
     */
    private LocalDateTime reconciliationTime;

    /**
     * 第三方返回业务码
     */
    private String resultCode;

    /**
     * 第三方返回提示信息
     */
    private String resultMsg;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private Long creater;

    /**
     * 更新人
     */
    private Long updater;

    /**
     * 逻辑删除，0：未删除，1：已删除
     */
    @TableLogic
    private Boolean deleted;
}