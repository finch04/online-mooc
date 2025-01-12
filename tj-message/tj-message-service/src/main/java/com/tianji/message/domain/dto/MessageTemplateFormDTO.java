package com.tianji.message.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 第三方短信平台
 * </p>
 *
 * @author 虎哥
 * @since 2022-08-19
 */
@Schema(description = "短信模板表单实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageTemplateFormDTO {

    @Schema(description = "信发送模板id，新增时无需填写")
    private Long id;

    @Schema(description = "第三方短信平台代号")
    private String platformCode;

    @Schema(description = "第三方平台短信签名")
    private String signName;

    @Schema(description = "第三方平台短信模板code")
    private String thirdTemplateCode;

    @Schema(description = "模板状态:  0-草稿，1-使用中，2-停用")
    private Integer status;

    @Schema(description = "短信模板名称，如果是通知模板下的短信模板，则无需填写")
    private String name;

    @Schema(description = "短信模板内容预览，如果是通知模板下的短信模板，则无需填写")
    private String content;
}