package com.tianji.media.domain.vo;

import com.tianji.media.enums.FileStatus;
import com.tianji.media.enums.Platform;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "文件详细信息")
public class FileDetailVO {
    @ApiModelProperty(value = "id", example = "1")
    private Long id;
    @ApiModelProperty(value = "文件名称", example = "文件名.avi")
    private String filename;
    @ApiModelProperty(value = "文件访问路径", example = "a.jpg")
    private String path;
    @ApiModelProperty(value = "文件hash值", example = "c8d586dc3022d27d256ec4450e4c8c36")
    private String fileHash;
    @ApiModelProperty(value = "文件类型", example = "video")
    private String fileType;
    @ApiModelProperty(value = "文件key")
    private String key;
    @ApiModelProperty(value = "文件存储平台", example = "MINIO")
    private Platform platform;
    @ApiModelProperty(value = "文件大小，单位字节", example = "1024")
    private Long fileSize;
    @ApiModelProperty(value = "被引用次数", example = "10")
    private Integer useTimes;
    @ApiModelProperty(value = "文件状态：1-上传中，2-已上传，3-处理中", example = "2")
    private Integer status;
    @ApiModelProperty(value = "创建时间", example = "2022-7-18 16:54:30")
    private LocalDateTime createTime;
    @ApiModelProperty(value = "创建者名称", example = "张三")
    private String creater;
}
