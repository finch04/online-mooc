package com.tianji.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.message.config.MessageProperties;
import com.tianji.message.domain.dto.UserInboxDTO;
import com.tianji.message.domain.po.NoticeTemplate;
import com.tianji.message.domain.po.PublicNotice;
import com.tianji.message.domain.po.UserInbox;
import com.tianji.message.domain.query.UserInboxQuery;
import com.tianji.message.mapper.UserInboxMapper;
import com.tianji.message.service.IPublicNoticeService;
import com.tianji.message.service.IUserInboxService;
import com.tianji.message.service.IUserPrivateMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户通知记录 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2022-08-19
 */
@Service
@RequiredArgsConstructor
public class UserInboxServiceImpl extends ServiceImpl<UserInboxMapper, UserInbox> implements IUserInboxService {

    private final MessageProperties properties;
    private final IPublicNoticeService publicNoticeService;
    private final UserClient userClient;
    private final IUserPrivateMessageService userPrivateMessageService;

    @Override
    public void saveNoticeToInbox(NoticeTemplate notice, List<UserDTO> users) {
        LocalDateTime pushTime = LocalDateTime.now();
        LocalDateTime expireTime = pushTime.plusMonths(properties.getMessageTtlMonths());
        // 1.初始化信箱数据
        List<UserInbox> list = new ArrayList<>(users.size());
        // 2.组装
        for (UserDTO user : users) {
            UserInbox box = new UserInbox();
            box.setTitle(notice.getTitle());
            box.setContent(notice.getContent());
            box.setUserId(user.getId());
            box.setType(notice.getType());
            box.setPushTime(pushTime);
            box.setExpireTime(expireTime);
            list.add(box);
        }
        // 3.保存
        saveBatch(list);
    }




    @Override
    @Transactional
    public PageDTO<UserInboxDTO> queryUserInBoxesPage(UserInboxQuery query) {
        // 1.获取用户信息
        Long userId = UserContext.getUser();
//        // 2.查询用户信箱中的最后一条公告，确认本次加载公告的最早时间点
        UserInbox latest = getBaseMapper().queryLatestPublicNotice(userId);
        // 2.1.默认时间点是当前时间减去公告的最大有效期时间（未过期的最早公告时间）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minTime = now.minusMonths(properties.getNoticeTtlMonths());
        // 2.2.如果有最后一条公告，判断公告时间是不是比最早时间要晚
        if(latest != null && latest.getPushTime().isAfter(minTime)){
            // 用户上次加载时间比最早时间晚，更新一下时间
            minTime = latest.getPushTime();
        }
        // 3.按照发布时间倒序，查看公告箱中的消息，最多加载200条
        Page<PublicNotice> noticePage = new Page<PublicNotice>(1, 200)
                .addOrder(new OrderItem("push_time", false));
        noticePage = publicNoticeService.lambdaQuery()
                .ge(PublicNotice::getPushTime, minTime)
                .page(noticePage);
        // 4.将公告写入用户收件箱
        if (CollUtils.isNotEmpty(noticePage.getRecords())) {
            saveNoticeListToInbox(noticePage.getRecords(), userId);
        }
        Page<UserInbox> page = new Page<UserInbox>(query.getPageNo(), query.getPageSize())
                .addOrder(new OrderItem("push_time", false));

        // 5.分页查询收件箱信息并返回
        page = this.lambdaQuery()
                .eq(UserInbox::getUserId, userId)
                .eq(query.getIsRead() != null, UserInbox::getIsRead, query.getIsRead())
                .eq(query.getType() != null, UserInbox::getType, query.getType())
                .page(page);
        //查出发送人姓名、头像等基本信息
        List<UserInbox> records = page.getRecords();
        // 收集发送者用户id
        Set<Long> userIds = records.stream()
                .map(UserInbox::getPublisher)
                .collect(Collectors.toSet());
        //根据发送者用户set用户名称和头像
        List<UserDTO> userDTOS = userClient.queryUserByIds(userIds);
        Map<Long, UserDTO> userMap = userDTOS.stream()
                .collect(Collectors.toMap(UserDTO::getId, user -> user));
        List<UserInboxDTO> dtoList = new ArrayList<>();
        for (UserInbox record : records) {
            UserInboxDTO dto= BeanUtils.copyProperties(record, UserInboxDTO.class);
            if(userMap.containsKey(record.getPublisher()) && record.getPublisher()!=0){
                dto.setPublisherName(userMap.get(record.getPublisher()).getName());
                dto.setPublisherIcon(userMap.get(record.getPublisher()).getIcon());
            }
            dtoList.add(dto);
        }

        return PageDTO.of(page, dtoList);
    }

    private void saveNoticeListToInbox(List<PublicNotice> notices, Long userId) {
        List<UserInbox> list = new ArrayList<>(notices.size());
        for (PublicNotice notice : notices) {
            UserInbox box = new UserInbox();
            box.setTitle(notice.getTitle());
            box.setContent(notice.getContent());
            box.setUserId(userId);
            box.setType(notice.getType());
            box.setPushTime(notice.getPushTime());
            box.setExpireTime(notice.getExpireTime());
            list.add(box);
        }
        saveBatch(list);
    }



    @Override
    public Integer getUnReadCountByType(Integer type) {
        //用户高频请求接口，可以考虑放到redis优化
        //获取当前登录人
        Long userId = UserContext.getUser();
        Integer userInboxUnReadCount = this.lambdaQuery()
                .eq(UserInbox::getUserId, userId)          // 用户ID相等
                .eq(UserInbox::getIsRead, false)// 未读消息
                .eq(UserInbox::getType, type)
                .ge(UserInbox::getExpireTime, LocalDateTime.now()) // 过期时间 >= 当前时间（未过期）
                .count();

        return userInboxUnReadCount;
    }

    @Override
    public Integer getUnReadCount() {
        //用户高频请求接口，可以考虑放到redis优化
        //获取当前登录人
        Long userId = UserContext.getUser();
        Integer userInboxUnReadCount = this.lambdaQuery()
                .eq(UserInbox::getUserId, userId)          // 用户ID相等
                .eq(UserInbox::getIsRead, false)          // 未读消息
                .ge(UserInbox::getExpireTime, LocalDateTime.now()) // 过期时间 >= 当前时间（未过期）
                .count();
        Integer userPrivateUnReadCount = userPrivateMessageService.getAllUnreadCountBySelf(userId);
        return userInboxUnReadCount+userPrivateUnReadCount;
    }

    @Override
    public Boolean markMessageAsRead(Long id) {
        LambdaQueryWrapper<UserInbox> userInboxLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userInboxLambdaQueryWrapper.eq(UserInbox::getId, id)
                .eq(UserInbox::getUserId, UserContext.getUser());
        boolean update = update(new UserInbox().setIsRead(true), userInboxLambdaQueryWrapper);
        return update;
    }

    @Override
    public Boolean markAllMessagesAsRead() {
        LambdaQueryWrapper<UserInbox> userInboxLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userInboxLambdaQueryWrapper
                .eq(UserInbox::getUserId, UserContext.getUser())
                .eq(UserInbox::getIsRead, false);
        boolean update = update(new UserInbox().setIsRead(true), userInboxLambdaQueryWrapper);
        return update;
    }
}
