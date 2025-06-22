package com.tianji.data.controller;

import com.tianji.data.influxdb.service.IFlowService;
import com.tianji.data.model.query.FlowQuery;
import com.tianji.data.model.vo.EchartsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
     * 7日|活跃用户数趋势
     * 7日|日访问量趋势
     * 7日|日用户访问量趋势（去重用户数）
     * @param query 查询参数
     * @return DnuVO
     */
    @GetMapping("/base")
    @ApiOperation(value = "查询基础流量数据")
    public EchartsVO base(FlowQuery query) {
        return flowService.base(query);
    }


    /**
     * 数据展示：
     * 7日|URL访问量前10名
     * @param query 查询参数
     * @return 每日报错次数列表
     */
    @GetMapping("/url/visits")
    @ApiOperation(value = "查询url访问数据")
    public EchartsVO urlVisits(FlowQuery query) {
        return flowService.urlVisits(query);
    }

    /**
     * 数据展示：
     * 7日|URL总体概览
     * 7日|URL报错量前10名
     * @param query 查询参数
     * @return 每日报错次数列表
     */
    @GetMapping("/url/errors")
    @ApiOperation(value = "查询url报错数据")
    public EchartsVO urlErrors(FlowQuery query) {
        return flowService.urlErrors(query);
    }

//
//    /**
//     * 数据展示：7日|新注册用户详情（RequestBody）
//     * @param query 查询参数
//     * @return 新注册用户请求体列表
//     */
//    @PostMapping("/dnu/body")
//    @ApiOperation(value = "7日|新注册用户详情", notes = "按日获取7天内新注册用户的请求体数据")
//    public EchartsVO dnuBody(FlowQuery query) {
//        return flowService.dnuBody(query);
//    }
}