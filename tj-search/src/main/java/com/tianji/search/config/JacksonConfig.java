//package com.tianji.search.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
//import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//@Configuration
//public class JacksonConfig {
//
//    // 定义空格分隔的日期时间格式
//    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
//
//    @Bean
//    public ObjectMapper objectMapper() {
//        ObjectMapper objectMapper = new ObjectMapper();
//        JavaTimeModule javaTimeModule = new JavaTimeModule();
//
//        // 注册自定义序列化器（可选，确保序列化时也使用相同格式）
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
//        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
//        // 注册自定义反序列化器（核心：支持解析空格分隔的格式）
//        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
//
//        objectMapper.registerModule(javaTimeModule);
//        return objectMapper;
//    }
//}