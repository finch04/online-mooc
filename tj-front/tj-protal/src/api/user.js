import request from "@/utils/request.js"
const USER_API_PREFIX = "/us"
const AUTH_API_PREFIX = "/as"
const WX_LOGIN_TYPE = 3;
const PHONE_LOGIN_TYPE = 2;
const PW_LOGIN_TYPE = 1;
// 微信登录
export const wxLogins = (data) => {
	// data.type = WX_LOGIN_TYPE;
	return request({
		url: `${AUTH_API_PREFIX}/accounts/wxLogin`,
		method: "post",
		data,
		withCredentials: true
	});
}
//微信登录 保存UUID到Redis
export const saveWxUuid = (uuid) => {
    return request({
        url: `${AUTH_API_PREFIX}/accounts/wx/saveUuid`,
        method: "post",
        data: { uuid },
        withCredentials: true
    });
}

//微信登录 检查微信登录状态
export const checkWxLoginStatus = (uuid) => {
    return request({
        url: `${AUTH_API_PREFIX}/accounts/wx/check`,
        method: "get",
        params: { uuid },
        withCredentials: true
    });
}
// 手机号验证码登录
export const phoneLogins = (data) => {
	data.type = PHONE_LOGIN_TYPE;
	return request({
		url: `${AUTH_API_PREFIX}/accounts/login`,
		method: "post",
		data,
		withCredentials: true
	});
}
// 账号登录
export const userLogins = (data) => {
	data.type = PW_LOGIN_TYPE;
	return request({
		url: `${AUTH_API_PREFIX}/accounts/login`,
		method: "post",
		data,
		withCredentials: true
	});
}


// 发送验证码
export const verifycode = (params) =>
request({
	url: `${AUTH_API_PREFIX}/code/verifycode`,
	method: 'post',
	params
})
// 找回密码
export const resetPassword = (params) =>
	request({
		url: `${AUTH_API_PREFIX}/users/reset`,
		method: 'post',
		data:params
	})
// 账号注册
export const userRegist = (params) =>
request({
	url: `${AUTH_API_PREFIX}/users/register`,
	method: 'post',
	data:params
})
// 获取用户信息
export const getUserInfo = (params) =>
	request({
		url: `${USER_API_PREFIX}/users/me`,
		method: 'get',
		params
	})	
// 更改用户信息
export const updateUserInfo = data =>
	request({
		url: `${USER_API_PREFIX}/students`,
		method: 'put',
		data
	})


// 用户解绑手机号  发送验证码
export const sendSms = (params) =>
	request({
		url: `${USER_API_PREFIX}/students/sendSms`,
		method: 'post',
		params
	})


// 用户更新绑定手机号  
export const bindPhone = (params) =>
	request({
		url: `${USER_API_PREFIX}/students/updateBindPhone`,
		method: 'post',
		params
	})


// 用户修改密码
export const updatePassword =  data =>
	request({
		url: `${USER_API_PREFIX}/students/updatePassword`,
		method: 'put',
		data
	})
	
// 账号退出登录
export const userLogout = () => {
	return request({
		url: `${AUTH_API_PREFIX}/accounts/logout`,
		method: "post",
		withCredentials: true
	});
}