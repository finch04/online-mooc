package com.tianji.media.storage.minio;/**
 * @author fsq
 * @date 2025/6/2 13:40
 */

import com.tianji.common.utils.StringUtils;
import com.tianji.media.config.MinioProperties;
import com.tianji.media.domain.po.Media;
import com.tianji.media.storage.IMediaStorage;
import com.tianji.media.storage.MediaUploadResult;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: fsq
 * @Date: 2025/6/2 13:40
 * @Version: 1.0
 */
/**
 * @Author: fsq
 * @Date: 2025/6/2 13:40
 * @Version: 1.0
 */
@Slf4j
public class MinioMediaStorage implements IMediaStorage {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final String bucketName;


    @Autowired
    public MinioMediaStorage(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
        bucketName = minioProperties.getBucketName();
    }

    @Override
    public String getUploadSignature() {
        return null;
    }

    @Override
    public String getPlaySignature(String fieldId, Long userId, Integer freeExpire) {
        return null;
    }

    @Override
    public MediaUploadResult uploadFile(String filename, InputStream inputStream, long contentLength) {
        MediaUploadResult result = new MediaUploadResult();
        try {
            // 上传文件到 MinIO
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .stream(inputStream, contentLength, -1)
                    .build();
            minioClient.putObject(putObjectArgs);

            // 设置上传结果
            result.setFileId(filename);
            result.setMediaUrl(String.format("%s/%s/%s",
                    minioProperties.getEndpoint(),  // MinIO 服务器地址
                    minioProperties.getBucketName(),
                    filename));
            result.setCoverUrl(null); // 假设没有封面 URL
            result.setRequestId(null); // 假设没有请求 ID
        } catch (Exception e) {
            // 处理异常
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void deleteFile(String fileId) {
        try {
            minioClient.removeObject(io.minio.RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileId)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteFiles(List<String> fileIds) {
        for (String fileId : fileIds) {
            deleteFile(fileId);
        }
    }

    @Override
    public List<Media> queryMediaInfos(String... fileIds) {
        return null;
    }

//    @Override
    public String getPreviewUrl(String fileName, String bucketName) {
        if (StringUtils.isNotBlank(fileName)) {
            bucketName = StringUtils.isNotBlank(bucketName) ? bucketName : minioProperties.getBucketName();
            try {
                minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(fileName).build());
                if (null != minioProperties.getPreviewExpiry()) {
                    return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(bucketName).object(fileName).expiry(minioProperties.getPreviewExpiry(), TimeUnit.HOURS).build());
                } else {
                    return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(bucketName).object(fileName).build());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
