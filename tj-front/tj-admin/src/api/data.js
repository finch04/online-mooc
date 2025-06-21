import request from "@/utils/request.js";

// 获取统计数据
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

