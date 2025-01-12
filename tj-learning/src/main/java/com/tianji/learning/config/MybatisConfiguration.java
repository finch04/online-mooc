package com.tianji.learning.config;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.tianji.learning.utils.TableInfoContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfiguration {

    @Bean
    public DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor() {
        // Map<String, TableNameHandler> map = new HashMap<>(1);
        // map.put("points_board", (sql, tableName) -> TableInfoContext.getInfo());
        return new DynamicTableNameInnerInterceptor((sql, tableName) -> {
            if(StrUtil.equals(tableName, "points_board")){
                return TableInfoContext.getInfo();
            }
            return tableName;
        });
    }
}
