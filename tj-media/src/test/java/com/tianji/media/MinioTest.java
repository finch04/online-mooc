package com.tianji.media;/**
 * @author fsq
 * @date 2025/6/1 18:23
 */

import com.tianji.media.storage.minio.MinioFileStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: fsq
 * @Date: 2025/6/1 18:23
 * @Version: 1.0
 */
@SpringBootTest
public class MinioTest {

     @Autowired
     private MinioFileStorage minioFileStorage;

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
}
