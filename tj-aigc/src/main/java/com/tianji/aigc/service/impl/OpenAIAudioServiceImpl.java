package com.tianji.aigc.service.impl;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.tianji.aigc.service.AudioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIAudioServiceImpl implements AudioService {

    private final OpenAiAudioSpeechModel openAiAudioSpeechModel;

    @Override
    public ResponseBodyEmitter ttsStream(String text) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        log.info("开始语音合成, 文本内容：{}", text);
        SpeechPrompt speechPrompt = new SpeechPrompt(text);
        Flux<SpeechResponse> responseStream = openAiAudioSpeechModel.stream(speechPrompt);
        // 订阅响应流并发送数据
        responseStream.subscribe(
                speechResponse -> {
                    try {
                        // 获取响应输出的数据，并发送到响应体中
                        byte[] audioBytes = speechResponse.getResult().getOutput();
                        emitter.send(audioBytes);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                emitter::complete
        );
        return emitter;
    }

    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

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
}
