package com.tianji.aigc.config;

import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisOutputFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfig {

    @Value("${azure.speech.key}")
    private String subscriptionKey;
    @Value("${azure.speech.region}")
    private String region;
    @Value("${azure.speech.language}")
    private String language;

    @Bean
    public SpeechConfig speechConfig() {
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(subscriptionKey, region);
        speechConfig.setSpeechRecognitionLanguage(language);
        speechConfig.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Audio16Khz32KBitRateMonoMp3);
        return speechConfig;
    }

}
