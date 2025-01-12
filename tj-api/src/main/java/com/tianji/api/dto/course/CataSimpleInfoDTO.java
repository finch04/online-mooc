package com.tianji.api.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wusongsong
 * @since 2022/7/27 14:22
 * @version 1.0.0
 **/
@Data
@Schema(description = "目录简单信息")
public class CataSimpleInfoDTO {
    @Schema(description = "目录id")
    private Long id;
    @Schema(description = "目录名称")
    private String name;
    @Schema(description = "数字序号，不包含章序号")
    private Integer cIndex;
}
