package com.tianji.data.domain;

import cn.idev.excel.EasyExcel;
import cn.idev.excel.FastExcel;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.influxdb.mapper.BusinessLogMapper;
import com.tianji.data.influxdb.service.IUrlLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import retrofit2.http.Url;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class LogExportServiceTest {

    @Autowired
    private BusinessLogMapper businessLogMapper;


    @Test
    public void exportBusinessLogsToExcel() {
        // 指定导出文件路径
        String filePath = "E:/idea/dev/tianji/tianji/code/test.xlsx"; // Windows系统路径示例
        // 如果是Linux/Mac系统，可使用: /tmp/business_logs_export.xlsx

        try {
            // 查询日志数据
            List<BusinessLog> businessLogs = businessLogMapper.exportLogs();

            if (businessLogs == null || businessLogs.isEmpty()) {
                System.out.println("没有可导出的日志数据");
                return;
            }

            // 创建父目录（如果不存在）
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            // 使用FastExcel写入数据到本地文件
            EasyExcel.write(filePath, BusinessLog.class)
                    .sheet("日志数据")
                    .doWrite(businessLogs);

            System.out.println("日志数据已成功导出到: " + filePath);

        } catch (Exception e) {
            System.err.println("导出日志失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

}