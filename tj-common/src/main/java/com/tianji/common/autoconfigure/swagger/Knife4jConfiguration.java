package com.tianji.common.autoconfigure.swagger;

import com.fasterxml.classmate.TypeResolver;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "tj.swagger", name = "enable", havingValue = "true")
@EnableConfigurationProperties(SwaggerConfigProperties.class)
public class Knife4jConfiguration {

    @Resource
    private SwaggerConfigProperties swaggerConfigProperties;

    @Bean
    @ConditionalOnProperty(name = "tj.swagger.enable", havingValue = "true")
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(this.swaggerConfigProperties.getTitle())
                        .version(this.swaggerConfigProperties.getVersion())
                        .description(this.swaggerConfigProperties.getDescription())
                        .contact(new Contact().name(this.swaggerConfigProperties.getContactName())
                                .url(this.swaggerConfigProperties.getContactUrl())
                                .email(this.swaggerConfigProperties.getContactEmail()))
                );
    }


    // @Bean(value = "defaultApi2")
    // public Docket defaultApi2(TypeResolver typeResolver) {
    //     // 1.初始化Docket
    //     Docket docket = new Docket(DocumentationType.SWAGGER_2);
    //     // 2.是否需要包装R
    //     if(swaggerConfigProperties.getEnableResponseWrap()){
    //         docket.additionalModels(typeResolver.resolve(R.class));
    //     }
    //     return docket.apiInfo(new ApiInfoBuilder()
    //             .title(this.swaggerConfigProperties.getTitle())
    //             .description(this.swaggerConfigProperties.getDescription())
    //             .contact(new Contact(
    //                     this.swaggerConfigProperties.getContactName(),
    //                     this.swaggerConfigProperties.getContactUrl(),
    //                     this.swaggerConfigProperties.getContactEmail()))
    //             .version(this.swaggerConfigProperties.getVersion())
    //             .build())
    //             .select()
    //             //这里指定Controller扫描包路径
    //             .apis(RequestHandlerSelectors.basePackage(swaggerConfigProperties.getPackagePath()))
    //             .paths(PathSelectors.any())
    //             .build();
    //
    // }
    // @Bean
    // @Primary
    // @ConditionalOnProperty(prefix = "tj.swagger", name = "enableResponseWrap",havingValue = "true")
    // public BaseSwaggerResponseModelPlugin baseSwaggerResponseModelPlugin(){
    //     return new BaseSwaggerResponseModelPlugin();
    // }
    // @Bean
    // @Primary
    // @ConditionalOnProperty(prefix = "tj.swagger", name = "enableResponseWrap",havingValue = "true")
    // public BaseSwaggerResponseBuilderPlugin baseSwaggerResponseBuilderPlugin(){
    //     return new BaseSwaggerResponseBuilderPlugin();
    // }
    // {
    //     // hutool的日期转换器加载
    //     ConverterRegistry converterRegistry = ConverterRegistry.getInstance();
    //     converterRegistry.putCustom(LocalDateTime.class, new TjTemporalConverter(LocalDateTime.class));
    // }

    @Bean
    public TypeResolver typeResolver() {
        return new TypeResolver();
    }
}
