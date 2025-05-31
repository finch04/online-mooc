package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.po.PointsMallItems;
import com.tianji.learning.domain.query.PointsItemsPageQuery;
import com.tianji.learning.service.IPointsMallItemsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 积分商城商品表 前端控制器
 * </p>
 *
 * @author 参考示例
 */
@Api(tags = "积分商城商品管理")
@RestController
@RequestMapping("/points-mall-items")
@RequiredArgsConstructor
public class PointsMallItemsController {

    private final IPointsMallItemsService pointsMallItemsService;

//    @ApiOperation("新增积分商品")
//    @PostMapping
//    public void addPointsItem(@RequestBody @Valid PointsMallItems items) {
//        pointsMallItemsService.save(items);
//    }

//    @ApiOperation("根据ID删除积分商品")
//    @DeleteMapping("/{id}")
//    public void deletePointsItem(@ApiParam(value = "商品ID", required = true) @PathVariable Long id) {
//        pointsMallItemsService.removeById(id);
//    }

//    @ApiOperation("更新积分商品信息")
//    @PutMapping
//    public void updatePointsItem(@RequestBody @Valid PointsMallItems items) {
//        pointsMallItemsService.updateById(items);
//    }

    @ApiOperation("根据ID查询积分商品")
    @GetMapping("/{id}")
    public PointsMallItems queryPointsItemById(@ApiParam(value = "商品ID", required = true) @PathVariable Long id) {
        return pointsMallItemsService.getById(id);
    }

    @ApiOperation("分页查询积分商品列表")
    @GetMapping("/page")
    public PageDTO<PointsMallItems> queryPointsItemsByPage(PointsItemsPageQuery query) {
        return pointsMallItemsService.queryPointsItemsByPage(query);
    }
}