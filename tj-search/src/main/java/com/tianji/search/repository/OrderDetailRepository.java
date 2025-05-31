package com.tianji.search.repository;

import com.tianji.search.domain.po.OrderDetail;

import java.util.List;
import java.util.Optional;

public interface OrderDetailRepository {
    String INDEX_NAME = "order_detail";
    String ORDER_ID = "orderId";
    String USER_ID = "userId";
    String COURSE_ID = "courseId";
    String PRICE = "price";
    String STATUS = "status";
    String REFUND_STATUS = "refundStatus";
    String PAY_CHANNEL = "payChannel";
    String CREATE_TIME = "createTime";
    String UPDATE_TIME = "updateTime";

    void save(OrderDetail orderDetail);

    void deleteById(Long orderDetailId);

    Optional<OrderDetail> findById(Long orderDetailId);

    void updateById(Long orderDetailId, Object ... docs);

    void saveAll(List<OrderDetail> list);

    void deleteByIds(List<Long> orderDetailIds);
}