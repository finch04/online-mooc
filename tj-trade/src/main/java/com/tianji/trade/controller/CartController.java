package com.tianji.trade.controller;

import com.tianji.trade.domain.dto.CartsAddDTO;
import com.tianji.trade.domain.vo.CartVO;
import com.tianji.trade.service.ICartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车条目信息，也就是购物车中的课程 前端控制器
 */

@RequiredArgsConstructor
@RestController
@RequestMapping("/carts")
@Tag(name = "购物车相关接口")
public class CartController {

    private final ICartService cartService;

    @Operation(summary = "添加课程到购物车")
    @PostMapping
    public void addCourse2Cart(@Valid @RequestBody CartsAddDTO cartsAddDTO){
        cartService.addCourse2Cart(cartsAddDTO.getCourseId());
    }

    @Operation(summary = "获取购物车中的课程")
    @GetMapping
    public List<CartVO> getMyCarts(){
        return cartService.getMyCarts();
    }

    @Operation(summary = "删除指定的购物车条目")
    @DeleteMapping("/{id}")
    public void deleteCartById(@Parameter(description = "购物车条目id") @PathVariable("id") Long id){
        cartService.deleteCartById(id);
    }

    @Operation(summary = "批量删除购物车条目")
    @DeleteMapping
    public void deleteCartById(@Parameter(description = "购物车条目id集合") @RequestParam("ids") List<Long> ids){
        cartService.deleteCartByIds(ids);
    }
}
