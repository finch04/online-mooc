package com.tianji.auth.domain.vo;

import com.tianji.auth.domain.po.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "菜单选项实体")
public class MenuOptionVO {
    @Schema(description = "菜单id", example = "1")
    private Long id;

    @Schema(description = "父菜单id", example = "0")
    private Long parentId;

    @Schema(description = "菜单文本", example = "系统管理")
    private String label;

    @Schema(description = "菜单图标", example = "el-icon-sys")
    private String icon;

    @Schema(description = "是否有子菜单", example = "false")
    private Boolean hasChildren;

    @Schema(description = "菜单顺序", example = "1")
    private Integer priority;

    @Schema(description = "子菜单集合")
    private List<MenuOptionVO> subMenus;

    public MenuOptionVO() {
    }

    public MenuOptionVO(Menu menu) {
        this.id = menu.getId();
        this.parentId = menu.getParentId();
        this.label = menu.getLabel();
        this.icon = menu.getIcon();
        this.hasChildren = menu.getHasChildren();
        this.priority = menu.getPriority();
    }
}
