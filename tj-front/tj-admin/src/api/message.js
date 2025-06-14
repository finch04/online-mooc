import request from "@/utils/request.js";

/**通知任务 */
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

/**短信平台信息 */
// 新增短信平台信息
export const saveSmsThirdPlatform = (smsThirdPlatformFormDTO) => {
  return request({
    url: '/sms/sms-platforms',
    method: 'post',
    data: smsThirdPlatformFormDTO
  });
};

// 更新短信平台信息
export const updateSmsThirdPlatform = (smsThirdPlatformFormDTO, id) => {
  return request({
    url: `/sms/sms-platforms/${id}`,
    method: 'put',
    data: smsThirdPlatformFormDTO
  });
};

// 分页查询短信平台信息
export const querySmsThirdPlatforms = (pageQuery) => {
  return request({
    url: '/sms/sms-platforms',
    method: 'get',
    params: pageQuery
  });
};

// 根据id查询短信平台信息
export const querySmsThirdPlatform = (id) => {
  return request({
    url: `/sms/sms-platforms/${id}`,
    method: 'get'
  });
};

// export const deleteSmsThirdPlatform = (id) => {
//   return request({
//     url: `/sms/sms-templates/${id}`,
//     method: 'delete'
//   });
// };

/**短信模板 */
// 新增短信模板
export const saveMessageTemplate = (messageTemplateFormDTO) => {
  return request({
    url: '/sms/message-templates',
    method: 'post',
    data: messageTemplateFormDTO
  });
};

// 更新短信模板
export const updateMessageTemplate = (messageTemplateFormDTO, id) => {
  return request({
    url: `/sms/message-templates/${id}`,
    method: 'put',
    data: messageTemplateFormDTO
  });
};

// 分页查询短信模板
export const queryMessageTemplates = (pageQuery) => {
  return request({
    url: '/sms/message-templates',
    method: 'get',
    params: pageQuery
  });
};

// 根据id查询短信模板
export const queryMessageTemplate = (id) => {
  return request({
    url: `/sms/message-templates/${id}`,
    method: 'get'
  });
};

/**通知模板 */

// 新增通知模板
export const saveNoticeTemplate = (noticeTemplateFormDTO) => {
  return request({
    url: '/sms/notice-templates',
    method: 'post',
    data: noticeTemplateFormDTO
  });
};

// 更新通知模板
export const updateNoticeTemplate = (noticeTemplateFormDTO, id) => {
  return request({
    url: `/sms/notice-templates/${id}`,
    method: 'put',
    data: noticeTemplateFormDTO
  });
};

// 分页查询通知模板
export const queryNoticeTemplates = (pageQuery) => {
  return request({
    url: '/sms/notice-templates',
    method: 'get',
    params: pageQuery
  });
};

// 根据id查询通知模板
export const queryNoticeTemplate = (id) => {
  return request({
    url: `/sms/notice-templates/${id}`,
    method: 'get'
  });
};

// export const deleteNoticeTemplate = (id) => {
//   return request({
//     url: `/sms/notice-templates/${id}`,
//     method: 'delete'
//   });
// };

/**敏感词管理 */
// 新增敏感词
export const saveSensitiveWord = (sensitiveWordFormDTO) => {
  return request({
    url: '/sms/sensitive',
    method: 'post',
    data: sensitiveWordFormDTO
  });
};

// 更新敏感词
export const updateSensitiveWord = (sensitiveWordFormDTO) => {
  return request({
    url: '/sms/sensitive',
    method: 'put',
    data: sensitiveWordFormDTO
  });
};

// 查询所有敏感词
export const listSensitiveWords = (pageQuery) => {
  return request({
    url: '/sms/sensitive/list',
    method: 'get',
    params: pageQuery
  });
};

// 删除敏感词
export const deleteSensitiveWord = (id) => {
  return request({
    url: `/sms/sensitive/${id}`,
    method: 'delete'
  });
};