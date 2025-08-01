package com.tianji.pay.third.ali.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tj.pay.ali")
public class AliPayProperties {
    private String protocol = "https";
//    private String gatewayHost= "openapi.alipay.com";
    private String gatewayHost= "openapi-sandbox.dl.alipaydev.com";    //沙箱环境专用网关
    private String signType = "RSA2";
    private String appId;
    private String merchantPrivateKey;
    private String publicKey;
    private String encryptKey;
}
