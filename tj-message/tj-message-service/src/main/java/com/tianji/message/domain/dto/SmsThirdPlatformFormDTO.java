package com.tianji.message.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 第三方云通讯平台
 * </p>
 *
 * @author 虎哥
 * @since 2022-08-20
 */
@Schema(description = "短信第三方平台信息的表单实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsThirdPlatformFormDTO {

    @Schema(description = "短信平台id，新增时无需填写")
    private Long id;
    @Schema(description = "短信平台名称")
    private String name;
    @Schema(description = "短信平台代码，例如：ali")
    private String code;
    @Schema(description = "数字越小优先级越高，最小为0")
    private Integer priority;
    @Schema(description = "短信平台状态：0-禁用，1-启用")
    private Integer status;
}