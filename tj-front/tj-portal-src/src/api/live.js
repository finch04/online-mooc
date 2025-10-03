import request from "@/utils/request.js"
const LIVE_API_PREFIX = "/lvs"

// 获取直播间详情
export const getLiveRoomById = (params) =>
    request({
        url: `${LIVE_API_PREFIX}/live/${params}`,		
        method: 'get',
		params
    })

export const getLiveRoomList = () =>
    request({
        url: `${LIVE_API_PREFIX}/live/list`,		
        method: 'get'
    })
    

// 获取IM服务器
export const getIMServerUrl = (params) =>
    request({
        url: `${LIVE_API_PREFIX}/im/getIMServer`,		
        method: 'post',
		data:params,
		params
    })
