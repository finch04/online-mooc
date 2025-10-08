package com.tianji.aigc.controller;

import com.tianji.aigc.service.AudioService;
import com.tianji.common.annotations.NoWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@RestController
@RequestMapping("/audio")
@Tag(name = "AI语音文本互转")
@RequiredArgsConstructor
public class AudioController {

    private final AudioService audioService;

    @NoWrapper
    @Operation(summary = "文本转语音")
    @PostMapping(value = "/tts-stream", produces = "audio/mp3")
    public ResponseBodyEmitter ttsStream(@RequestBody String text) {
        return this.audioService.ttsStream(text);
    }

    @Operation(summary = "语音转文本")
    @PostMapping("/stt")
    public String stt(@RequestParam("audioFile") MultipartFile audioFile) {
        return this.audioService.stt(audioFile);
    }
}
