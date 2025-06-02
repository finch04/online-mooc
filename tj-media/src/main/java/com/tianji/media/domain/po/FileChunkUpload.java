package com.tianji.media.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 分片上传记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("file_chunk_upload")
public class FileChunkUpload {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long fileId;
    private String uploadId;
    private Integer chunkCount;
    private Integer chunkSize;
    private String completedChunks; // JSON数组格式的已上传分片序号
    
    private Date createTime;
    private Date updateTime;
}