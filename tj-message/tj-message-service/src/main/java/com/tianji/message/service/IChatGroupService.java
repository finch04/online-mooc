package com.tianji.message.service;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.message.domain.dto.ChatGroupDTO;
import com.tianji.message.domain.dto.ChatMessageDTO;
import com.tianji.message.domain.dto.GroupMemberDTO;
import com.tianji.message.domain.po.ChatGroup;
import com.tianji.message.domain.po.ChatMessage;
import com.tianji.message.domain.query.ChatGroupQuery;
import com.tianji.message.domain.vo.ChatGroupVO;

import java.util.List;

public interface IChatGroupService {
    Long createGroup(ChatGroupDTO groupDTO);
    List<ChatGroupVO> getUserGroups(ChatGroupQuery query,Long userId);
    void addGroupMember(Long groupId, GroupMemberDTO memberDTO);
    void removeGroupMember(Long groupId, Long userId);
    PageDTO<ChatGroupVO> getAllGroups(ChatGroupQuery query);

    void addToGroup(Long groupId, Long userId);
}