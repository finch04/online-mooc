import request from "@/utils/request.js"
const AI_API_PREFIX = "/ct"

// AI聊天接口
export const memoryChatRedis = (params) =>
    request({
        url: `${AI_API_PREFIX}/chat/assistant/redis`,
        method: 'get',
        params
    })
//AI流式聊天接口
export const memoryChatRedisStream = (params) =>
    request({
        url: `${AI_API_PREFIX}/chat/assistant/redis/stream`,
        method: 'get',
        params,
        responseType: 'stream' 
    })