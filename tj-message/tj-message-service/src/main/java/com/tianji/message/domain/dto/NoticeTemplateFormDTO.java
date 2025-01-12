package com.tianji.message.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "通知模板表单实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeTemplateFormDTO {
    @Schema(description = "主键id，新增时无需填写")
    private Long id;
    @Schema(description = "模板名称")
    private String name;
    @Schema(description = "模板代号，例如：VERIFY_CODE")
    private String code;
    @Schema(description = "通知类型：0-系统通知，1-笔记通知，2-问答通知，3-其它通知")
    private Integer type;
    @Schema(description = "模板状态:  0-草稿，1-使用中，2-停用")
    private Integer status;
    @Schema(description = "通知标题")
    private String title;
    @Schema(description = "通知内容")
    private String content;
    @Schema(description = "是否是短信模板，默认false")
    private Boolean isSmsTemplate;
    @Schema(description = "如果需要添加短信模板时填写这个，可以包含不同短信渠道")
    private List<MessageTemplateFormDTO> messageTemplates;
    @Schema(description = "如果需要删除已添加的短信模板时填写这个，只填id")
    private List<Long> deleteMessageTemplates;
}