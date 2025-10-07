import request from "@/utils/request.js"
import qs from 'qs'
const LIVE_API_PREFIX = "/lvs"

// 获取直播间详情
export const getLiveRoomById = (params) =>
    request({
        url: `${LIVE_API_PREFIX}/live/${params}`,		
        method: 'get'
    })
// 获取直播间列表
export const getLiveRoomList = () =>
    request({
        url: `${LIVE_API_PREFIX}/live/list`,		
        method: 'get'
    })
    

// 获取IM服务器
export const getIMServerUrl = (roomId,userId) =>
    request({
        url: `${LIVE_API_PREFIX}/im/getIMServer/${roomId}/${userId}`,		
        method: 'get',
    })

//获取直播间实时在线人数
export const getLiveRoomOnlineCount = (params) =>
    request({
        url: `${LIVE_API_PREFIX}/live/onlineCount/${params}`,		
        method: 'get',
    })

// 关注用户/取消关注用户
export const follow = (params) =>
    request({
        url: `${LIVE_API_PREFIX}/follow`,		
        method: 'post',
        data: params
    })