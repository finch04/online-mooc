package com.tianji.live.controller;

import com.tianji.common.exceptions.CommonException;
import com.tianji.live.protocol.MessageBody;
import com.tianji.live.service.IChatBusiService;
import com.tianji.live.service.IIMService;
import com.tianji.live.service.IIMTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

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
    private IIMTokenService IIMTokenService;

    @Resource
    private IChatBusiService IChatBusiService;

    @Resource
    private IIMService iMService;

    /**
     * 获取IM服务器地址
     * @return
     */
    @Operation(summary = "获取IM服务器地址")
    @GetMapping("/getIMServer/{roomId}/{userId}")
    public Map<String,Object> getIMServer(@PathVariable("roomId") String roomId,
                                          @PathVariable("userId") String userId){
        System.out.println("userId = " + userId);
        List<ServiceInstance> instances = discoveryClient.getInstances(imInstance);
        if(instances.size()  == 0){
            throw new CommonException("IM服务未启动");
        }else{
            List<ServiceInstance> livinginstances = new ArrayList<>();
            instances.forEach(instance -> {
                if(instance.getPort()<9000){
                    livinginstances.add(instance);
                }
            });
            int index = ThreadLocalRandom.current().nextInt(0, livinginstances.size());
//            items.get(ThreadLocalRandom.current().nextInt(items.size()))
            ServiceInstance instanceToChoose = livinginstances.get(ThreadLocalRandom.current().nextInt(instances.size()));
            //ws://localhost:8989/chat/1/2
            var instanceUrl = "ws://"+instanceToChoose.getHost()+":"+instanceToChoose.getPort()+"/chat/"+roomId+"/"+userId;
            var imToken = IIMTokenService.generateIMToken(userId);
            Map<String ,Object > res = new HashMap<>();
            res.put("imToken",imToken);
            res.put("url",instanceUrl);
            return res;
        }
    }

    /**
     * 获取房间历史消息(未启用)
     */
    @Operation(summary = "获取房间历史消息")
    @GetMapping("/messages/{roomId}")
    public List<MessageBody> getRoomMessages(@PathVariable Long roomId) {
        if (roomId == null || roomId <= 0) {
            throw new CommonException("房间ID不能为空");
        }
        List<MessageBody> messages = IChatBusiService.getRoomHistoryMessages(roomId);
        return messages == null ? new ArrayList<>() : messages;
    }


    //TODO 往直播间发送广播消息。--只用作单机测试。
    // IMServer后端只做了单机测试，没做集群化处理。
    @Operation(summary = "发送房间广播消息")
    @PostMapping("/sendRoomBroadCast")
    public void sendRoomBroadCast(Long roomId,String message){
        iMService.sendMesasgeToRoom(roomId,message);
    }


}
