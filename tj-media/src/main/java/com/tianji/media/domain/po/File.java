package com.tianji.media.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.tianji.media.enums.FileStatus;
import com.tianji.media.enums.Platform;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 文件表，可以是普通文件、图片等
 * </p>
 *
 * @author 虎哥
 * @since 2022-07-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("file")
public class File implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，文件id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 文件在云端的唯一标示，例如：aaa.jpg
     */
    @TableField("`key`")
    private String key;

    /**
     * 文件上传时的名称
     */
    private String filename;

    /**
     * 请求id
     */
    private String requestId;

    /**
     * 文件内容哈希值（用于秒传）
     */
    private String fileHash;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型/MIME类型
     */
    private String fileType;

    /**
     * 存储桶名称
     */
    private String bucketName = "tianji";

    /**
     * 状态：1-待上传 2-上传中 3-已上传,未使用 4-已使用 5-上传失败
     */
    private FileStatus status;

    /**
     * 平台：1-腾讯 2-阿里 3-Minio
     */
    private Platform platform;


    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private Long creater;

    /**
     * 更新者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;

    /**
     * 部门id
     */
    private Long depId;

    /**
     * 逻辑删除，默认0
     */
    @TableLogic
    private Integer deleted;
}