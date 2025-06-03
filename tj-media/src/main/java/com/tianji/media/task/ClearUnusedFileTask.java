package com.tianji.media.task;/**
 * @author fsq
 * @date 2025/6/3 12:47
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.vod.v20180717.models.ConfirmEventsRequest;
import com.tencentcloudapi.vod.v20180717.models.EventContent;
import com.tencentcloudapi.vod.v20180717.models.PullEventsRequest;
import com.tencentcloudapi.vod.v20180717.models.PullEventsResponse;
import com.tianji.media.domain.po.File;
import com.tianji.media.enums.FileStatus;
import com.tianji.media.service.IFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/6/3 12:47
 * @Version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClearUnusedFileTask {

    private final IFileService fileService;

    /**
     *  定时清理过期文件
     */
//    @Scheduled(fixedDelay = 30000)
    public void clearUnusedFile() {
        LambdaQueryWrapper<File> queryWrapper =  new LambdaQueryWrapper<>();
         queryWrapper.eq(File::getStatus, FileStatus.UPLOADED)
                 .eq(File::getUseTimes, 0)
                .lt(File::getUpdateTime, System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7);
        List<File> files = fileService.getBaseMapper().selectList(queryWrapper);
        for (File file : files){
             fileService.deleteFileById(file.getId());
        }
        log.info("清理过期文件完成，共清理{}个文件", files.size());
    }
}
