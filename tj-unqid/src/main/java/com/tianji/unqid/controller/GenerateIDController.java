package com.tianji.unqid.controller;

import com.tianji.unqid.service.IGenerateIDService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: fsq
 * @Date: 2025/10/4 19:33
 * @Version: 1.0
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "ID生成服务")
@RequestMapping("/unqid")
public class GenerateIDController {

    private final IGenerateIDService generateIDService;


    @GetMapping("/getSeqId")
    public Long getSeqId(){
        return generateIDService.getSeqId();
    }


    @GetMapping("/getUnSeqId")
    public Long getUnSeqId(){
        return generateIDService.getUnSeqId();
    }
}
