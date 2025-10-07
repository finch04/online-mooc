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

//获取直播间实时统计数据
export const getStat = (params) =>
    request({
        url: `${LIVE_API_PREFIX}/live/stat/${params}`,		
        method: 'get',
    })

// 关注用户/取消关注用户
export const follow = (params) =>
    request({
        url: `${LIVE_API_PREFIX}/follow`,		
        method: 'post',
        data: params
    })
// 直播间点赞
export const like = (params) =>
    request({
        url: `${LIVE_API_PREFIX}/live/like/${params}`,		
        method: 'post',
    })