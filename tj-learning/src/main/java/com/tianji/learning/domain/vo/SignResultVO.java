package com.tianji.learning.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "签到结果")
public class SignResultVO {
    @Schema(description = "连续签到天数")
    private Integer signDays;
    @Schema(description = "签到得分")
    private Integer signPoints = 1;
    @Schema(description = "连续签到奖励积分，连续签到超过7天以上才有奖励")
    private Integer rewardPoints;

    @JsonIgnore
    public int totalPoints() {
        return signPoints + rewardPoints;
    }
}
