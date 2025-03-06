package com.tianji.aigc.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AudioService {
    /**
     * 语音转文字（STT）
     * @param audioFile 音频文件
     * @return 识别结果文本
     */
    String stt(MultipartFile audioFile);

    /**
     * 文字转语音（TTS）
     * @param text 待合成的文本内容
     * @param response HTTP响应对象，用于直接输出音频流
     */
    void tts(String text, HttpServletResponse response);
}
