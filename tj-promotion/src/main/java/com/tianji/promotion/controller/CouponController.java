package com.tianji.promotion.controller;


import com.tianji.api.dto.promotion.CouponDetailSimpleVO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.promotion.domain.dto.CouponFormDTO;
import com.tianji.promotion.domain.dto.CouponIssueFormDTO;
import com.tianji.promotion.domain.query.CouponQuery;
import com.tianji.promotion.domain.vo.CouponDetailVO;
import com.tianji.promotion.domain.vo.CouponPageVO;
import com.tianji.promotion.domain.vo.CouponVO;
import com.tianji.promotion.service.ICouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 优惠券的规则信息 前端控制器
 * </p>
 *
 * @author fsq
 * @since 2023-10-28
 */
@Api(tags = "优惠券相关接口")
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final ICouponService couponService;

    @PostMapping("")
    @ApiOperation("新增优惠券-管理端")
    public void saveCoupon(@RequestBody @Validated CouponFormDTO dto){
        couponService.saveCoupon(dto);
    }

    @GetMapping ("page")
    @ApiOperation("分页查询优惠券-管理端")
    public PageDTO<CouponPageVO> queryCouponPage(CouponQuery query){
        return couponService.queryCouponPage(query);
    }


    @PutMapping ("{id}/issue")
    @ApiOperation("发放优惠券")
    public void issueCoupon(
            @PathVariable Long id,
            @RequestBody @Validated CouponIssueFormDTO dto){
        couponService.issueCoupon(id,dto);
    }

    @PutMapping ("{id}")
    @ApiOperation("修改优惠券")
    public void modifyCoupon(@PathVariable Long id,@RequestBody @Validated CouponFormDTO dto){
        couponService.modifyCoupon(id,dto);
    }

    @ApiOperation("根据id查询优惠券接口")
    @GetMapping("/{id}")
    public CouponDetailVO queryCouponById(@ApiParam("优惠券id") @PathVariable("id") Long id){
        return couponService.queryCouponById(id);
    }

    @ApiOperation("根据id查询简单优惠券接口")
    @GetMapping("/simple/{id}")
    public CouponDetailSimpleVO querySimpleCouponById(@ApiParam("优惠券id") @PathVariable("id") Long id){
        return couponService.querySimpleCouponById(id);
    }

    @ApiOperation("暂停发放优惠券接口")
    @PutMapping("/{id}/pause")
    public void pauseIssue(@ApiParam("优惠券id") @PathVariable("id") Long id) {
        couponService.pauseIssue(id);
    }

    @ApiOperation("删除优惠券")
    @DeleteMapping("{id}")
    public void deleteById(@ApiParam("优惠券id") @PathVariable("id") Long id) {
        couponService.deleteById(id);
    }


    @ApiOperation("查询发放中的优惠券列表-用户端")
    @GetMapping("/list")
    public List<CouponVO> queryIssuingCoupons(){
        return couponService.queryIssuingCoupons();
    }

}
