import request from "@/utils/request.js";

/**URL日志接口 */    
export const getLogsPageByUrl = (params) =>
  request({
    url: `/ds/data/url/page/log`,
    method: "get",
    params
  });
export const getLogsPageByUrlByLike = (params) =>
request({
    url: `/ds/data/url/page/log/like`,
    method: "get",
    params
});
export const getMetricByUrl = (params) =>
    request({
        url: `/ds/data/url/metric`,
        method: "get",
        params
    });
export const getMetricByUrlByLike = (params) =>
  request({
      url: `/ds/data/url/metric/like`,
      method: "get",
      params
  });
/**流量统计接口 */    
export const getBaseFlow = (params) =>
  request({
      url: `/ds/data/flow/base`,
      method: "get",
      params
  });
export const getVisitsUrlFlow = (params) =>
  request({
      url: `/ds/data/flow/url/visits`,
      method: "get",
      params
  });
export const getErrorsUrlFlow = (params) =>
  request({
      url: `/ds/data/flow/url/errors`,
      method: "get",
      params
  });
    
