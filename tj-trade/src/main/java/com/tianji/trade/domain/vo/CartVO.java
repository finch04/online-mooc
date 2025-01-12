package com.tianji.trade.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "购物车条目信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartVO {
    @Schema(description = "购物车中条目id")
    private Long id;
    @Schema(description = "课程id")
    private Long courseId;
    @Schema(description = "课程名称")
    private String courseName;
    @Schema(description = "课程封面url")
    private String coverUrl;
    @Schema(description = "加入购物车时的课程价格，单位元")
    private Integer price;
    @Schema(description = "现在的课程价格，单位元")
    private Integer nowPrice;
    @Schema(description = "课程是否已经过期")
    private Boolean expired;
    @JsonIgnore
    @Schema(description = "课程有效期", hidden = true)
    private LocalDateTime courseValidDate;
}
