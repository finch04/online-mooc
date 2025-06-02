package com.tianji.media.storage;

import com.tianji.media.domain.po.File;
import io.minio.errors.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

public interface IFileStorage {

    /**
     * 上传文件
     *
     * @param key         文件唯一标识（a.jpg)
     * @param inputStream 文件流
     * @return requestId
     */
    String uploadFile(String key, InputStream inputStream, long contentLength);

    /**
     * 获取文件访问路径
     *
     * @param key
     * @return
     */
    String getFileUrl(String key);


    /**
     * 下载文件
     *
     * @param key 文件唯一标识（a.jpg)
     * @return 文件流
     */
    InputStream downloadFile(String key);

    /**
     * 删除指定文件
     *
     * @param key 文件唯一标识（a.jpg)
     */
    void deleteFile(String key);

    /**
     * 删除指定文件
     *
     * @param keys 文件唯一标识（a.jpg)的集合
     */
    void deleteFiles(List<String> keys);

    /**
     * 根据路径检查文件是否存在（用于分片上传前检查或检查文件是否存在）
     * @param path
     * @return
     */
    InputStream checkFileByPath(String path);

    /**
     *  通过文件路径添加文件
     * @param localFilePath
     * @param objectName
     * @return
     */
    Boolean addFileByPath(String localFilePath,String objectName);

    /**
     * 分片上传 合并文件专用
     * @param fileMd5
     * @param chunkTotal
     * @param fileName
     * @return
     */
    File mergeChunks(String fileMd5, int chunkTotal, String fileName, String chunkFileFolderPath);

    /**
     * 清除分块文件
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     */
    void clearChunkFiles(String chunkFileFolderPath,int chunkTotal);
}
