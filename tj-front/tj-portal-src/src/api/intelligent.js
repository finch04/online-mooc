import request from '@/utils/request.js';
// 新建窗口
export const newSession = (params) =>
  request({
    url: `/ais/session`,
    method: 'post',
    data: params,
  });
  // 新建窗口
export const newHot = (params) =>
  request({
    url: `/ais/session/hot`,
    method: 'get',
    params: params,
  });
// 聊天接口
export const getChat = (params) =>
  request({
    url: `/ais/chat`,
    method: 'post',
    data: params,
  });
//联想、润色、帮写、简写
export const getChatText = (params) =>
  request({
    url: `/ais/chat/text`,
    method: 'post',
    data: params,
  });
// 聊天接口
export const getChatTemplates = (params) =>
  request({
    url: `/ais/chat/templates`,
    method: 'get'
  });
/// 保存标题
export const saveSession = (params) =>
  request({
    url: `/ais/session`,
    method: 'put',
    params: params,
  });
/// 更新标题
export const updateSession = (params) =>
	request({
	  url: `/ais/session/history`,
	  method: 'put',
	  params: params,
	});
// 查询聊天记录列表
export const getChatHistoryList = (params) =>
  request({
    url: `/ais/session/history`,
    method: 'get',
  });
  // 查询聊天记录
export const delectHistory = (sessionId) =>
  request({
    url: `/ais/session/history?sessionId=${sessionId}`,
    method: 'delete',
  });
// 查询聊天记录
export const getChatHistory = (params) =>
  request({
    url: `/ais/session/${params.sessionId}`,
    method: 'get',
  });
export const chatStop = (params) =>
  request({
    url: `/ais/chat/stop?sessionId=${params.sessionId}`,
    method: 'post',
    data: params,
  });
export const getClassDetails = (id) =>
  request({
    url: `${COURSE_API_PREFIX}/courses/baseInfo/${id}`,
    method: 'get',
  });
// 文本聊天
export const textSession = (params) =>
  request({
    url: `/ais/chat/text`,
    method: 'post',
    params: params,
  });
// 语音转文本
export const audioTextStt = (params) =>
  request({
    url: `/ais/audio/stt`,
    method: 'post',
    data: params,
		headers: {
			'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundarynl6gT1BKdPWIejNq'
		}
  });
  // 文本转语音
export const audioStt = (params) =>
  request({
    url: `/ais/audio/tts-stream`,
    method: 'post',
    data: params,
    responseType: 'blob'
  });