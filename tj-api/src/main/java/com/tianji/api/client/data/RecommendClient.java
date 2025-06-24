package com.tianji.api.client.data;

import com.tianji.api.dto.course.CataSimpleInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(contextId = "recommend", value = "data-service",path = "data")
public interface RecommendClient {

    /**
     * 基于特征的推荐算法
     *
     * @return 课程id
     */
    @GetMapping("/recommend/feature")
    List<Long> featureRecommend();


}