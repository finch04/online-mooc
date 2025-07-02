import { defineStore } from 'pinia';
import { TOKEN_NAME } from '@/config/global';
import  store  from '@/store';
import {tryRefreshToken} from "@/utils/refreshToken";
const InitUserInfo = {};
export const useUserStore = defineStore('user', {
  id:"userInfo",
  state: () => ({
    token: sessionStorage.getItem(TOKEN_NAME) , // 使用 || 'main_token' 默认token不走权限
    userInfo: JSON.parse(sessionStorage.getItem("userInfo")),
    sessionInfo: sessionStorage.getItem("sessionInfo")!=='undefined'?JSON.parse(sessionStorage.getItem("sessionInfo")):{},
    // sessionInfo: sessionStorage.getItem("sessionInfo"),
    chatDataList:JSON.parse(sessionStorage.getItem("chatDataList")),
    orderData:sessionStorage.getItem("orderData")
  }),
  persist: {
    enabled: true,
    strategies: [
      { storage: sessionStorage, paths: ["token", "userInfo","sessionInfo","chatDataList",'orderData'] },
    ],
  },
  getters: {
    getToken: (state) => {
      return state.token;
    },
    getUserInfo: (state) => {
      return state.userInfo;
    },
    getSessionInfo: (state) => {
      console.log(state.sessionInfo)
      return state.sessionInfo;
    },
    getChatDataList: (state) => {
      
      return state.chatDataList;
    },
    getOrderData: (state) => {
      return state.orderData;
    },
  },
  actions: {
    // 记录用户token
    async setToken(token) {
      this.token = token;
      sessionStorage.setItem(TOKEN_NAME, token)
    },
    // 记录登录时获取的用户信息
    async setUserInfo(userInfo) {
      this.userInfo = userInfo;
      sessionStorage.setItem('userInfo', JSON.stringify(userInfo));
    },
    async logout() {
      sessionStorage.removeItem(TOKEN_NAME);
      sessionStorage.removeItem('userInfo');
      this.token = '';
      this.userInfo = InitUserInfo;
    },
    async removeToken() {
      this.token = '';
    },
    // ai新建聊天窗口id
    async setSessionInfo(sessionInfo) {
      this.sessionInfo = sessionInfo;
      sessionStorage.setItem('sessionInfo', JSON.stringify(sessionInfo));
    },
    // 新建聊天记录
    async setChatDataList(chatDataList) {
      this.chatDataList = chatDataList
      sessionStorage.setItem('chatDataList', JSON.stringify(chatDataList));
    },
    // 新建聊天记录
    async setOrderData(orderData) {
      this.orderData = orderData
      sessionStorage.setItem('orderData', orderData);
    },
  },
  
});
async function refresh(){
  return await tryRefreshToken();
}
export function getToken() {
  return useUserStore(store);
}
export function isLogin(token) {
  if(!!sessionStorage.getItem(TOKEN_NAME)){
    return true;
  }
  return refresh()
}
