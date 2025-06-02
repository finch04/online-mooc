import request from "@/utils/request.js";
// 获取列表
export const getMedia = (params) =>
  request({
    url: `/ms/medias`,
    method: "get",
    params,
  });
// 删除媒资
export const deleteMedia = (id) =>
  request({
    url: `/ms/medias/${id}`,
    method: "delete",
  });
// 批量删除媒资视频
export const deleteMediaAll = (params) =>
  request({
    url: `/ms/medias`,
    method: "delete",
    params,
  });
// 获取上传视频的授权签名
export const getMediaUpload = (params) =>
  request({
    url: `/ms/medias/signature/upload`,
    method: "get",
    params,
  });
// 上传后的视频传到后台
export const mediaUpload = (params) =>
  request({
    url: `/ms/medias`,
    method: "post",
    data:params,
  });
// 管理端获取预览视频的授权签名
export const getMediasSignature = (params) =>
  request({
    url: `/ms/medias/signature/preview`,
    method: "get",
    params,
  });

// 文件管理相关 API
// 获取文件列表
export const getFiles = (params) =>
  request({
    url: `/ms/files`,
    method: "get",
    params,
  });

// 上传文件
export const uploadFile = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return request({
    url: `/ms/files`,
    method: "post",
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};

// 获取文件信息
export const getFileInfo = (id) =>
  request({
    url: `/ms/files/${id}`,
    method: "get",
  });

// 删除文件
export const deleteFile = (id) =>
  request({
    url: `/ms/files/${id}`,
    method: "delete",
  });

//分片上传文件 相关api
// 上传前提交注册
export const upRegister = (params) =>
  request({
    url: `/ms/files/upload/checkfile`,
    method: "post",
    params,
  });

// 上传分块前检查
export const checkchunk = (params) =>
  request({
    url: `/ms/files/upload/checkchunk`,
    method: "post",
    params,
  });

// 分块上传
export const upChunk = (params) =>
  request({
    url: `/ms/files/upload/uploadchunk`,
    method: "post",
    data:params,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  });

// 提交合并
export const mergeChunks = (params) =>
  request({
    url: `/ms/files/upload/mergechunks`,
    method: "post",
    params,
  });