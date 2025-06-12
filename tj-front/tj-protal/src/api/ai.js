import request from "@/utils/request.js"
const AI_API_PREFIX = "/ct"

// 用户会话模块

//新增会话
export const createUserSession = (data) =>
    request({
        url: `${AI_API_PREFIX}/session`,
        method: 'post',
        data
    })
//查询用户会话列表
export const getUserSessionList = () =>
    request({
        url: `${AI_API_PREFIX}/session/list`,
        method: 'get',
    })
//更改对话
export const updateUserSession = (id,params) =>
    request({
        url: `${AI_API_PREFIX}/session/${id}`,
        method: 'put',
        params
    })
//删除会话
export const deleteUserSession = (id) =>
    request({
        url: `${AI_API_PREFIX}/session/${id}`,
        method: 'delete',
    })

//聊天接口

//根据会话id获取聊天记录
export const getChatRecord = (params) =>
    request({
        url: `${AI_API_PREFIX}/chat/records`,
        method: 'get',
        params
    })

// AI聊天接口
export const memoryChatRedis = (params) =>
    request({
        url: `${AI_API_PREFIX}/chat/simple`,
        method: 'get',
        params
    })
//AI流式聊天接口
export const memoryChatRedisStream = (params) =>
    request({
        url: `${AI_API_PREFIX}/chat/`,
        method: 'get',
        params,
        responseType: 'stream' 
    })

// 知识库接口

// 上传文件到知识库
export const uploadMarkdown = (file, level = 2) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('level', level);
    return request({
    url: `${AI_API_PREFIX}/file/upload`,
    method: 'post',
    data: formData,
    headers: {
        'Content-Type': 'multipart/form-data'
    }
    });
};

// 根据知识库内容对话
export const chatByMarkdownDoc = (params) =>
    request({
    url: `${AI_API_PREFIX}/file/chat`,
    method: 'get',
    params
    });

// 分页查询用户知识库文件列表
export const queryMarkdownPage = (query) =>
    request({
    url: `${AI_API_PREFIX}/file/page`,
    method: 'get',
    query
    });

// 根据文件 id 查看文件内容
export const getMarkdown = (fileId) =>
    request({
    url: `${AI_API_PREFIX}/file/${fileId}`,
    method: 'get'
    });

// 更新文件内容
export const updateMarkdown = (markdownDocs) =>
    request({
    url: `${AI_API_PREFIX}/file/update`,
    method: 'put',
    data: markdownDocs
    });

// 根据文件 id 删除文件
export const deleteMarkdown = (fileId) =>
    request({
    url: `${AI_API_PREFIX}/file/${fileId}`,
    method: 'delete'
});