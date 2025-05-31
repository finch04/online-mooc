package com.tianji.search.service;

import com.tianji.api.dto.trade.OrderAnalysisDTO;
import com.tianji.search.domain.po.Order;

import java.util.List;

/**
 * @author fsq
 * @date 2025/5/25 9:48
 */
public interface IOrderService {

    void saveAll(List<Order> orders);
}
