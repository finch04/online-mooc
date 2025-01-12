package com.tianji.trade.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理端订单条目详细信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailAdminVO {
    @Schema(description = "订单条目id")
    private Long id;
    @Schema(description = "订单id")
    private Long orderId;
    @Schema(description = "退款id")
    private Long refundApplyId;

    @Schema(description = "支付流水单号")
    private Long payOrderNo;
    @Schema(description = "退款流水单号")
    private Long refundOrderNo;

    @Schema(description = "学员昵称")
    private String studentName;
    @Schema(description = "手机号")
    private String mobile;
    @Schema(description = "退款申请人，格式：角色-名字")
    private String refundProposerName;

    @Schema(description = "支付方式")
    private String payChannel;
    @Schema(description = "退款方式")
    private String refundChannel;
    @Schema(description = "退款失败原因")
    private String failedReason;

    @Schema(description = "申请退款原因")
    private String refundReason;
    @Schema(description = "申请退款描述")
    private String refundMessage;
    @Schema(description = "审批意见")
    private String remark;

    @Schema(description = "订单状态，1：待支付，2：已支付，3：已关闭，4：已完成，5：已报名")
    private Integer status;
    @Schema(description = "退款状态，1：待审批，2：取消退款，3：同意退款，4：拒绝退款，5：退款成功，6：退款失败")
    private Integer refundStatus;
    @Schema(description = "状态描述")
    private String message;

    @Schema(description = "订单节点进度节点列表")
    private List<OrderProgressNodeVO> nodes;

    @Schema(description = "课程名称")
    private String name;
    @Schema(description = "课程价格")
    private Integer price;
    @Schema(description = "实付金额")
    private Integer realPayAmount;
    @Schema(description = "优惠券规则")
    private String couponDesc;
    @Schema(description = "优惠总金额")
    private Integer discountAmount;
    @Schema(description = "学习有效期")
    private LocalDateTime studyValidTime;

    @Schema(description = "是否可以退款")
    private Boolean canRefund;
}