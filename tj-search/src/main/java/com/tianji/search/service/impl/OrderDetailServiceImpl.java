package com.tianji.search.service.impl;/**
 * @author fsq
 * @date 2025/5/25 9:49
 */

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.search.domain.po.OrderDetail;
import com.tianji.search.repository.OrderDetailRepository;
import com.tianji.search.service.IOrderDetailService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/5/25 9:49
 * @Version: 1.0
 */
@Service
public class OrderDetailServiceImpl implements IOrderDetailService {

    @Resource
    private OrderDetailRepository orderDetailRepository;

    @Override
    public void saveAll(List<OrderDetail> orderDetails) {
        orderDetailRepository.saveAll(orderDetails);
    }
}
