package com.tianji.message.domain.dto;

import com.tianji.common.domain.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * <p>
 * 公告消息模板
 * </p>
 *
 * @author 虎哥
 * @since 2022-08-19
 */
@Schema(description = "通知任务")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NoticeTaskDTO extends BaseDTO {

    @Schema(description = "任务id，新增时无需填写")
    private Long id;
    @Schema(description = "任务要发送的通知模板id")
    private Long templateId;
    @Schema(description = "任务名称")
    private String name;
    @Schema(description = "true-通知所有人;false-通知部分人。默认false")
    private Boolean partial;
    @Schema(description = "任务预期执行时间")
    private LocalDateTime pushTime;
    @Schema(description = "任务重复执行次数上限，0则不重复")
    private Integer maxTimes;
    @Schema(description = "任务重复执行时间间隔，单位是分钟")
    private Long interval;
    @Schema(description = "任务失效时间")
    private LocalDateTime expireTime;
    @Schema(description = "任务是否已经完成。默认false")
    private Boolean finished;
}
