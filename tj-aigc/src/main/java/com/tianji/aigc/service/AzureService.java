package com.tianji.aigc.service;

import jakarta.servlet.http.HttpServletResponse;

public interface AzureService {
    /**
     * 语音转文字（STT）
     * @param audioFileData WAV格式的音频文件字节数组
     * @return 识别结果文本
     */
    String stt(byte[] audioFileData);

    /**
     * 文字转语音（TTS）
     * @param text 待合成的文本内容
     * @param response HTTP响应对象，用于直接输出音频流
     */
    void tts(String text, HttpServletResponse response);
}
