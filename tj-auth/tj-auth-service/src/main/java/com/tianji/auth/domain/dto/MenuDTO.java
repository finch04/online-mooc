package com.tianji.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "菜单表单实体")
public class MenuDTO {
    @Schema(description = "菜单id", example = "1")
    private Long id;

    @Schema(description = "父菜单id", example = "0")
    private Long parentId;

    @Schema(description = "菜单文本", example = "系统管理")
    private String label;

    @Schema(description = "菜单路径", example = "/sys/index")
    private String path;

    @Schema(description = "菜单图标", example = "el-icon-sys")
    private String icon;

    @Schema(description = "菜单顺序", example = "1")
    private Integer priority;
}
