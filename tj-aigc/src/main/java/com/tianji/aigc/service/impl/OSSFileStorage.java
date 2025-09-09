package com.tianji.aigc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import com.tianji.aigc.config.AliYunProperties;
import com.tianji.aigc.service.FileStorage;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.CommonException;
import com.tianji.common.utils.AssertUtils;
import com.tianji.common.utils.CollUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

import static com.tianji.aigc.constants.FileErrorInfo.Msg.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OSSFileStorage implements FileStorage {

    private final OSS ossClient;
    private final AliYunProperties aliYunProperties;

    @Override
    public String uploadFile(String key, InputStream inputStream, long contentLength) {
        // 1.数据校验
        var bucketName = this.aliYunProperties.getOss().getBucket();
        AssertUtils.isNotBlank(bucketName, BUCKET_NAME_IS_NULL);
        AssertUtils.isNotBlank(key, FILE_KEY_IS_NULL);
        AssertUtils.isNotNull(inputStream);
        try {
            // 2.上传文件元数据处理
            ObjectMetadata objectMeta = new ObjectMetadata();
            objectMeta.setContentLength(contentLength);
            // 3.请求参数
            PutObjectRequest request = new PutObjectRequest(bucketName, key, inputStream, objectMeta);
            // 4.上传
            PutObjectResult result = ossClient.putObject(request);
            result.getResponse();
            return StrUtil.format("https://{}.{}/{}", bucketName, aliYunProperties.getOss().getEndpoint(), key);
        } catch (Exception e) {
            log.error("上传文件[{}]失败 ", key, e);
            throw new CommonException("上传文件失败!", e);
        }
    }

    @Override
    public InputStream downloadFile(String key) {
        // 1.数据校验
        var bucketName = this.aliYunProperties.getOss().getBucket();
        AssertUtils.isNotBlank(bucketName, BUCKET_NAME_IS_NULL);
        AssertUtils.isNotBlank(key, FILE_KEY_IS_NULL);
        try {
            GetObjectRequest request = new GetObjectRequest(bucketName, key);
            return ossClient.getObject(request).getObjectContent();
        } catch (Exception e) {
            log.error("下载文件[{}]时发生异常：", key, e);
            throw new CommonException("文件下载异常。", e);
        }
    }

    @Override
    public void deleteFile(String key) {
        // 1.数据校验
        var bucketName = this.aliYunProperties.getOss().getBucket();
        AssertUtils.isNotBlank(bucketName, BUCKET_NAME_IS_NULL);
        AssertUtils.isNotBlank(key, FILE_KEY_IS_NULL);
        try {
            // 2.删除
            ossClient.deleteObject(bucketName, key);
        } catch (Exception e) {
            log.error("删除文件[{}]时发生异常：", key, e);
            throw new CommonException("删除异常。", e);
        }
    }

    @Override
    public void deleteFiles(List<String> keys) {
        // 1.数据校验
        if (CollUtils.isEmpty(keys)) {
            return;
        }
        var bucketName = this.aliYunProperties.getOss().getBucket();
        AssertUtils.isNotBlank(bucketName, BUCKET_NAME_IS_NULL);
        if (keys.size() > 1000) {
            throw new BadRequestException(FILE_KEY_TOO_MANY);
        }
        // 2.准备request
        DeleteObjectsRequest request = new DeleteObjectsRequest(bucketName).withKeys(keys);
        try {
            // 3.删除
            ossClient.deleteObjects(request);
        } catch (Exception e) {
            log.error("批量删除文件[{}]时发生异常：", keys, e);
            throw new CommonException("删除异常。", e);
        }
    }
}
