package com.tianji.media.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {

    /**
     * 获取文件大小（字节）
     * @param file 上传的文件
     * @return 文件大小，若文件为空或出错返回-1
     */
    public static long getFileSize(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return -1;
        }
        return file.getSize();
    }

    /**
     * 获取文件MIME类型
     * @param file 上传的文件
     * @return MIME类型字符串，无法判断时返回null
     */
    public static String getFileType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String contentType = file.getContentType();
        return contentType != null && !contentType.isEmpty() ? contentType : null;
    }

    /**
     * 使用MD5算法计算文件哈希值
     * @param file 上传的文件
     * @return 哈希值字符串（十六进制），出错时返回null
     */
    public static String getFileHash(MultipartFile file) {
        return getFileHash(file, "MD5");
    }

    /**
     * 计算文件的哈希值
     * @param file 上传的文件
     * @param algorithm 哈希算法，如"MD5","SHA-256"等
     * @return 哈希值字符串（十六进制），出错时返回null
     */
    public static String getFileHash(MultipartFile file, String algorithm) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try (InputStream is = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            return null;
        }
    }



    /**
     * 获取文件大小（字节）
     * @param file 文件对象
     * @return 文件大小，若文件不存在或出错返回-1
     */
    public static long getFileSize(File file) {
        if (file == null || !file.exists()) {
            return -1;
        }
        return file.length();
    }

    /**
     * 获取文件MIME类型
     * @param file 文件对象
     * @return MIME类型字符串，无法判断时返回null
     */
    public static String getFileType(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            return Files.probeContentType(file.toPath());
        } catch (IOException e) {
            return null;
        }
    }

    public static String getFileHash(File file) {
        return getFileHash(file,"MD5");
    }

    /**
     * 计算文件的哈希值
     * @param file 文件对象
     * @param algorithm 哈希算法，如"MD5","SHA-256"等
     * @return 哈希值字符串（十六进制），出错时返回null
     */
    public static String getFileHash(File file, String algorithm) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            return null;
        }
    }
}