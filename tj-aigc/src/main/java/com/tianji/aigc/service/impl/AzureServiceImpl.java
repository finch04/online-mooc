package com.tianji.aigc.service.impl;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioInputStream;
import com.microsoft.cognitiveservices.speech.audio.PushAudioInputStream;
import com.tianji.aigc.service.AzureService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.concurrent.Future;

@Slf4j
@Service
public class AzureServiceImpl implements AzureService {

    @Resource
    private SpeechConfig speechConfig;

    /**
     * 语音转文字
     *
     * @param audioFileData 音频文件，wav格式
     * @return 识别结果
     */
    public String stt(byte[] audioFileData) {
        SpeechRecognizer recognizer = null;
        try {
            //创建音频文件推送流
            PushAudioInputStream pushStream = AudioInputStream.createPushStream();
            //写入音频文件
            pushStream.write(audioFileData);
            //通过推送流创建音频配置
            AudioConfig audioInput = AudioConfig.fromStreamInput(pushStream);
            //创建语音识别器
            recognizer = new SpeechRecognizer(speechConfig, audioInput);
            //创建同步识别任务
            Future<SpeechRecognitionResult> task = recognizer.recognizeOnceAsync();
            //等待识别完成
            SpeechRecognitionResult speechRecognitionResult = task.get();
            //返回识别结果
            return speechRecognitionResult.getText();
        } catch (Exception e) {
            log.error("Voice to text conversion failed!", e);
        } finally {
            if (recognizer != null) {
                recognizer.close();
            }
        }
        return "ERROR";
    }

    public void tts(String text, HttpServletResponse response) {
        SpeechSynthesizer speechSynthesizer = null;
        try {
            // 创建 OutputStream 用于将音频数据写入响应
            OutputStream outputStream = response.getOutputStream();

            // 创建语音合成器对象
            speechSynthesizer = new SpeechSynthesizer(speechConfig, null);
            // 调用 SpeakText 方法进行文本转语音
            SpeechSynthesisResult result = speechSynthesizer.SpeakText(text);
            // 设置音频输出格式为 mp3 格式
            speechConfig.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Audio16Khz32KBitRateMonoMp3);
            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                // 从合成结果中获取音频数据
                AudioDataStream stream = AudioDataStream.fromResult(result);

                // 循环读取音频数据并写入响应输出流
                byte[] buffer = new byte[1024];
                long filledSize = stream.readData(buffer);
                while (filledSize > 0) {
                    // 将音频数据写入响应输出流
                    outputStream.write(buffer);
                    filledSize = stream.readData(buffer);
                }
            }
        } catch (Exception e) {
            log.error("Text to speech synthesis failed!", e);
        } finally {
            if (null != speechSynthesizer) {
                speechSynthesizer.close();
            }
        }
    }

}
