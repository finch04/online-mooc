package com.tianji.data.controller;/**
 * @author fsq
 * @date 2025/6/22 15:49
 */

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.data.model.query.FlowQuery;
import com.tianji.data.model.vo.CourseProfileVO;
import com.tianji.data.model.vo.EchartsVO;
import com.tianji.data.model.vo.FunnelPlotChartsVO;
import com.tianji.data.model.vo.UserProfileVO;
import com.tianji.data.service.IAnalysisService;
import com.tianji.data.service.IRecommendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/6/24 15:49
 * @Version: 1.0
 */
@RestController
@RequestMapping("/data/recommend")
@Api(tags = "数据分析相关操作")
@Slf4j
public class RecommendController {

    @Autowired
    private IRecommendService recommendService;

    /**
     * 基于特征推荐课程
     * @return
     */
    @GetMapping("/feature")
    @ApiOperation(value = "基于特征推荐课程")
    public List<Long> featureRecommend() {
        return recommendService.featureRecommend();
    }

}
