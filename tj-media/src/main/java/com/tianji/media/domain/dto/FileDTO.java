package com.tianji.media.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "文件信息实体")
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {
    @Schema(description = "文件id", example = "1")
    private Long id;
    @Schema(description = "文件名称", example = "图片.jpg")
    private String filename;
    @Schema(description = "文件访问路径", example = "a.jpg")
    private String path;

    public static FileDTO of(Long id, String filename, String path) {
        return new FileDTO(id, filename, path);
    }
}
