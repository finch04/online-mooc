package com.tianji.message.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户通知记录
 * </p>
 *
 * @author 虎哥
 * @since 2022-08-20
 */
@Schema(description = "用户收件箱消息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInboxDTO{

    @Schema(description = "收件箱消息id")
    private Long id;

    @Schema(description = "通知类型：0-系统通知，1-笔记通知，2-问答通知，3-其它通知，4-私信")
    private Integer type;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知或私信内容")
    private String content;

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "消息发送者id")
    private Long publisher;

    @Schema(description = "收件箱消息id")
    private LocalDateTime pushTime;
}
