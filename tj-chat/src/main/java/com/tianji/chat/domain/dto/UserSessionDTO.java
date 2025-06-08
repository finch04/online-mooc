package com.tianji.chat.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户与会话关联表
 * </p>
 *
 * @author [你的名字]
 * @since [具体日期]
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserSessionDTO {

    /**
     * 会话名称
     */
    private String name;

    /**
     * 会话标签
     */
    private String tag;

    /**
     * 用户id
     */
    private Long userId;

}