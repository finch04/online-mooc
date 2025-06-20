package com.tianji.data.influxdb.service.impl;

import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.influxdb.domain.UrlMetrics;
import com.tianji.data.influxdb.mapper.BusinessLogMapper;
import com.tianji.data.influxdb.service.IUrlAnalysisService;
import com.tianji.data.influxdb.tool.UrlRegexConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UrlAnalysisServiceImpl implements IUrlAnalysisService {
    
    private final BusinessLogMapper businessLogMapper;

    /**
     * 分析URL的访问指标
     * @param url 要分析的URL
     * @param beginTime 开始时间（格式：yyyy-MM-dd HH:mm:ss）
     * @param endTime 结束时间（格式：yyyy-MM-dd HH:mm:ss）
     * @return 包含访问指标的结果对象
     */
    public List<BusinessLog> analyzeUrl(String url, String beginTime, String endTime) {
        //示例 URL：/accounts/login
        // 调用Mapper方法执行查询
        return businessLogMapper.findLogsByUrl(url, beginTime, endTime);
    }

    /**
     * 分析URL的访问指标(模糊搜索url)---注意SQL注入---
     * @param url 要分析的URL
     * @param beginTime 开始时间（格式：yyyy-MM-dd HH:mm:ss）
     * @param endTime 结束时间（格式：yyyy-MM-dd HH:mm:ss）
     * @return 包含访问指标的结果对象
     */
    public List<BusinessLog> analyzeUrlByLike(String url, String beginTime, String endTime) {
        //示例URL:/login

        // 将URL转换为InfluxDB正则表达式格式：/\/accounts/
        // 1. 转义URL中的斜杠：/login → \/login
        String escapedUrl = url.replace("/", "\\\\/"); // 四个反斜杠表示一个\/

        // 2. 用/包裹整个正则：/\/login/
        String regex = "/" + escapedUrl + "/";
        // 调用Mapper方法执行查询
        return businessLogMapper.findLogsByUrlByLike(regex, beginTime, endTime);
    }
}