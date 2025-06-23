package com.tianji.data.domain;

import cn.idev.excel.EasyExcel;
import cn.idev.excel.FastExcel;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.mapper.BusinessLogMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;

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