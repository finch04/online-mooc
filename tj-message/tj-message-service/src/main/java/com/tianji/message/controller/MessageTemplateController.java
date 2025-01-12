package com.tianji.message.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.message.domain.dto.MessageTemplateDTO;
import com.tianji.message.domain.dto.MessageTemplateFormDTO;
import com.tianji.message.domain.query.MessageTemplatePageQuery;
import com.tianji.message.service.IMessageTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 第三方短信平台模板信息管理 前端控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/message-templates")
@Tag(name = "短信模板管理接口")
public class MessageTemplateController {

    private final IMessageTemplateService messageTemplateService;

    @PostMapping
    @Operation(summary = "新增短信模板")
    public Long saveMessageTemplate(@RequestBody MessageTemplateFormDTO messageTemplateDTO) {
        return messageTemplateService.saveMessageTemplate(messageTemplateDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新短信模板")
    public void updateMessageTemplate(
            @RequestBody MessageTemplateFormDTO messageTemplateDTO,
            @Parameter(description = "短信模板id", example = "1") @PathVariable("id") Long id) {
        messageTemplateDTO.setId(id);
        messageTemplateService.updateMessageTemplate(messageTemplateDTO);
    }

    @GetMapping
    @Operation(summary = "分页查询短信模板")
    public PageDTO<MessageTemplateDTO> queryMessageTemplates(MessageTemplatePageQuery pageQuery) {
        return messageTemplateService.queryMessageTemplates(pageQuery);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询短信模板")
    public MessageTemplateDTO queryMessageTemplate(@Parameter(description = "模板id", example = "1") @PathVariable("id") Long id) {
        return messageTemplateService.queryMessageTemplate(id);
    }
}
