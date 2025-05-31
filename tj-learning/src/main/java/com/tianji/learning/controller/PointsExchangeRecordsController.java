package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.PointsExchangeRecordDTO;
import com.tianji.learning.domain.po.PointsExchangeRecords;
import com.tianji.learning.domain.vo.PointsExchangeRecordsVO;
import com.tianji.learning.service.IPointsExchangeRecordsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 积分兑换记录表 前端控制器
 * </p>
 *
 * @author 参考示例
 */
@Api(tags = "积分兑换记录管理")
@RestController
@RequestMapping("/points-exchange-records")
@RequiredArgsConstructor
public class PointsExchangeRecordsController {

    private final IPointsExchangeRecordsService exchangeRecordsService;

    @ApiOperation("用户兑换商品")
    @PostMapping
    public void exchangeItem(@RequestBody @Valid PointsExchangeRecordDTO dto) {
        exchangeRecordsService.exchangeItem(dto);
    }

//    @ApiOperation("更新兑换记录状态")
//    @PutMapping("/status/{id}")
//    public void updateExchangeStatus(
//            @ApiParam(value = "兑换记录ID", required = true) @PathVariable Long id,
//            @ApiParam(value = "兑换状态：0-待发货，1-已发货，2-已完成，3-已取消", required = true) @RequestParam Byte status) {
//        exchangeRecordsService.updateExchangeStatus(id, status);
//    }

    @ApiOperation("取消兑换")
    @PutMapping("/cancel/{id}")
    public void cancelExchangeStatus(
            @ApiParam(value = "兑换记录ID", required = true) @PathVariable Long id) {
        exchangeRecordsService.cancelExchangeStatus(id);
    }

    @ApiOperation("根据ID查询兑换记录")
    @GetMapping("/{id}")
    public PointsExchangeRecordsVO queryExchangeRecordById(@ApiParam(value = "兑换记录ID", required = true) @PathVariable Long id) {
        return exchangeRecordsService.queryExchangeRecordById(id);
    }

    @ApiOperation("分页查询用户兑换记录")
    @GetMapping("/user/page")
    public PageDTO<PointsExchangeRecordsVO> queryExchangeRecordsByUser(PageQuery query) {
        Long userId = UserContext.getUser();
        return exchangeRecordsService.queryExchangeRecordsByUser(userId, query);
    }

//    @ApiOperation("分页查询所有兑换记录")
//    @GetMapping("/page")
//    public PageDTO<PointsExchangeRecords> queryAllExchangeRecords(
//            PageQuery query,
//            @ApiParam("商品ID") @RequestParam(required = false) Long itemId,
//            @ApiParam("兑换状态") @RequestParam(required = false) Byte status) {
//        return exchangeRecordsService.queryAllExchangeRecords(query, itemId, status);
//    }
}