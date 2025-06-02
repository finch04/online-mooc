package com.tianji.media.storage.minio;

import com.tianji.common.utils.StringUtils;
import com.tianji.media.config.MinioProperties;
import com.tianji.media.enums.FileStatus;
import com.tianji.media.enums.Platform;
import com.tianji.media.storage.IFileStorage;
import com.tianji.media.utils.FileUtils;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class MinioFileStorage implements IFileStorage {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Autowired
    public MinioFileStorage(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    @Override
    public String uploadFile(String key, InputStream inputStream, long contentLength) {
        try {
            // 确保桶存在
            createBucketIfNotExists();

            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(key)
                    .stream(inputStream, contentLength, -1)
                    .build();

            ObjectWriteResponse response = minioClient.putObject(args);
            return response.object(); // 或者 response.etag() 作为 requestId
        } catch (ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | IOException |
                 NoSuchAlgorithmException | ServerException | XmlParserException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public InputStream downloadFile(String key) {
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(key)
                    .build();

            return minioClient.getObject(args);
        } catch (ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | IOException |
                 NoSuchAlgorithmException | ServerException | XmlParserException e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败", e);
        }
    }

    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileByName(String bucket,String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile=File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public void deleteFile(String key) {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(key)
                    .build();

            minioClient.removeObject(args);
        } catch (ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | IOException |
                 NoSuchAlgorithmException | ServerException | XmlParserException e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件删除失败", e);
        }
    }

    @Override
    public void deleteFiles(List<String> keys) {
        try {
            // 转换为RemoveObject列表
            List<DeleteObject> objects = keys.stream()
                    .map(DeleteObject::new)
                    .collect(Collectors.toList());

            RemoveObjectsArgs args = RemoveObjectsArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .objects(objects)
                    .build();

            // 批量删除
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(args);

            // 检查是否有删除失败的情况
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.error("删除文件失败: {}", error.objectName(), error.message());
            }
        } catch (Exception e) {
            log.error("批量删除文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量删除文件失败", e);
        }
    }


    @Override
    // 添加获取文件URL的方法
    public String getFileUrl(String key) {
        // 直接拼接固定 URL，无需签名
        return String.format("%s/%s/%s",
                minioProperties.getEndpoint(),  // MinIO 服务器地址
                minioProperties.getBucketName(),
                key);
    }

    @Override
    public InputStream checkFileByPath(String path) {
        try {
            return minioClient.getObject(
                     GetObjectArgs.builder()
                             .bucket(minioProperties.getBucketName())
                             .object(path)
                             .build());
        } catch (Exception e) {
            log.error("检查文件路径是否存在出错！");
        }
        return null;
    }

    @Override
    public Boolean addFileByPath(String localFilePath, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())//桶
                    .filename(localFilePath) //指定本地文件路径
                    .object(objectName)//对象名 放在子目录下
                    .build();
            //上传文件
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("上传文件到minio成功,objectName:{}",objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错,objectName:{},错误信息:{}",objectName,e.getMessage());
        }
        return false;
    }

    @Override
    public com.tianji.media.domain.po.File mergeChunks(String fileMd5, int chunkTotal, String fileName, String chunkFileFolderPath) {
        //找到所有的分块文件
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i).limit(chunkTotal).map(i -> ComposeSource.builder().bucket(minioProperties.getBucketName()).object(chunkFileFolderPath + i).build()).collect(Collectors.toList());
        //扩展名
        String extension = fileName.substring(fileName.lastIndexOf("."));
        //合并后文件的objectname
        String objectName = getFilePathByMd5(fileMd5, extension);
        //指定合并后的objectName等信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
                .object(objectName)//合并后的文件的objectname
                .sources(sources)//指定源文件
                .build();
        //===========合并文件============
        //报错size 1048576 must be greater than 5242880，minio默认的分块文件大小为5M
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错,bucket:{},objectName:{},错误信息:{}",minioProperties.getBucketName(),objectName,e.getMessage());
        }

        //===========校验合并后的和源文件是否一致，视频上传才成功===========
        //先下载合并后的文件
        File file = downloadFileByName(minioProperties.getBucketName(), objectName);
        try(FileInputStream fileInputStream = new FileInputStream(file)){
            //计算合并后文件的md5
            String mergeFile_md5 = DigestUtils.md5Hex(fileInputStream);
            //比较原始md5和合并后文件的md5
            if(!fileMd5.equals(mergeFile_md5)){
                log.error("校验合并文件md5值不一致,原始文件:{},合并文件:{}",fileMd5,mergeFile_md5);
            throw new RuntimeException("校验合并文件md5值不一致");
            }
            com.tianji.media.domain.po.File fileInfo = null;
                fileInfo = new com.tianji.media.domain.po.File();
                fileInfo.setFilename(fileName);
                fileInfo.setKey(objectName);
                fileInfo.setFileType( FileUtils.getFileType(file));
                fileInfo.setStatus(FileStatus.UPLOADED);
                fileInfo.setPlatform(Platform.MINIO);
                fileInfo.setFileHash(FileUtils.getFileHash(file));
                fileInfo.setFileSize(FileUtils.getFileSize(file));
                fileInfo.setFileType(FileUtils.getFileType(file));
                return  fileInfo;
        }catch (Exception e) {
        }
        return null;
    }

    @Override
    public void clearChunkFiles(String chunkFileFolderPath, int chunkTotal) {
        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(minioProperties.getBucketName()).objects(deleteObjects).build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(r->{
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清除分块文件失败,objectname:{}",deleteError.objectName(),e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清楚分块文件失败,chunkFileFolderPath:{}",chunkFileFolderPath,e);
        }
    }

    /**
     * 检查并创建桶（如果不存在）
     */
    private void createBucketIfNotExists() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .build());

            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .build());
            }
        } catch (Exception e) {
            log.error("检查/创建桶失败: {}", e.getMessage(), e);
            throw new RuntimeException("检查/创建桶失败", e);
        }
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