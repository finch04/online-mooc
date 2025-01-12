package com.tianji.message.domain.dto;

import com.tianji.common.domain.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * <p>
 * 第三方短信平台
 * </p>
 *
 * @author 虎哥
 * @since 2022-08-19
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "短信模板")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageTemplateDTO extends BaseDTO {

    @Schema(description = "信发送模板id，新增时无需填写")
    private Long id;

    @Schema(description = "第三方短信推送渠道id")
    private Long platformCode;

    @Schema(description = "第三方短信推送渠道名称")
    private String platformName;

    @Schema(description = "短信模板名称")
    private String name;

    @Schema(description = "短信模板预览内容")
    private String content;

    @Schema(description = "第三方平台短信签名")
    private String signName;

    @Schema(description = "第三方平台短信模板code")
    private String thirdTemplateCode;

    @Schema(description = "模板状态:  0-草稿，1-使用中，2-停用")
    private Integer status;
}
