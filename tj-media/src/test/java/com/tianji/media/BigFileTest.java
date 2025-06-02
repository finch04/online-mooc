package com.tianji.media;

import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BigFileTest {

    @Autowired
    MinioClient minioClient;


    //测试文件分块方法
    @Test
    public void testChunk() throws IOException {
        // 指定源文件路径
        File sourceFile = new File("d:/develop/bigfile_test/nacos.mp4");
        // 指定分块文件存放的路径
        String chunkPath = "d:/develop/bigfile_test/chunk/";
        // 创建存储分块文件的文件夹对象
        File chunkFolder = new File(chunkPath);
        // 检查文件夹是否存在，如果不存在则创建
        if (!chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }
        // 定义每个分块的大小为 1MB
        long chunkSize = 1024 * 1024 * 1;
        // 计算分块的数量，使用 Math.ceil 确保分块数足够容纳整个文件
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        System.out.println("分块总数：" + chunkNum);
        // 定义缓冲区大小为 1KB
        byte[] b = new byte[1024];
        // 使用 RandomAccessFile 以只读模式打开源文件
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        // 循环进行分块操作
        for (int i = 0; i < chunkNum; i++) {
            // 创建分块文件对象
            File file = new File(chunkPath + i);
            // 如果分块文件已存在，则删除
            if (file.exists()) {
                file.delete();
            }
            // 创建新的分块文件
            boolean newFile = file.createNewFile();
            if (newFile) {
                // 以读写模式打开分块文件
                RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
                int len = -1;
                // 从源文件读取数据到缓冲区，并写入分块文件
                while ((len = raf_read.read(b)) != -1) {
                    raf_write.write(b, 0, len);
                    // 当分块文件达到指定大小后，停止写入
                    if (file.length() >= chunkSize) {
                        break;
                    }
                }
                // 关闭分块文件的写入流
                raf_write.close();
                System.out.println("完成分块" + i);
            }
        }
        // 关闭源文件的读取流
        raf_read.close();
    }


    //测试文件合并方法
    @Test
    public void testMerge() throws IOException {
        // 指定分块文件所在的文件夹
        File chunkFolder = new File("d:/develop/bigfile_test/chunk/");
        // 指定原始文件路径
        File originalFile = new File("d:/develop/bigfile_test/nacos.mp4");
        // 指定合并后的文件路径
        File mergeFile = new File("d:/develop/bigfile_test/nacos01.mp4");
        // 如果合并后的文件已存在，则删除
        if (mergeFile.exists()) {
            mergeFile.delete();
        }
        // 创建新的合并文件
        mergeFile.createNewFile();
        // 以读写模式打开合并文件
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        // 将文件指针移动到文件开头
        raf_write.seek(0);
        // 定义缓冲区大小为 1KB
        byte[] b = new byte[1024];
        // 获取分块文件列表
        File[] fileArray = chunkFolder.listFiles();
        // 将分块文件数组转换为列表，方便排序
        List<File> fileList = Arrays.asList(fileArray);
        // 对分块文件列表按文件名从小到大排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });
        // 循环合并分块文件
        for (File chunkFile : fileList) {
            // 以读写模式打开分块文件
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            // 从分块文件读取数据到缓冲区，并写入合并文件
            while ((len = raf_read.read(b)) != -1) {
                raf_write.write(b, 0, len);
            }
            // 关闭分块文件的读取流
            raf_read.close();
        }
        // 关闭合并文件的写入流
        raf_write.close();

        // 校验文件
        try (
                // 打开原始文件的输入流
                FileInputStream fileInputStream = new FileInputStream(originalFile);
                // 打开合并文件的输入流
                FileInputStream mergeFileStream = new FileInputStream(mergeFile);
        ) {
            // 计算原始文件的 MD5 值
            String originalMd5 = DigestUtils.md5Hex(fileInputStream);
            // 计算合并文件的 MD5 值
            String mergeFileMd5 = DigestUtils.md5Hex(mergeFileStream);
            // 比较两个 MD5 值，判断合并是否成功
            if (originalMd5.equals(mergeFileMd5)) {
                System.out.println("合并文件成功");
            } else {
                System.out.println("合并文件失败");
            }
        }
    }


}