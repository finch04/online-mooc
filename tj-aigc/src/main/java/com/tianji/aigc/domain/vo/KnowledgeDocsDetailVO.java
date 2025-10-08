package com.tianji.aigc.domain.vo;

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
public class KnowledgeDocsDetailVO {

    /**
     * 主键，自增
     */
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
     * 文件扩展名
     */
    private String fileExtension;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文档文本内容
     */
    private String content;

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

    /**
     * 切割后的片段数量
     */
    private Integer segmentCount;

    /**
     * 状态(0-草稿 1-正常 2-已删除 3-处理中)
     */
    private Integer status;

    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 最近更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
