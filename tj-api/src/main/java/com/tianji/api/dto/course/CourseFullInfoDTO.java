package com.tianji.api.dto.course;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程信息
 *
 * @author wusongsong
 * @since 2022/8/5 16:54
 * @version 1.0.0
 **/
@Data
@Schema(description = "课程详细信息，包含课程、目录、教师")
public class CourseFullInfoDTO {
    @Schema(description = "课程id")
    private Long id;
    @Schema(description = "课程名称")
    private String name;
    @Schema(description = "封面链接")
    private String coverUrl;
    @Schema(description = "价格")
    private Integer price;
    @Schema(description = "一级课程分类id")
    private Long firstCateId;
    @Schema(description = "二级课程分类id")
    private Long secondCateId;
    @Schema(description = "三级课程分类id")
    private Long thirdCateId;
    @Schema(description = "课程总节数")
    private Integer sectionNum;
    @Schema(description = "课程购买有效期结束时间")
    private LocalDateTime purchaseEndTime;
    @Schema(description = "课程学习有效期")
    private Integer validDuration;
    @Schema(description = "课程章信息")
    private List<CatalogueDTO> chapters;
    @Schema(description = "老师列表")
    private List<Long> teacherIds;

    @JsonIgnore
    public List<Long> getCategoryIds() {
        return List.of(firstCateId, secondCateId, thirdCateId);
    }
}
