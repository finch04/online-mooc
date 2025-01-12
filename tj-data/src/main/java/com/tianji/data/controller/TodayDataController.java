package com.tianji.data.controller;

import com.tianji.data.model.dto.TodayDataDTO;
import com.tianji.data.model.vo.TodayDataVO;
import com.tianji.data.service.TodayDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 今日数据
 * @author wusongsong
 * @date 2022/10/13 9:21
 */
@RestController
@RequestMapping("/data/today")
@Tag(name = "今日数据相关接口")
@Slf4j
public class TodayDataController {

    @Resource
    private TodayDataService todayDataService;

    @GetMapping("")
    @Operation(summary = "获取今日数据")
    public TodayDataVO get() {
        return todayDataService.get();
    }

    @PutMapping("set")
    @Operation(summary = "设置线上数据")
    public void set(@RequestBody TodayDataDTO todayDataDTO) {
        todayDataService.set(todayDataDTO);
    }
}
