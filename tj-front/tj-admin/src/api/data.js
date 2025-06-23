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
export const exportLog = () =>
  request({
      url: `/ds/data/url/export/logs`,
      method: "get",
      headers: {
        'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      },
      responseType: 'blob' // 设置响应类型为二进制流
  });
/**流量统计接口 */    
export const getDnu = (params) =>
  request({
      url: `/ds/data/flow/dnu`,
      method: "get",
      params
  });
export const getDuv = (params) =>
  request({
      url: `/ds/data/flow/duv`,
      method: "get",
      params
  });
export const getDpv = (params) =>
  request({
      url: `/ds/data/flow/dpv`,
      method: "get",
      params
  });
export const getDau = (params) =>
  request({
      url: `/ds/data/flow/dau`,
      method: "get",
      params
  });
export const getDpvTime = (params) =>
  request({
      url: `/ds/data/flow/dpv/time`,
      method: "get",
      params
  });
export const getDauProvince = (params) =>
  request({
      url: `/ds/data/flow/dau/province`,
      method: "get",
      params
  });
/**数据分析接口 */     
export const getCourseConversionDpv = (params) =>
  request({
      url: `/ds/data/analysis/course/conversion`,
      method: "get",
      params
  });
export const getCourseDetailGenderDuv = (params) =>
  request({
      url: `/ds/data/analysis/course/gender`,
      method: "get",
      params
  });
export const getCourseDetailProvinceDuv = (params) =>
  request({
      url: `/ds/data/analysis/course/province`,
      method: "get",
      params
  });
    