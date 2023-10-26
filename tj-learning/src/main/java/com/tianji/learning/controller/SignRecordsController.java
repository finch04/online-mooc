package com.tianji.learning.controller;


import com.tianji.learning.domain.vo.SignResultVO;
import com.tianji.learning.service.ISignRecordsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 签到控制器
 */
@RestController
@Api(tags = "签到相关接口")
@RequestMapping("/sign-records")
@RequiredArgsConstructor
public class SignRecordsController {

    private final ISignRecordsService recordsService;

    @ApiOperation("签到")
    @PostMapping
    public SignResultVO addSignRecords(){
        return recordsService.addSignRecords();
    }

    @ApiOperation("查询签到记录")
    @GetMapping
    public Byte[] querySignRecords(){
        return recordsService.querySignRecords();
    }
}
