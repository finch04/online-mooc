package com.tianji.api.dto.leanring;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "学习课表进度信息")
public class LearningLessonDTO {
    @Schema(description = "课表id")
    private Long id;
    @Schema(description = "最近学习的小节id")
    private Long latestSectionId;
    @Schema(description = "学习过的小节的记录")
    private List<LearningRecordDTO> records;
}
