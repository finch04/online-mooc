package com.tianji.media.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.course.MediaQuoteDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.CommonException;
import com.tianji.common.exceptions.DbException;
import com.tianji.common.utils.AssertUtils;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.media.config.PlatformProperties;
import com.tianji.media.domain.dto.FileDTO;
import com.tianji.media.domain.po.File;
import com.tianji.media.domain.po.FileChunkUpload;
import com.tianji.media.domain.po.Media;
import com.tianji.media.domain.query.FileQuery;
import com.tianji.media.domain.vo.FileDetailVO;
import com.tianji.media.domain.vo.FileVO;
import com.tianji.media.domain.vo.MediaVO;
import com.tianji.media.enums.FileErrorInfo;
import com.tianji.media.enums.FileStatus;
import com.tianji.media.mapper.FileChunkUploadMapper;
import com.tianji.media.mapper.FileMapper;
import com.tianji.media.service.IFileService;
import com.tianji.media.storage.IFileStorage;
import com.tianji.media.utils.FileUtils;
import io.minio.ComposeObjectArgs;
import io.minio.ComposeSource;
import io.minio.GetObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tianji.media.constants.FileErrorInfo.USER_NOT_EXISTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements IFileService {

    private final IFileStorage fileStorage;
    private final PlatformProperties properties;
    private final UserClient userClient;
    private final FileChunkUploadMapper fileChunkUploadMapper;


    @Override
    public PageDTO<FileVO> queryFilePage(FileQuery query) {
        // 1.分页条件
        Page<File> page = new Page<>(query.getPageNo(), query.getPageSize());
        if(StringUtils.isNotBlank(query.getSortBy())){
            page.addOrder(new OrderItem(query.getSortBy(), query.getIsAsc()));
        }
        // 2.分页搜索
        lambdaQuery()
                .like(StringUtils.isNotBlank(query.getName()), File::getFilename, query.getName())
                .page(page);
        // 3.解析数据
        List<File> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        List<Long> ids = new ArrayList<>(records.size());
        Set<Long> createIds = new HashSet<>();
        for (File m : records) {
            ids.add(m.getId());
            createIds.add(m.getCreater());
        }
        createIds.remove(0L);
        // 4.TODO 查询引用次数
//        List<MediaQuoteDTO> mediaQuoteDTOS = courseClient.mediaUserInfo(ids);
//        AssertUtils.isNotEmpty(mediaQuoteDTOS, com.tianji.media.constants.FileErrorInfo.MEDIA_QUOTE_NOT_EXISTS);
//        Map<Long, Integer> quoteMap = mediaQuoteDTOS
//                .stream()
//                .collect(Collectors.toMap(MediaQuoteDTO::getMediaId, MediaQuoteDTO::getQuoteNum));

        // 5.查询创建者信息
        Map<Long, String> userMap = null;
        if(CollUtils.isNotEmpty(createIds)) {
            List<UserDTO> users = userClient.queryUserByIds(createIds);
            AssertUtils.isNotEmpty(users, USER_NOT_EXISTS);
            userMap = users.stream().collect(Collectors.toMap(UserDTO::getId, UserDTO::getName));
        }
        // 6.数据转换
        List<FileVO> list = new ArrayList<>(records.size());
        for (File m : records) {
            FileVO v = BeanUtils.toBean(m, FileVO.class);
            v.setStatus(m.getStatus().getValue());
            //TODO 引用次数
            v.setUseTimes(0);
//            v.setPath( fileStorage.getFileUrl(m.getKey()));
            if(userMap != null) {
                v.setCreater(userMap.get(m.getCreater()));
            }
            list.add(v);
        }
        return new PageDTO<>(page.getTotal(), page.getPages(), list);
    }


    @Override
    public FileDetailVO getFileInfo(Long id) {
        File file = getById(id);
        if (file == null) {
            return null;
        }
        FileDetailVO fileDetailVO = BeanUtils.copyProperties(file, FileDetailVO.class);
        fileDetailVO.setPath(fileStorage.getFileUrl(file.getKey()));
        fileDetailVO.setCreater(userClient.queryUserById(file.getCreater()).getName());
        fileDetailVO.setStatus(file.getStatus().getValue());
        //TODO 引用次数
        fileDetailVO.setUseTimes(0);
        return fileDetailVO;
    }


    @Override
    public FileDTO uploadFile(MultipartFile file) {
        // 计算文件哈希值
        String fileHash = FileUtils.getFileHash(file);
        // 检查是否可以秒传
        File existingFile = checkFileExistsByHash(fileHash);
        if (existingFile != null) {
            log.info("文件秒传成功！文件Key：{}",existingFile.getKey());
            return FileDTO.of(existingFile.getId(), existingFile.getFilename(), fileStorage.getFileUrl(existingFile.getKey()));
        }

        // 正常上传流程
        // 1.获取文件名称
        String originalFilename = file.getOriginalFilename();
        // 2.生成新文件名
        String filename = generateNewFileName(originalFilename);
        // 3.获取文件流
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            throw new CommonException("文件读取异常", e);
        }
        // 4.上传文件
        String requestId = fileStorage.uploadFile(filename, inputStream, file.getSize());
        // 5.写入数据库
        File fileInfo = null;
        try {
            fileInfo = new File();
            fileInfo.setFilename(originalFilename);
            fileInfo.setKey(filename);
            fileInfo.setStatus(FileStatus.UPLOADED);
            fileInfo.setRequestId(requestId);
            fileInfo.setPlatform(properties.getFile());

            fileInfo.setFileHash(FileUtils.getFileHash(file));
            fileInfo.setFileSize(FileUtils.getFileSize(file));
            fileInfo.setFileType(FileUtils.getFileType(file));

            save(fileInfo);
        } catch (Exception e) {
            log.error("文件信息保存异常", e);
            fileStorage.deleteFile(filename);
            throw new DbException(FileErrorInfo.Msg.FILE_UPLOAD_ERROR);
        }
        // 6.返回
        FileDTO fileDTO = new FileDTO();
        fileDTO.setId(fileInfo.getId());
        fileDTO.setPath(fileStorage.getFileUrl(filename));
        fileDTO.setFilename(originalFilename);
        return fileDTO;
    }


    @Override
    public Boolean checkFile(String fileMd5) {
        //查询文件信息
        File file = checkFileExistsByHash(fileMd5);
        if ( file!= null){
            //桶
            String bucket = file.getBucketName();
            //存储目录
            String filePath = file.getKey();
            //文件流
            InputStream stream = null;
            try {
                stream = fileStorage.checkFileByPath(filePath);
                if (stream != null) {
                    //文件已存在
                    return true;
                }
            } catch (Exception e) {
                log.error( "文件检查异常", e);
            }
        }
        return false;
    }

    @Override
    public Boolean checkChunk(String fileMd5, int chunkIndex) {
        //得到分块文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;

        //文件流
        InputStream fileInputStream = null;
        try {
            fileInputStream = fileStorage.checkFileByPath(chunkFilePath);
            if (fileInputStream != null) {
                //分块已存在
                return true;
            }
        } catch (Exception e) {
        }
        //分块未存在
        return false;
    }

    @Override
    public Boolean uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        //得到分块文件的目录路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunk;
        //将文件存储至minIO
        boolean b = fileStorage.addFileByPath(localChunkFilePath, chunkFilePath);
        if (!b) {
            log.error("上传分块文件失败:{}", chunkFilePath);
            return false;
        }
        log.debug("上传分块文件成功:{}",chunkFilePath);
        return true;
    }


    @Override
    public FileDTO mergechunks(String fileMd5, int chunkTotal, String fileName) {
        //分块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File fileInfo = null;
        try {
             fileInfo = fileStorage.mergeChunks(fileMd5, chunkTotal, fileName,chunkFileFolderPath);
             if (fileInfo==null) {
                throw new RuntimeException("合并文件失败");
            }
        }catch (Exception e) {
            throw new  CommonException("合并文件失败", e);
        }
        //==============将文件信息入库============
        try {
            save(fileInfo);
        } catch (Exception e) {
            log.error("文件信息保存异常", e);
            fileStorage.deleteFile(fileName);
            throw new DbException(FileErrorInfo.Msg.FILE_UPLOAD_ERROR);
        }

        //==========清理分块文件=========
        fileStorage.clearChunkFiles(chunkFileFolderPath,chunkTotal);

        return FileDTO.of(fileInfo.getId(), fileInfo.getFilename(), fileStorage.getFileUrl(fileInfo.getKey()));
    }


    private String generateNewFileName(String originalFilename) {
        // 1.获取后缀
        String suffix = StringUtils.subAfter(originalFilename, ".", true);
        // 2.生成新文件名
        return UUID.randomUUID().toString(true) + "." + suffix;
    }


    private File checkFileExistsByHash(String fileHash) {
        return lambdaQuery().eq(File::getFileHash, fileHash).one();
    }

    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * 得到合并后的文件的地址
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }



}