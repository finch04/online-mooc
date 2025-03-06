package com.tianji.aigc.service.impl;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.tianji.aigc.service.AudioService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;

@Service
@RequiredArgsConstructor
public class OpenAIAudioServiceImpl implements AudioService {


    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;
    private final OpenAiAudioSpeechModel openAiAudioSpeechModel;

    @Value("classpath:/speech/jfk.flac")
    private Resource audioFile;

    @Override
    public String stt(MultipartFile multipartFile) {
        // 将MultipartFile转换为Resource
        Resource audioResource = multipartFile.getResource();
        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioResource);
        // 调用OpenAiAudioTranscriptionModel进行语音识别
        AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(transcriptionRequest);
        // 获取识别结果
        String output = response.getResult().getOutput();
        // 将繁体转换为简体
        return ZhConverterUtil.toSimple(output);
    }


    @Override
    public void tts(String text, HttpServletResponse response) {
        SpeechPrompt speechPrompt = new SpeechPrompt(text);
        SpeechResponse speechResponse = openAiAudioSpeechModel.call(speechPrompt);
        byte[] output = speechResponse.getResult().getOutput();

        try {
            // 创建 OutputStream 用于将音频数据写入响应
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
