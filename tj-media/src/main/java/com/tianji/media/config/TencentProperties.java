package com.tianji.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tj.tencent")
public class TencentProperties {
    private Long appId;
    private String secretId;
    private String secretKey;
    private VodProperties vod;
    private CosProperties cos;
    @Data
    public static class VodProperties{
        /*是否启用腾讯VOD*/
        private boolean enable;
        /*签名有效期*/
        private long vodValidSeconds;
        /*区域*/
        private String region;
        /** 上传后执行的点播任务流名称（ApplyUpload / 控制台「任务流」），用于转码等处理 */
        private String procedure;
        /** 播放 JWT 签名使用的播放密钥，对应控制台「分发播放设置」中的播放密钥（勿与防盗链 KEY 混淆） */
        private String urlKey;
        /**
         * 历史遗留字段：旧版播放器签名曾作为 pcfg 传入，新版签名已改为 {@link #transcodeDefinition} 与 contentInfo。
         * 可删除或留空；播放签名不再读取该字段。
         */
        private String pfcg;
        /**
         * 播放签名必填：{@code contentInfo.transcodeDefinition}，云点播控制台「视频处理 - 转码模板」中的模板数字 ID。
         * 最简播放（普通 MP4/HLS 转码一条）请选用对应模板的 ID。
         */
        private Integer transcodeDefinition;
    }
    @Data
    public static class CosProperties{
        /*区域*/
        private String region;
        /*存储桶*/
        private String bucket;
        /*触发分块上传的阈值*/
        private long multipartUploadThreshold;
        /*分块上传的最小分块大小*/
        private long minimumUploadPartSize;
    }
}
