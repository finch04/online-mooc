package com.tianji.message.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.message.domain.po.Sensitive;
import com.tianji.message.domain.query.SensitiveQuery;
import com.tianji.message.service.ISensitiveService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "敏感词管理接口")
@RestController
@RequestMapping("/sensitive")
@RequiredArgsConstructor
public class SensitiveController {

    @Autowired
    private ISensitiveService sensitiveService;

    @GetMapping("/list")
    public PageDTO<Sensitive> list(SensitiveQuery query) {
        return sensitiveService.getAllSensitiveWords(query);
    }

    @PostMapping
    public boolean saveSensitive(@RequestBody Sensitive sensitive) {
        return sensitiveService.saveSensitive(sensitive);
    }

    @PutMapping
    public boolean updateSensitive(@RequestBody Sensitive sensitive) {
        return sensitiveService.updateSensitive(sensitive);
    }

    @DeleteMapping("/{id}")
    public boolean deleteSensitive(@PathVariable Long id) {
        return sensitiveService.deleteSensitive(id);
    }
}    