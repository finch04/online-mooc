package com.tianji.search.service;

import com.tianji.search.domain.po.OrderDetail;

import java.util.List;

/**
 * @author fsq
 * @date 2025/5/25 9:48
 */
public interface IOrderDetailService {


    void saveAll(List<OrderDetail> orderDetails);
}
