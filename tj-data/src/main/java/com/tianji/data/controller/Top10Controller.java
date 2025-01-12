package com.tianji.data.controller;

import com.tianji.data.model.dto.Top10DataSetDTO;
import com.tianji.data.model.vo.Top10DataVO;
import com.tianji.data.service.Top10Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 工作台top10数据相关接口
 * @author wusongsong
 * @date 2022/10/10 19:27
 */
@RestController
@RequestMapping("/data/top10")
@Tag(name = "工作台top10数据相关接口")
@Slf4j
public class Top10Controller {

    @Resource
    private Top10Service top10Service;

    @GetMapping("")
    @Operation(summary = "top10数据获取")
    public Top10DataVO getTop10Data() {
        return top10Service.getTop10Data();
    }

    @PutMapping("set")
    @Operation(summary = "设置top10数据")
    public void setTop10Data(@RequestBody @Validated Top10DataSetDTO top10DataSetDTO) {
        top10Service.setTop10Data(top10DataSetDTO);
    }

}
