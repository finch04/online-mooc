package com.tianji.gateway.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @ClassName LogHelper.java
 * @Description 请求处理类
 */
public class RequestHelper {

    /**
     * @Description 根据MediaType获取字符集，如果获取不到，则默认返回<tt>UTF_8</tt>
     * @param mediaType MediaType
     * @return Charset
     */
    public static Charset getMediaTypeCharset(@Nullable MediaType mediaType) {
        if (Objects.nonNull(mediaType) && mediaType.getCharset() != null) {
            return mediaType.getCharset();
        } else {
            return StandardCharsets.UTF_8;
        }
    }

    /**
     * @Description 读取请求体内容
     * @param request ServerHttpRequest
     * @return 请求体
     */
    public static String readRequestBody(ServerHttpRequest request,byte[] bytes) {
        HttpHeaders headers = request.getHeaders();
        MediaType mediaType = headers.getContentType();
        String method = request.getMethodValue().toUpperCase();
        if (Objects.nonNull(mediaType) && mediaType.equals(MediaType.MULTIPART_FORM_DATA)) {
            return "upload-file";
        }else if (method.equals(HttpMethod.GET)&&!request.getQueryParams().isEmpty()) {
            return request.getQueryParams().toString();
        }else if (headers.getContentLength() > 0) {
            return new String(bytes, getMediaTypeCharset(mediaType));
        }else {
            return "no-have-body";
        }
    }

    /**
     * @Description 判断是否是上传文件
     * @param mediaType MediaType
     * @return Boolean
     */
    public static boolean isUploadFile(@Nullable MediaType mediaType) {
        if (Objects.isNull(mediaType)) {
            return false;
        }
        return mediaType.includes(MediaType.MULTIPART_FORM_DATA)
                || mediaType.includes(MediaType.IMAGE_GIF)
                || mediaType.includes(MediaType.IMAGE_JPEG)
                || mediaType.includes(MediaType.IMAGE_PNG)
                || mediaType.equals(MediaType.MULTIPART_FORM_DATA_VALUE);
    }
}
