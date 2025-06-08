import request from "@/utils/request.js"
const AI_API_PREFIX = "/ct"

// 用户会话模块

//新增会话
export const createUserSession = (data) =>
    request({
        url: `${AI_API_PREFIX}/session`,
        method: 'post',
        data
    })
//查询用户会话列表
export const getUserSessionList = () =>
    request({
        url: `${AI_API_PREFIX}/session/list`,
        method: 'get',
    })
//更改对话
export const updateUserSession = (id,params) =>
    request({
        url: `${AI_API_PREFIX}/session/${id}`,
        method: 'put',
        params
    })
//删除会话
export const deleteUserSession = (id) =>
    request({
        url: `${AI_API_PREFIX}/session/${id}`,
        method: 'delete',
    })

//聊天接口

//根据会话id获取聊天记录
export const getChatRecord = (params) =>
    request({
        url: `${AI_API_PREFIX}/chat/records`,
        method: 'get',
        params
    })

// AI聊天接口
export const memoryChatRedis = (params) =>
    request({
        url: `${AI_API_PREFIX}/chat/simple`,
        method: 'get',
        params
    })
//AI流式聊天接口
export const memoryChatRedisStream = (params) =>
    request({
        url: `${AI_API_PREFIX}/chat/`,
        method: 'get',
        params,
        responseType: 'stream' 
    })