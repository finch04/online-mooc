package com.tianji.aigc.controller;

import com.tianji.aigc.service.AudioService;
import com.tianji.common.annotations.NoWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@RestController
@RequestMapping("/audio")
@RequiredArgsConstructor
public class AudioController {

    private final AudioService audioService;

    @PostMapping("/tts")
    public void tts(@RequestBody String text, HttpServletResponse response) {
        // 设置响应头
        response.setContentType("audio/mp3"); // 设置音频文件的MIME类型
        this.audioService.tts(text, response);
    }

    @NoWrapper
    @PostMapping(value = "/tts-stream", produces = "audio/mp3")
    public ResponseBodyEmitter ttsStream(@RequestBody String text) {
        return this.audioService.ttsStream(text);
    }

    @PostMapping("/stt")
    public String stt(@RequestParam("audioFile") MultipartFile audioFile) {
        return this.audioService.stt(audioFile);
    }

}
