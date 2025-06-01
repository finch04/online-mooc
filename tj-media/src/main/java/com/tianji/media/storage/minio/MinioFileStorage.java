package com.tianji.media.storage.minio;

import com.tianji.common.utils.StringUtils;
import com.tianji.media.config.MinioProperties;
import com.tianji.media.storage.IFileStorage;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Override
    // 添加获取文件URL的方法
    public String getFileUrl(String key) {
        // 直接拼接固定 URL，无需签名
        return String.format("%s/%s/%s",
                minioProperties.getEndpoint(),  // MinIO 服务器地址
                minioProperties.getBucketName(),
                key);
    }

    public String getPreviewUrl(String fileName, String bucketName) {
        if (StringUtils.isNotBlank(fileName)) {
            bucketName = StringUtils.isNotBlank(bucketName) ? bucketName :minioProperties.getBucketName();
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