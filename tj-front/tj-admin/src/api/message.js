import request from "@/utils/request.js";

// 新增通知任务
export const saveNoticeTask = (noticeTaskFormDTO) => {
  return request({
    url: '/sms/notice-tasks',
    method: 'post',
    data: noticeTaskFormDTO
  });
};

// 更新通知任务
export const updateNoticeTask = (noticeTaskFormDTO, id) => {
  return request({
    url: `/sms/notice-tasks/${id}`,
    method: 'put',
    data: noticeTaskFormDTO
  });
};

// 分页查询通知任务
export const queryNoticeTasks = (pageQuery) => {
  return request({
    url: '/sms/notice-tasks',
    method: 'get',
    params: pageQuery
  });
};

// 根据id查询任务
export const queryNoticeTask = (id) => {
  return request({
    url: `/sms/notice-tasks/${id}`,
    method: 'get'
  });
};