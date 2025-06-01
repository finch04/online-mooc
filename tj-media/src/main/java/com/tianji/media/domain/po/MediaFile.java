package com.tianji.media.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 文件信息实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("media_file")
public class MediaFile {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String fileName;
    private String fileKey;
    private String fileHash;
    private Long fileSize;
    private String fileType;
    private String bucketName;
    private Integer uploadStatus; // 0-未上传 1-上传中 2-上传完成 3-上传失败
    
    private Date createTime;
    private Date updateTime;
    
    // 上传状态枚举
    public static class Status {
        public static final int NOT_UPLOADED = 0;
        public static final int UPLOADING = 1;
        public static final int UPLOADED = 2;
        public static final int FAILED = 3;
    }
}