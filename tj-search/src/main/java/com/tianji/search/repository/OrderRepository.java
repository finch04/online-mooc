package com.tianji.search.repository;

import com.tianji.search.domain.po.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    String INDEX_NAME = "order";
    String PAY_ORDER_NO = "payOrderNo";
    String USER_ID = "userId";
    String STATUS = "status";
    String TOTAL_AMOUNT = "totalAmount";
    String REAL_AMOUNT = "realAmount";
    String DISCOUNT_AMOUNT = "discountAmount";
    String PAY_CHANNEL = "payChannel";
    String CREATE_TIME = "createTime";
    String PAY_TIME = "payTime";
    String CLOSE_TIME = "closeTime";
    String FINISH_TIME = "finishTime";
    String REFUND_TIME = "refundTime";
    String UPDATE_TIME = "updateTime";

    void save(Order order);

    void deleteById(Long orderId);

    Optional<Order> findById(Long orderId);

    void updateById(Long orderId, Object ... docs);

    void saveAll(List<Order> list);

    void deleteByIds(List<Long> orderIds);
}