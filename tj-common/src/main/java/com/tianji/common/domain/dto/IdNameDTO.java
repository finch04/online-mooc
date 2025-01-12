package com.tianji.common.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "id和name键值对")
@NoArgsConstructor
@AllArgsConstructor
public class IdNameDTO {
    @Schema(description = "id")
    private Long id;
    @Schema(description = "name")
    private String name;
}
