package com.tianji.learning.controller;/**
 * @author fsq
 * @date 2025/6/15 14:38
 */

import com.tianji.common.domain.R;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.ratelimiter.annotation.TjRateLimiter;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.ShareDetailDTO;
import com.tianji.learning.domain.dto.ShareLinkDTO;
import com.tianji.learning.service.IShareService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: fsq
 * @Date: 2025/6/15 14:38
 * @Version: 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/share")
public class ShareController {

    private final IShareService shareService;

    /**
     * 生成课程分享链接
     * @param courseId 课程ID
     * @return 分享链接DTO
     */
    @TjRateLimiter(permitsPerSecond = 1, timeout = 1)
    @PostMapping("/generate/{id}")
    @ApiOperation("生成课程分享链接")
    public ShareLinkDTO generateShareLink(@PathVariable("id") Long courseId) {
        Long userId = UserContext.getUser();
        ShareLinkDTO result = shareService.generateShareLink(userId, courseId);
        return result;
    }

    /**
     * 解析分享链接
     * @param shortCode 短码
     * @return 分享详情DTO
     */
    @GetMapping("/{code}")
    @ApiOperation("解析分享链接")
    public ShareDetailDTO parseShareLink(@PathVariable("code") String shortCode) {
        ShareDetailDTO result = shareService.parseShareLink(shortCode);
        if(result==null){
            throw new BadRequestException("链接不存在或已过期");
        }
        return result;
    }
}
