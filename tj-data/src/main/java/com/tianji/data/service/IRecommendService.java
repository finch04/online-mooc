package com.tianji.data.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.data.model.po.Dau;

import java.util.List;

/**
 * @Description：推荐算法服务
 */
public interface IRecommendService {


    List<Long> featureRecommend();
}
