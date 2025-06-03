import request from "@/utils/request.js";

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