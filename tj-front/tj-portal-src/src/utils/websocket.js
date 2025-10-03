import {getEmitter} from './messageEmitter'
import { getIMServerUrl } from "@/api/live.js";
  
let socket;
   
// const getWebSocket = async (url,userId) => {  

const getWebSocket = async (userId) => {  
console.info("getWebSocket",userId,socket)
  if (socket && socket.isOpen()) {
    // 如果已经存在连接，则不重新创建  
    return;  
  }
  //如果传递了userId，就将userId作为后缀添加到url上。
  // if(userId){
  //   url += '/'+userId
  // }else{
  //   url += '/'+generateRandomString(8)
  // }
  if(!userId){
    userId = generateRandomString(8)
  }
  let res = await getIMServerUrl(userId)
  console.info("res ",res)
  if(res){
    socket = new WebSocket(res.url,res.imToken);
  
    // ... 其他WebSocket事件监听器  
    socket.onopen = () =>{
      console.info('建立Websocket链接')
    }
    socket.onmessage = (event) => {  
    console.info("收到websocket消息：",event.data)
      try{
        const genericMessage = JSON.parse(event.data);
    console.info("推送websocket消息：",genericMessage)    
        // 触发一个事件
        getEmitter().emit('messageReceived', genericMessage);  
      }catch(e){
        console.info("收到非标准消息:",event.data)
      }
    };  
    socket.onclose=() =>{
      console.info('关闭Websocket链接')
    }
    return socket;
  }else{
    console.info("IM服务暂未启动")
    return socket;
  }
};  
  
const closeWebSocket = () => {  
  if (socket) {  
    socket.close();  
    socket = null;  
  }  
};  


function generateRandomString(length) {
  let result = '';
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';

  for (let i = 0; i < length; i++) {
      result += characters.charAt(Math.floor(Math.random() * characters.length));
  }

  return result;
}

  
export { getWebSocket,closeWebSocket };