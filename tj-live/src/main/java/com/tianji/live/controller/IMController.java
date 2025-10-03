package com.tianji.live.controller;

import com.tianji.common.domain.R;
import com.tianji.common.exceptions.CommonException;
import com.tianji.live.service.IMTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author: fsq
 * @Date: 2025/10/3 16:27
 * @Version: 1.0
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "IM管理")
@RequestMapping("/im")
public class IMController {

    @Value("${tj.im.instance}")
    private String imInstance;

    @Resource
    private DiscoveryClient discoveryClient;

    @Resource
    private IMTokenService imTokenService;

    /**
     * 获取IM服务器地址
     * @return
     */
    @PostMapping("/getIMServer")
    public Map<String,Object> getIMServer(String userId){
        List<ServiceInstance> instances = discoveryClient.getInstances(imInstance);
        if(instances.size()  == 0){
            throw new CommonException("IM服务未启动");
        }else{
            //TODO IM-SERVER增加Dubbo服务后，Dubbo服务也会往Nacos上注册一个dubbo服务的实例。这些实例要过滤掉。
            List<ServiceInstance> livinginstances = new ArrayList<>();
            instances.forEach(instance -> {
                if(instance.getPort()<9000){
                    livinginstances.add(instance);
                }
            });
            int index = ThreadLocalRandom.current().nextInt(0, livinginstances.size());
//            items.get(ThreadLocalRandom.current().nextInt(items.size()))
            ServiceInstance instanceToChoose = livinginstances.get(ThreadLocalRandom.current().nextInt(instances.size()));
            //ws://localhost:8989/chat/1
            var instanceUrl = "ws://"+instanceToChoose.getHost()+":"+instanceToChoose.getPort()+"/chat/"+userId;
            var imToken = imTokenService.generateIMToken(userId);
            Map<String ,Object > res = new HashMap<>();
            res.put("imToken",imToken);
            res.put("url",instanceUrl);
            return res;
        }
    }
}
