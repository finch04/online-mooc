package com.itheima;


import com.tianji.api.cache.CategoryCache;
import com.tianji.learning.LearningApplication;
import io.swagger.annotations.ApiModelProperty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = LearningApplication.class)
public class CategoryCacheTest {

    @Autowired
    CategoryCache categoryCache;

    @Test
    public void test(){
        String categoryNames = categoryCache.getCategoryNames(List.of(1001L, 2001L, 3003L));
        System.out.println(categoryNames);
    }

}
