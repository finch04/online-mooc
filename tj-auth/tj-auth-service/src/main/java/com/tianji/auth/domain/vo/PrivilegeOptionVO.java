package com.tianji.auth.domain.vo;

import com.tianji.auth.domain.po.Privilege;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "API权限选项实体")
public class PrivilegeOptionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "权限id", example = "1")
    private Long id;
    @Schema(description = "权限说明", example = "新增员工")
    private String intro;
    @Schema(description = "是否选中", example = "true")
    private Boolean checked;

    public PrivilegeOptionVO() {
    }

    public PrivilegeOptionVO(Privilege privilege) {
        this.id = privilege.getId();
        this.intro = privilege.getIntro();
    }
}
