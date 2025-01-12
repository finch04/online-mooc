package com.tianji.api.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName MediaQuoteDTO
 * @author wusongsong
 * @since 2022/7/18 17:43
 * @version 1.0.0
 **/
@Data
@Schema(description = "媒资被引用情况")
@AllArgsConstructor
@NoArgsConstructor
public class MediaQuoteDTO {
    @Schema(description = "媒资id")
    private Long mediaId;
    @Schema(description = "引用数")
    private Integer quoteNum;
}
