package com.tianji.aigc.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioTranscriptionOptions;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisModel;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisPrompt;
import com.alibaba.cloud.ai.dashscope.audio.transcription.AudioTranscriptionModel;
import com.tianji.aigc.service.AudioService;
import com.tianji.aigc.service.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.nio.ByteBuffer;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tj.ai", name = "audio-type", havingValue = "DASHSCOPE")
public class DashScopeAudioServiceImpl implements AudioService {

    private final SpeechSynthesisModel speechSynthesisModel;
    private final AudioTranscriptionModel audioTranscriptionModel;
    private final FileStorage fileStorage;

    private static final String DEFAULT_MODEL_1 = "sensevoice-v1";

    @Override
    public ResponseBodyEmitter ttsStream(String text) {
        var prompt = new SpeechSynthesisPrompt(text);
        var response = this.speechSynthesisModel.stream(prompt);
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        // 订阅响应流并发送数据
        response.subscribe(
                speechResponse -> {
                    try {
                        // 获取响应输出的数据，并发送到响应体中
                        ByteBuffer byteBuffer = speechResponse.getResult().getOutput().getAudio();
                        byte[] bytes = new byte[byteBuffer.remaining()];
                        byteBuffer.get(bytes);
                        emitter.send(bytes);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                emitter::complete
        );
        return emitter;
    }

    /**
     * 由于阿里云返回的结果中多了sentence_id字段，导致sdk识别失败，暂未测通
     *
     * @param multipartFile 待识别的音频文件
     * @return 识别结果
     */
    @Override
    public String stt(MultipartFile multipartFile) {
        try {
            // 文件上传到阿里云OSS
            String suffix = StrUtil.subAfter(multipartFile.getOriginalFilename(), ".", true);
            var path = StrUtil.format("{}/{}.{}",
                    DateUtil.format(DateUtil.date(), "yyyy/MM/dd"),
                    IdUtil.fastSimpleUUID(),
                    suffix);
            String url = this.fileStorage.uploadFile(path, multipartFile.getInputStream(), multipartFile.getSize());

            // 将上传的文件路径转换成资源对象
            var resource = new UrlResource(url);
            var audioTranscriptionPrompt = new AudioTranscriptionPrompt(resource,  DashScopeAudioTranscriptionOptions.builder()
                    .withModel(DEFAULT_MODEL_1)
                    .build());

            // 调用大模型进行语音识别
            var response = this.audioTranscriptionModel.call(audioTranscriptionPrompt);

            // 获取识别结果
            return response.getResult().getOutput();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}