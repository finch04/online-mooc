package com.tianji.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.media.domain.dto.FileDTO;
import com.tianji.media.domain.po.File;
import com.tianji.media.domain.query.FileQuery;
import com.tianji.media.domain.vo.FileDetailVO;
import com.tianji.media.domain.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * <p>
 * 文件表，可以是普通文件、图片等 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2022-06-30
 */
public interface IFileService extends IService<File> {

    FileDTO uploadFile(MultipartFile file);

    FileDetailVO getFileInfo(Long id);

    Boolean checkFile(String fileMd5);

    Boolean checkChunk(String fileMd5, int chunk);

    Boolean uploadChunk(String fileMd5, int chunk, String absolutePath);

    FileDTO mergechunks(String fileMd5, int chunkTotal, String fileName);

    PageDTO<FileVO> queryFilePage(FileQuery query);

    void deleteFileById(Long id);
}
