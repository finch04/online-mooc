package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.po.PointsMonthlySummary;
import com.tianji.learning.service.IPointsMonthlySummaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 用户月度积分汇总表 前端控制器
 * </p>
 *
 * @author 参考示例
 */
@Api(tags = "用户月度积分汇总管理")
@RestController
@RequestMapping("/points-monthly-summary")
@RequiredArgsConstructor
public class PointsMonthlySummaryController {

    private final IPointsMonthlySummaryService summaryService;

    @ApiOperation("获取用户指定月份积分汇总")
    @GetMapping("/user/{userId}/month/{yearMonth}")
    public PointsMonthlySummary querySummaryByUserAndMonth(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId,
            @ApiParam(value = "年月（YYYYMM）", required = true) @PathVariable String yearMonth) {
        return summaryService.querySummaryByUserAndMonth(userId, yearMonth);
    }

    @ApiOperation("分页查询用户积分汇总记录")
    @GetMapping("/user/{userId}/page")
    public PageDTO<PointsMonthlySummary> querySummaryByUser(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId,
            PageQuery query) {
        return summaryService.querySummaryByUser(userId, query);
    }

//    @ApiOperation("统计用户总积分（所有月份累计）")
//    @GetMapping("/user/{userId}/total")
//    public Integer queryUserTotalPoints(@ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
//        return summaryService.queryUserTotalPoints(userId);
//    }

    @ApiOperation("获取最近n个月的积分汇总")
    @GetMapping("/user/{userId}/recent/{n}")
    public List<PointsMonthlySummary> queryRecentNSummaries(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId,
            @ApiParam(value = "最近n个月", required = true) @PathVariable Integer n) {
        return summaryService.queryRecentNSummaries(userId, n);
    }
}