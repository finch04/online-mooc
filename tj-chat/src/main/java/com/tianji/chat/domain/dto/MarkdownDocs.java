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
 * 用户上传的 Markdown 文档表
 * </p>
 *
 * @author lusy
 * @since 2025-05-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_markdown_docs")
public class MarkdownDocs  {

    /**
     * 主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 上传用户ID
     */
    private Long userId;

    /**
     * 上传时的原始文件名
     */
    private String fileName;

    /**
     * 整个 Markdown 文本内容
     */
    private String content;

    /**
     * 上传时间
     */
    private LocalDateTime createdAt;

    /**
     * 最近更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 文档切割等级
     */
    private Integer level;
}
