package com.tianji.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@Schema(description = "API权限")
public class PrivilegeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "权限id", example = "1")
    private Long id;
    @Schema(description = "权限所属菜单id", example = "1")
    private Long menuId;
    @Schema(description = "权限说明", example = "新增员工")
    @NotNull(message = "权限说明不能为空")
    private String intro;
    @Schema(description = "API请求方式", example = "GET")
    @Pattern(regexp = "^GET|POST|PUT|DELETE$", message = "请求方式必须是大写")
    private String method;
    @Schema(description = "API请求路径", example = "/account/staff")
    @NotNull(message = "uri不能为空")
    private String uri;
    @Schema(description = "是否是内部权限")
    private Boolean internal;
}
