package com.tianji.live.controller;/**
 * @author fsq
 * @date 2025/6/5 16:03
 */

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: fsq
 * @Date: 2025/6/5 16:03
 * @Version: 1.0
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "直播管理")
@RequestMapping("/live")
public class LiveController {
//    @GetMapping("/{streamId}")
//    public void stream(@PathVariable String streamId, HttpServletResponse response) throws IOException {
//        // 获取直播流数据
//
//        // 设置响应头，告诉浏览器是流媒体数据
//        response.setContentType("application/octet-stream");
//
//        // 通过Javacv和Ffmpeg将直播流数据写入响应输出流
//        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("rtmp://example.com/live/" + streamId);
//        grabber.start();
//
//        Frame frame;
//        while ((frame = grabber.grabFrame()) != null) {
//            response.getOutputStream().write(frame.image[0]);
//        }
//
//        grabber.stop();
//    }
}
