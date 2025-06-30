package org.springframework.ai.autoconfigure.chat.client;

import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeChatProperties;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.autoconfigure.openai.OpenAiChatProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.chat.client.observation.ChatClientInputContentObservationFilter;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@AutoConfiguration
@ConditionalOnClass(ChatClient.class)
@EnableConfigurationProperties(ChatClientBuilderProperties.class)
@ConditionalOnProperty(prefix = "spring.ai.chat.my-client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MyChatClientAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MyChatClientAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    ChatClientBuilderConfigurer chatClientBuilderConfigurer(ObjectProvider<ChatClientCustomizer> customizerProvider) {
        ChatClientBuilderConfigurer configurer = new ChatClientBuilderConfigurer();
        configurer.setChatClientCustomizers(customizerProvider.orderedStream().toList());
        return configurer;
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnProperty(prefix = DashScopeChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true")
    ChatClient.Builder dashScopeChatClientBuilder(ChatClientBuilderConfigurer chatClientBuilderConfigurer,
                                                  DashScopeChatModel dashScopeChatModel,
                                                  ObjectProvider<ObservationRegistry> observationRegistry,
                                                  ObjectProvider<ChatClientObservationConvention> observationConvention) {
        ChatClient.Builder builder = ChatClient.builder(dashScopeChatModel,
                observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP),
                observationConvention.getIfUnique(() -> null));
        return chatClientBuilderConfigurer.configure(builder);
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnProperty(prefix = OpenAiChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true")
    ChatClient.Builder openAiChatClientBuilder(ChatClientBuilderConfigurer chatClientBuilderConfigurer,
                                               OpenAiChatModel openAiChatModel,
                                               ObjectProvider<ObservationRegistry> observationRegistry,
                                               ObjectProvider<ChatClientObservationConvention> observationConvention) {
        ChatClient.Builder builder = ChatClient.builder(openAiChatModel,
                observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP),
                observationConvention.getIfUnique(() -> null));
        return chatClientBuilderConfigurer.configure(builder);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = ChatClientBuilderProperties.CONFIG_PREFIX + ".observations", name = "include-input",
            havingValue = "true")
    ChatClientInputContentObservationFilter chatClientInputContentObservationFilter() {
        logger.warn(
                "You have enabled the inclusion of the input content in the observations, with the risk of exposing sensitive or private information. Please, be careful!");
        return new ChatClientInputContentObservationFilter();
    }

}