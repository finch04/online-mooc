package com.tianji.api.dto.course;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wusongsong
 * @since 2022/7/27 14:32
 * @version 1.0.0
 **/
@Data
@Schema(description = "课程简单信息")
public class CourseSimpleInfoDTO {
    @Schema(description = "课程id")
    private Long id;
    @Schema(description = "课程名称")
    private String name;
    @Schema(description = "封面url")
    private String coverUrl;
    @Schema(description = "价格")
    private Integer price;
    @Schema(description = "课程状态")
    private Integer status;
    @Schema(description = "是否是免费课程")
    private Boolean free;
    @Schema(description = "一级分类id")
    private Long firstCateId;
    @Schema(description = "二级分类id")
    private Long secondCateId;
    @Schema(description = "三级分类id")
    private Long thirdCateId;
    @Schema(description = "小节数量")
    private Integer sectionNum;
    @Schema(description = "课程购买有效期结束时间")
    private LocalDateTime purchaseEndTime;
    @Schema(description = "课程学习有效期，单位：月")
    private Integer validDuration;

    @JsonIgnore
    public List<Long> getCategoryIds() {
        return List.of(firstCateId, secondCateId, thirdCateId);
    }
}
