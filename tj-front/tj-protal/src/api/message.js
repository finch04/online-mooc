import request from "@/utils/request.js"
// 我的消息页面接口
const MESSAGE_API_PREFIX = "/sms"

// 获取未读消息
export const getNotRead = () =>
    request({
        url: `${MESSAGE_API_PREFIX}/inboxes/unread`,
        method: 'get',
    })		

// 根据消息类型得到其未读数
export const getUnReadCountByType = (type) =>
    request({
        url: `${MESSAGE_API_PREFIX}/inboxes/unread/${type}`,
        method: 'get',
    })	

// 分页查询收件箱
export const queryUserInbox = (params) =>
    request({
        url: `${MESSAGE_API_PREFIX}/inboxes/`,
        method: 'get',
        params
    })		

// 标记该通知已读
export const markMessageAsRead = (id) =>
    request({
        url: `${MESSAGE_API_PREFIX}/inboxes/mark/${id}`,
        method: 'post',
    })	

// 标记全部通知已读
export const markAllMessageAsRead = () =>
    request({
        url: `${MESSAGE_API_PREFIX}/inboxes/markAll`,
        method: 'post',
    })	

// 分页查询会话列表
export const queryUserConversation = (params) =>
    request({
        url: `${MESSAGE_API_PREFIX}/conversations`,
        method: 'get',
        params
    })	
// 屏蔽会话
export const blockUserConversation = (id) =>
    request({
        url: `${MESSAGE_API_PREFIX}/conversations/block/${id}`,
        method: 'put',
    })	
// 取消屏蔽会话
export const unblockUserConversation = (id) =>
    request({
        url: `${MESSAGE_API_PREFIX}/conversations/unblock/${id}`,
        method: 'put',
    })	
// 删除会话
export const deleteUserConversation = (id) =>
    request({
        url: `${MESSAGE_API_PREFIX}/conversations/${id}`,
        method: 'delete',
    })	    
// 私信获取聊天记录
export const getMessageRecords = (params) =>
    request({
        url: `${MESSAGE_API_PREFIX}/messages/`,
        method: 'get',
        params
    })	

// 发送私信
export const sendMessageToUser = data =>
    request({
        url: `${MESSAGE_API_PREFIX}/messages/`,
        method: 'post',
        data
    })		

/**在线群聊 */
//查看在线人数
export const getOnlineCount = () =>
    request({
        url: `${MESSAGE_API_PREFIX}/online-count/`,
        method: 'get'
    })
/** 群聊相关接口 */
// 创建群组
export const createGroup = (data) =>
    request({
        url: `${MESSAGE_API_PREFIX}/chat/groups`,
        method: 'post',
        data
    })

// 获取我的群组列表
export const getMyGroups = (params) =>
    request({
        url: `${MESSAGE_API_PREFIX}/chat/groups`,
        method: 'get',
        params
    })
// 获取所有群列表
export const getAllGroups = (params) =>
    request({
        url: `${MESSAGE_API_PREFIX}/chat/groups/all`,
        method: 'get',
        params
    })
// 用户主动加群
export const addToGroup = (groupId) =>
    request({
        url: `${MESSAGE_API_PREFIX}/chat/groups/${groupId}`,
        method: 'post',
    })

// 添加群成员
export const addGroupMember = (groupId, userId) =>
    request({
        url: `${MESSAGE_API_PREFIX}/chat/groups/${groupId}/members`,
        method: 'post',
        data: { userId }
    })

// 移除群成员
export const removeGroupMember = (groupId, userId) =>
    request({
        url: `${MESSAGE_API_PREFIX}/chat/groups/${groupId}/members/${userId}`,
        method: 'delete'
    })

// 发送群消息
export const sendGroupMessage = (data) =>
    request({
        url: `${MESSAGE_API_PREFIX}/chat/messages/group`,
        method: 'post',
        data
    })

// 获取历史消息
export const getHistoryMessages = (params) =>
    request({
        url: `${MESSAGE_API_PREFIX}/chat/messages`,
        method: 'get',
        params
    })

// 获取群成员列表
export const getGroupMembers = (groupId) =>
    request({
        url: `${MESSAGE_API_PREFIX}/chat/groups/${groupId}/members`,
        method: 'get'
    })