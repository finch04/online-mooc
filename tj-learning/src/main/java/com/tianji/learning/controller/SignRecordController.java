package com.tianji.learning.controller;

import com.tianji.learning.domain.vo.SignResultVO;
import com.tianji.learning.service.ISignRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 签到相关接口
 */
@RestController
@RequestMapping("sign-records")
@Tag(name = "签到相关接口")
@RequiredArgsConstructor
public class SignRecordController {

    private final ISignRecordService recordService;

    @Operation(summary = "签到功能接口")
    @PostMapping
    public SignResultVO addSignRecords() {
        return recordService.addSignRecords();
    }

    @Operation(summary = "查询签到记录")
    @GetMapping
    public Byte[] querySignRecords() {
        return recordService.querySignRecords();
    }
}
