package com.tianji.data.controller;

import com.tianji.data.influxdb.service.IFlowService;
import com.tianji.data.model.query.FlowQuery;
import com.tianji.data.model.vo.EchartsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
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
     * 数据展示：
     * 7日|每日新增用户数
     * @param query 查询参数
     * @return DnuVO
     */
    @PostMapping("/base")
    @ApiOperation(value = "7日|每日新增用户数", notes = "7日|每日新增用户数")
    public EchartsVO dnu(FlowQuery query) {
        return flowService.dnu(query);
    }

    /**
     * 数据展示：7日|活跃用户数趋势
     * @param query 查询参数
     * @return DauVO
     */
    @PostMapping("/dau")
    @ApiOperation(value = "7日|活跃用户数趋势", notes = "7日|活跃用户数趋势")
    public EchartsVO dau(FlowQuery query) {
        return flowService.dau(query);
    }

    /**
     * 数据展示：7日|日访问量趋势
     * @param query 查询参数
     * @return 每日访问量列表
     */
    @PostMapping("/dpv")
    @ApiOperation(value = "7日|日访问量趋势", notes = "按日统计7天内的访问量")
    public EchartsVO dpv(FlowQuery query) {
        return flowService.dpv(query);
    }

    /**
     * 数据展示：7日|日用户访问量趋势（去重用户数）
     * @param query 查询参数
     * @return 每日去重用户数列表
     */
    @PostMapping("/duv")
    @ApiOperation(value = "7日|日用户访问量趋势", notes = "按日统计7天内的去重用户数")
    public EchartsVO duv(FlowQuery query) {
        return flowService.duv(query);
    }

    /**
     * 数据展示：7日|日报错次数趋势
     * @param query 查询参数
     * @return 每日报错次数列表
     */
    @PostMapping("/error/count")
    @ApiOperation(value = "7日|日报错次数趋势", notes = "按日统计7天内的报错次数")
    public EchartsVO errorCount(FlowQuery query) {
        return flowService.errorCount(query);
    }


    /**
     * 数据展示：7日|新注册用户详情（RequestBody）
     * @param query 查询参数
     * @return 新注册用户请求体列表
     */
    @PostMapping("/dnu/body")
    @ApiOperation(value = "7日|新注册用户详情", notes = "按日获取7天内新注册用户的请求体数据")
    public EchartsVO dnuBody(FlowQuery query) {
        return flowService.dnuBody(query);
    }
}