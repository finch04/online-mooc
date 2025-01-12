package com.tianji.message.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 用户通知记录
 * </p>
 *
 * @author 虎哥
 * @since 2022-08-20
 */
@Schema(description = "用户私信表单实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInboxFormDTO {

    @Schema(description = "目标用户id")
    private Long userId;

    @Schema(description = "私信内容")
    private String content;
}