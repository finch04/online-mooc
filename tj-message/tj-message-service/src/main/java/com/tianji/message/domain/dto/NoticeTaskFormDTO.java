package com.tianji.message.domain.dto;

import com.tianji.common.utils.DateUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 公告消息模板
 * </p>
 *
 * @author 虎哥
 * @since 2022-08-19
 */
@Data
@ApiModel(description = "通知任务的表单实体")
public class NoticeTaskFormDTO {

    @ApiModelProperty("任务id，新增时无需填写")
    private Long id;
    @ApiModelProperty("任务要发送的通知模板id")
    private Long templateId;
    @ApiModelProperty("任务名称")
    private String name;
    @ApiModelProperty("true-通知所有人;false-通知部分人。默认false")
    private Boolean partial;
    @ApiModelProperty("如果是通知部分人，通知部分人的用户id")
    private List<Long> userIds;
    @ApiModelProperty("任务预期执行时间，如果为null，或者小于等于当前时间，则立刻执行")
    @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime pushTime;
    @ApiModelProperty("任务重复执行次数上限，0则不重复")
    private Integer maxTimes;
    @ApiModelProperty("任务重复执行时间间隔，单位是分钟")
    private Long interval;
    @ApiModelProperty("任务失效时间")
    @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime expireTime;
}
