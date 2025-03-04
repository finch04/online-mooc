package com.tianji.aigc.controller;

import com.tianji.aigc.service.AzureService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/audio")
@RequiredArgsConstructor
public class AudioController {

    private final AzureService azureService;

    @PostMapping("/tts")
    public void tts(@RequestParam("text") String text, HttpServletResponse response) throws Exception {
        // 设置响应头
        response.setContentType("audio/mp3"); // 设置音频文件的MIME类型
        this.azureService.tts(text, response);
    }

    @PostMapping("/stt")
    public String stt(@RequestParam("audioFile") MultipartFile audioFile) throws Exception {
        return this.azureService.stt(audioFile.getBytes());
    }

}
