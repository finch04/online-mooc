package com.tianji.aigc.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioTranscriptionOptions;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisModel;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisPrompt;
import com.alibaba.cloud.ai.dashscope.audio.transcription.AudioTranscriptionModel;
import com.tianji.aigc.domain.param.AudioEventFilterParam;
import com.tianji.aigc.service.AudioService;
import com.tianji.aigc.service.FileStorage;
import com.tianji.aigc.utils.AudioEventFilterUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
     * 语音转文本（带音频事件过滤）
     * @param multipartFile 上传的音频文件
     * @param filterParam 过滤参数（可外部传入，无则用默认规则）
     * @return 过滤后的文本结果
     */
    public String stt(MultipartFile multipartFile, AudioEventFilterParam filterParam) {
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
            var audioTranscriptionPrompt = new AudioTranscriptionPrompt(resource, DashScopeAudioTranscriptionOptions.builder()
                    .withModel(DEFAULT_MODEL_1)
                    .build());

            // 调用大模型进行语音识别
            var response = this.audioTranscriptionModel.call(audioTranscriptionPrompt);

            String rawSttResult = response.getResult().getOutput();
            // 获取识别结果
            log.info("阿里云STT原始结果（带事件标记）：{}", rawSttResult);

            // 5. 应用音频事件过滤（无参数则用默认规则：只保留人声，剔除其他）
            AudioEventFilterParam finalFilterParam = filterParam == null ?
                    AudioEventFilterParam.defaultKeepSpeech() : filterParam;
            String filteredResult = AudioEventFilterUtil.filter(rawSttResult, finalFilterParam);
            log.info("过滤后STT结果：{}", filteredResult);

            return filteredResult;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 重载方法：使用默认过滤规则（只保留人声）
     */
    @Override
    public String stt(MultipartFile multipartFile) {
        return this.stt(multipartFile, null);
    }
}
