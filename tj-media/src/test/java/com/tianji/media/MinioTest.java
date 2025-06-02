package com.tianji.media;/**
 * @author fsq
 * @date 2025/6/1 18:23
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianji.media.domain.po.File;
import com.tianji.media.service.IFileService;
import com.tianji.media.storage.minio.MinioFileStorage;
import com.tianji.media.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/6/1 18:23
 * @Version: 1.0
 */
@SpringBootTest
public class MinioTest {

     @Autowired
     private MinioFileStorage minioFileStorage;
     @Autowired
     private IFileService fileService;

    @Test
     public void PreviewTest() {
        String tianji = minioFileStorage.getPreviewUrl("00deb122e45b4a0e94454fd0a9f6aa79.jpg", "tianji");
         System.out.println(tianji);
    }

    @Test
    public void getUrlTest() {
        String tianji = minioFileStorage.getFileUrl("00deb122e45b4a0e94454fd0a9f6aa79.jpg");
        System.out.println(tianji);
    }

    public void putFileInfo(){
        List<File> files = fileService.getBaseMapper().selectList(new LambdaQueryWrapper<>());
        for (File file : files) {
            InputStream inputStream = minioFileStorage.downloadFile(file.getKey());
//
//            file.setFileHash(FileUtils.getFileHash(file));
//            file.setFileSize(FileUtils.getFileSize(file));
//            file.setFileType(FileUtils.getFileType(file));
            fileService.getBaseMapper().updateById(file);
        }
    }

    @Test
    public void deleteFile(){
        List<String> list = new ArrayList<>();
        list.add("00deb122e45b4a0e94454fd0a9f6aa79.jpg");
        list.add("0d323a282d7e4d99a5b367550ad2e542.jpg");
        list.add("fe8983b6718e43cda81d4e715e2c205b.jpg");
        list.add("e/9/e9572c36b46c18342ae8b48edcb01649/e9572c36b46c18342ae8b48edcb01649.jpg");
        minioFileStorage.deleteFiles(list);
    }


    @Test
    public void updateFile(){
        List<File> files = fileService.getBaseMapper().selectList(null);
         for (File file : files) {
             file.setUseTimes(0);
             fileService.updateById(file);
        }

    }

}
