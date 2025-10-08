package com.tianji.aigc.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户上传的文档表
 * </p>
 *
 * @author fsq
 * @since 2025-10-08
 */
@Data
public class KnowledgeDocsDTO {

    /**
     * 文档描述
     */
    private String description;
    /**
     * 标签列表
     */
    private String tags;

    /**
     * 文档切割等级
     */
    private Integer level;
}
