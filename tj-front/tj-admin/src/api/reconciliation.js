import request from "@/utils/request.js";

//支付渠道配置
export const listAllPayChannels = () => {
  return request({
    url: `/ps/pay-channels/list`,
    method: 'get'
  });
}
export const addPayChannel = (data) => {
  return request({
    url: `/ps/pay-channels`,
    method: 'post',
    data
  });
}

export const getPayChannelById = (id) => {
  return request({
    url: `/ps/pay-channels/${id}`,
    method: 'get'
  });
}

export const updatePayChannel = (id,data) => {
  return request({
    url: `/ps/pay-channels/${id}`,
    method: 'put',
    data
  });
}

// 获取对账列表
export function getReconciliationList(data) {
    return request({
      url: "/ps/reconciliation/list",
      method: "get",
      params: data,
    });
  }
  
  // 获取对账详情
  export function getReconciliationDetails(id) {
    return request({
      url: `/ps/reconciliation/${id}`,
      method: "get",
    });
  }