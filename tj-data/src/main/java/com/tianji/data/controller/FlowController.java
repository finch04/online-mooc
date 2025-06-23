package com.tianji.data.controller;

import com.tianji.data.service.IFlowService;
import com.tianji.data.model.query.FlowQuery;
import com.tianji.data.model.vo.EchartsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: fsq
 * @Date: 2025/6/21 10:28
 * @Version: 1.0
 */
@RestController
@Api(tags = "流量统计相关接口")
@RequestMapping("/data/flow")
public class FlowController {

    @Autowired
    private IFlowService flowService;

    /**
     * 数据展示：7日|新用户数趋势
     * @param query 查询参数
     * @return EchartsVO
     */
    @GetMapping("/dnu")
    @ApiOperation(value = "查询每日新增用户数")
    public EchartsVO dnu(FlowQuery query) {
        return flowService.dnu(query);
    }

    /**
     * 数据展示：7日|页面浏览量趋势
     * @param query 查询参数
     * @return EchartsVO
     */
    @GetMapping("/dpv")
    @ApiOperation(value = "查询页面浏览量趋势")
    public EchartsVO dpv(FlowQuery query) {
        return flowService.dpv(query);
    }

    /**
     * 数据展示：7日|独立访客数趋势
     * @param query 查询参数
     * @return EchartsVO
     */
    @GetMapping("/duv")
    @ApiOperation(value = "查询独立访客数趋势")
    public EchartsVO duv(FlowQuery query) {
        return flowService.duv(query);
    }

    /**
     * 数据展示：7日|活跃用户数趋势
     * @param query 查询参数
     * @return EchartsVO
     */
    @GetMapping("/dau")
    @ApiOperation(value = "查询日活跃用户数趋势")
    public EchartsVO dau(FlowQuery query) {
        return flowService.dau(query);
    }

    /**
     * 数据展示：7日|活跃访问数时段
     * @param query 查询参数
     * @return EchartsVO
     */
    @GetMapping("/dpv/time")
    @ApiOperation(value = "查询日活跃访问数时段分布")
    public EchartsVO dpvTime(FlowQuery query) {
        return flowService.dpvTime(query);
    }

    /**
     * 数据展示：7日|活跃用户省分布排名
     * @param query 查询参数
     * @return EchartsVO
     */
    @GetMapping("/dau/province")
    @ApiOperation(value = "查询日活跃用户省分布排名")
    public EchartsVO dauProvince(FlowQuery query) {
        return flowService.dauProvince(query);
    }


}