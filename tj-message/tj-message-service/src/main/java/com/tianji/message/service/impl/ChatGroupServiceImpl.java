package com.tianji.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.message.domain.dto.ChatGroupDTO;
import com.tianji.message.domain.dto.GroupMemberDTO;
import com.tianji.message.domain.dto.NoticeTemplateDTO;
import com.tianji.message.domain.po.ChatGroup;
import com.tianji.message.domain.po.GroupMember;
import com.tianji.message.domain.po.NoticeTemplate;
import com.tianji.message.domain.po.UserConversation;
import com.tianji.message.domain.query.ChatGroupQuery;
import com.tianji.message.domain.vo.ChatGroupVO;
import com.tianji.message.mapper.ChatGroupMapper;
import com.tianji.message.mapper.GroupMemberMapper;
import com.tianji.message.service.IChatGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatGroupServiceImpl extends ServiceImpl<ChatGroupMapper, ChatGroup> implements IChatGroupService {

    private final GroupMemberMapper groupMemberMapper;

    @Override
    @Transactional
    public Long createGroup(ChatGroupDTO groupDTO) {
        // 1.创建群组
        ChatGroup group = BeanUtils.copyBean(groupDTO, ChatGroup.class);
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        save(group);

        // 2.添加群成员(包括创建者)
        List<Long> memberIds = groupDTO.getMemberIds();
        if (CollUtils.isNotEmpty(memberIds)) {
            // 确保创建者在群中
            if (!memberIds.contains(groupDTO.getCreatorId())) {
                memberIds.add(groupDTO.getCreatorId());
            }

            List<GroupMember> members = memberIds.stream()
                    .map(userId -> new GroupMember()
                            .setGroupId(group.getId())
                            .setUserId(userId)
                            .setJoinedAt(LocalDateTime.now()))
                    .collect(Collectors.toList());

            // 批量插入群成员
            groupMemberMapper.batchInsert(members);
        }

        return group.getId();
    }

    @Override
    public List<ChatGroupVO> getUserGroups(ChatGroupQuery query,Long userId) {
        Page<ChatGroup> page = new Page<>(query.getPageNo(), query.getPageSize());
        IPage<ChatGroup> pages= baseMapper.selectUserGroups(page,userId,query);
        List<ChatGroupVO> voList = new ArrayList<>();
        for (ChatGroup chatGroup : pages.getRecords()) {
            ChatGroupVO chatGroupVO = new ChatGroupVO();
            BeanUtils.copyProperties(chatGroup, chatGroupVO);
            chatGroupVO.setMemberCount(groupMemberMapper.selectCount(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, chatGroup.getId())));
            chatGroupVO.setIsJoined( groupMemberMapper.selectCount(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, chatGroup.getId()))>0);
            voList.add(chatGroupVO);
        }
        return voList;
    }

    @Override
    public PageDTO<ChatGroupVO> getAllGroups(ChatGroupQuery query) {
        Page<ChatGroup> page = query.toMpPage();
        LambdaQueryWrapper<ChatGroup> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        if (query.getStatus() != null) {
//            lambdaQueryWrapper.eq(ChatGroup::getStatus, query.getStatus());
//        }
//        if (StringUtils.isNotBlank(query.getName())) {
//            lambdaQueryWrapper.like(ChatGroup::getName, query.getName());
//        }
        page = this.baseMapper.selectPage(page,lambdaQueryWrapper);
        List<ChatGroup> chatGroups = page.getRecords();
        List<ChatGroupVO> voList = new ArrayList<>();
        for (ChatGroup chatGroup : chatGroups) {
            ChatGroupVO chatGroupVO = new ChatGroupVO();
            BeanUtils.copyProperties(chatGroup, chatGroupVO);
            chatGroupVO.setIsJoined( groupMemberMapper.selectCount(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, chatGroup.getId()))>0);
            chatGroupVO.setMemberCount(groupMemberMapper.selectCount(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, chatGroup.getId())));
            voList.add(chatGroupVO);
        }
        return PageDTO.of(page, voList);
    }


    @Override
    @Transactional
    public void addGroupMember(Long groupId, GroupMemberDTO memberDTO) {
        ChatGroup chatGroup = this.baseMapper.selectById(groupId);
        if(!UserContext.getUser().equals(chatGroup.getCreatorId())){
            throw new RuntimeException("只有群主才能添加群成员");
        }
        // 检查是否已经是群成员
        Integer count = groupMemberMapper.selectCount(new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, memberDTO.getUserId()));

        if (count == 0) {
            GroupMember member = new GroupMember()
                    .setGroupId(groupId)
                    .setUserId(memberDTO.getUserId())
                    .setJoinedAt(LocalDateTime.now());
            groupMemberMapper.insert(member);

            // 更新群组更新时间
            this.lambdaUpdate().eq(ChatGroup::getId, groupId)
                    .set(ChatGroup::getUpdatedAt, LocalDateTime.now())
                    .update();
        }
    }

    @Override
    @Transactional
    public void removeGroupMember(Long groupId, Long userId) {
        ChatGroup chatGroup = this.baseMapper.selectById(groupId);
        if(!UserContext.getUser().equals(chatGroup.getCreatorId())){
            throw new RuntimeException("只有群主才能删除群成员");
        }
        // 删除群成员
        groupMemberMapper.delete(new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId));

        this.lambdaUpdate()
                .eq(ChatGroup::getId, groupId)
                .set(ChatGroup::getUpdatedAt, LocalDateTime.now()).update();
    }


    @Override
    public void addToGroup(Long groupId, Long userId) {
        ChatGroup chatGroup = this.baseMapper.selectById(groupId);
        if(chatGroup == null){
            throw new RuntimeException("群组不存在");
        }
        if(chatGroup.getStatus() != 0){
            throw new RuntimeException("群组状态异常");
        }
        if(groupMemberMapper.selectCount(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId)) > 0){
            throw new RuntimeException("用户已加入群组");
        }
        GroupMember member = new GroupMember()
                .setGroupId(groupId)
                .setUserId(userId)
                .setJoinedAt(LocalDateTime.now());
        groupMemberMapper.insert(member);
    }
}