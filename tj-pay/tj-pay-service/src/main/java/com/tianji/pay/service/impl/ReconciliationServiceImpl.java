package com.tianji.pay.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.pay.domain.po.PayOrder;
import com.tianji.pay.domain.po.ReconciliationRecord;
import com.tianji.pay.domain.po.RefundOrder;
import com.tianji.pay.domain.query.ReconciliationQueryDTO;
import com.tianji.pay.mapper.ReconciliationRecordMapper;
import com.tianji.pay.service.IPayOrderService;
import com.tianji.pay.service.IReconciliationService;
import com.tianji.pay.service.IRefundOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationServiceImpl extends ServiceImpl<ReconciliationRecordMapper, ReconciliationRecord> implements IReconciliationService {

    @Autowired
    private IPayOrderService payOrderService;

    @Autowired
    private IRefundOrderService refundOrderService;

    @Override
    public PageDTO<ReconciliationRecord> queryReconciliationList(ReconciliationQueryDTO queryDTO) {
        // 1.分页和排序条件
        Page<ReconciliationRecord> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        page.addOrder(new OrderItem("id", true));
        // 2.查询
        Page<ReconciliationRecord> result = lambdaQuery()
                .eq(queryDTO.getStatus() != null, ReconciliationRecord::getReconciliationStatus, queryDTO.getStatus())
                .page(page);
        return PageDTO.of(result);
    }


    /**
     * 执行支付订单对账
     */
    @Override
    public void reconcilePayOrders() {
        log.info("开始执行未支付订单对账任务");
        List<PayOrder> payingOrders = payOrderService.queryPayingOrderByPage(1, Integer.MAX_VALUE).getList();
        for (PayOrder payOrder : payingOrders) {
            processOrderReconciliation(payOrder, true);
        }
        log.info("未支付订单对账任务执行完成");
    }

    /**
     * 执行退款订单对账
     */
    @Override
    public void reconcileRefundOrders() {
        log.info("开始执行退款中订单对账任务");
        List<RefundOrder> refundingOrders = refundOrderService.queryRefundingOrderByPage(1, Integer.MAX_VALUE).getList();
        for (RefundOrder refundOrder : refundingOrders) {
            processOrderReconciliation(refundOrder, false);
        }
        log.info("退款中订单对账任务执行完成");
    }

    /**
     * 统一处理订单对账并落库
     * @param order 订单对象，可以是 PayOrder 或 RefundOrder
     * @param isPayOrder 是否为支付订单
     */
    private void processOrderReconciliation(Object order, boolean isPayOrder) {
        ReconciliationRecord record = new ReconciliationRecord();
        try {
            if (isPayOrder) {
                PayOrder payOrder = (PayOrder) order;
                payOrderService.checkPayOrder(payOrder);
                record.setBizOrderNo(payOrder.getBizOrderNo());
                record.setPayOrderNo(payOrder.getPayOrderNo());
                record.setAmount(payOrder.getAmount());
                record.setPayChannelCode(payOrder.getPayChannelCode());
                // 假设对账成功
                record.setReconciliationStatus(1);
            } else {
                RefundOrder refundOrder = (RefundOrder) order;
                refundOrderService.checkRefundOrder(refundOrder);
                record.setBizOrderNo(refundOrder.getBizOrderNo());
                record.setRefundOrderNo(refundOrder.getRefundOrderNo());
                record.setRefundAmount(refundOrder.getRefundAmount());
                record.setPayChannelCode(refundOrder.getPayChannelCode());
                // 假设对账成功
                record.setReconciliationStatus(1);
            }
        } catch (Exception e) {
            if (isPayOrder) {
                PayOrder payOrder = (PayOrder) order;
                log.error("支付订单对账失败，支付订单号：{}", payOrder.getPayOrderNo(), e);
                record.setPayOrderNo(payOrder.getPayOrderNo());
            } else {
                RefundOrder refundOrder = (RefundOrder) order;
                log.error("退款订单对账失败，退款订单号：{}", refundOrder.getRefundOrderNo(), e);
                record.setRefundOrderNo(refundOrder.getRefundOrderNo());
            }
            record.setReconciliationStatus(2);
            record.setResultCode("ERROR");
            record.setResultMsg(e.getMessage());
        }
        record.setReconciliationTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        // 这里假设创建人和更新人是当前用户，你可以根据实际情况修改
        record.setCreater(1L);
        record.setUpdater(1L);
        record.setDeleted(false);
        save(record);
    }
}