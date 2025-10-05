<template>
  <div class="mainWrapper">
    <!-- 面包屑导航 - 左上角 -->
    <div class="container">
      <Breadcrumb data="直播间" class="breadcrumb" />
    </div>
    <div class="liveroomContainer">
      <el-container class="mainContent">
        <el-main>
          <!-- 主播信息 - 消息框上方 -->
          <div class="anchorInfo">
            <img :src="anchorInfo.avatar" class="anchorAvatar" alt="">
            <div class="anchorName">{{ anchorInfo.nickName }}</div>
            <div class="butCont fx-ct">
              <span class="bt-red Btn">关注</span>
              <span class="bt-blue Btn">分享</span>
            </div>
          </div>

          <!-- 视频和聊天区域左右布局 -->
          <div class="videoChatWrapper">
            <!-- 视频区域 -->
            <div class="videoSide">
              <div class="videoContainer">
                <video class="video-js" ref="videoplayer" width="100%"
                  style="background-color: rgb(18, 9, 37);width:100%;height:610px"></video>
              </div>
              <!-- 礼物面板 - 底部罗列 -->
              <div class="giftArea">
                <div class="giftList">
                  <div class="giftItem" v-for="item in giftList" :key="item.giftId">
                    <img @click="sendGift(item)" :src="item.coverImgUrl" class="giftImg" alt="">
                    <div class="giftItemName">{{ item.giftName }}</div>
                    <div class="giftItemPrice">{{ item.price }}金币</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 聊天区域 -->
            <div class="chatArea">
              <div class="talkContentBox" id="chatContentBox">
                <!-- 统一的消息容器，通过类型区分样式 -->
                <div 
                  v-for="(chatItem, index) in chatList" 
                  :key="index" 
                  class="message-item"
                  :class="{
                    'message-user': chatItem.msgType === 2,
                    'message-system': chatItem.msgType === 0,
                    'message-gift': chatItem.msgType === 5
                  }"
                >
                  <!-- 用户聊天消息 -->
                  <template v-if="chatItem.msgType === 2">
                    <span class="message-sender">{{ chatItem.senderName }}:</span>
                    <span class="message-content">{{ chatItem.content }}</span>
                  </template>
                  
                  <!-- 系统消息（进入房间） -->
                  <template v-if="chatItem.msgType === 0">
                    <span class="message-content">{{ chatItem.content }}</span>
                  </template>
                  
                  <!-- 礼物消息 -->
                  <template v-if="chatItem.msgType === 5">
                    <span class="gift-icon">🎁</span>
                    <span class="message-content">{{ chatItem.content }}</span>
                  </template>
                </div>
              </div>
              <div class="commentBox">
                <div v-if="userId">
                  <el-input v-model="comment" placeholder="发送直播评论" max="40"></el-input>
                </div>
                <span v-if="userId" class="bt sendBtn" style="margin-top: 10px;" @click="sendComment()">发送消息</span>
                <button class="loginPrompt" v-if="!userId" @click="login()">请先登录，才能开始聊天</button>
              </div>
            </div>
          </div>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox, ElMessage } from 'element-plus'
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import videojs from 'video.js'
import { useUserStore } from '@/store'
import { getWebSocket } from "@/utils/websocket"
import { getEmitter } from '@/utils/messageEmitter'

// 组件导入
import Breadcrumb from "@/components/Breadcrumb.vue";

// 路由与房间信息
const route = useRoute()
const router = useRouter()
const roomId = route.query.id
const url = 'http://192.168.150.101/hls/test.m3u8'

// 视频播放器
const videoplayer = ref(null)
const myPlayer = ref(null)

// 主播信息
const anchorInfo = ref({
  nickName: '主播信息加载中······',
  avatar: '/img/avatar.png'
})

// 用户信息
const store = useUserStore()
const userStore = ref(store.getUserInfo)
const userId = ref(userStore.value?.id || '')
const userName = ref(userStore.value?.name || '')

// 聊天相关
const comment = ref('')
const chatList = ref([])
let heartbeatInterval = null
let isConfirming = ref(false)

// 礼物相关
const giftList = ref([
  { giftId: '1', coverImgUrl: '/img/gift1.png', giftName: '大天使', price: '5' },
  { giftId: '2', coverImgUrl: '/img/gift2.png', giftName: '大皇冠', price: '10' },
  { giftId: '3', coverImgUrl: '/img/gift3.png', giftName: '爱心', price: '10' },
  { giftId: '4', coverImgUrl: '/img/gift4.png', giftName: '跑车', price: '20' },
  { giftId: '5', coverImgUrl: '/img/gift5.png', giftName: '彩虹', price: '20' },
  { giftId: '6', coverImgUrl: '/img/gift6.png', giftName: '大红心', price: '30' },
  { giftId: '7', coverImgUrl: '/img/gift7.png', giftName: '棒棒糖', price: '30' },
  { giftId: '8', coverImgUrl: '/img/gift8.png', giftName: '大黄鸭', price: '40' },
  { giftId: '9', coverImgUrl: '/img/gift9.png', giftName: '小灰兔', price: '40' },
  { giftId: '10', coverImgUrl: '/img/gift10.png', giftName: '飞机', price: '50' }
])

// 登录跳转方法
const login = () => {
  router.push('/login')
}

// 清理资源
onUnmounted(() => {
  if (myPlayer.value) {
    myPlayer.value.dispose()
  }
  clearInterval(heartbeatInterval)
})

// 初始化播放器
const initPlayer = () => {
  if (!myPlayer.value) {
    myPlayer.value = videojs(videoplayer.value, {
      autoplay: false,
      poster: "/img/loading.gif",
      controls: true,
      controlBar: true,
      bigPlayButton: true,
      sources: [{
        src: url
      }]
    }, () => {
      console.info("播放器加载完成")
    })
    myPlayer.value.on('error', (error) => {
      console.info('播放器加载错误', error)
    })
  }
}

// WebSocket相关
const socket = ref(null);
const emitter = getEmitter()

const scrollToBottom = () => {
  nextTick(() => {
    const chatBox = document.getElementById("chatContentBox")
    chatBox.scrollTop = chatBox.scrollHeight
  })
}

const initWebsocket = async () => {
  socket.value = await getWebSocket(userId.value ? userId.value : '');

  if (!socket.value) {
    ElMessage({
      showClose: true,
      message: 'IM服务暂未启动，请联系管理员',
      type: 'error',
    })
    return
  }

  // 发送进入房间消息
  const joinRoomMsg = {
    type: 0,
    roomId: roomId,
    fromUserId: userId.value ? userId.value : '',
    fromUserName: userName.value ? userName.value : '',
  }
  
  if (socket.value && socket.value.readyState === WebSocket.OPEN) {
    socket.value.send(JSON.stringify(joinRoomMsg))
  } else {
    setTimeout(() => {
      if (socket.value && socket.value.readyState === WebSocket.OPEN) {
        socket.value.send(JSON.stringify(joinRoomMsg))
      }
    }, 1000)
  }

  // 处理消息接收
  emitter.on("messageReceived", (genericMessage) => {
    console.info("收到消息", genericMessage)

    if (!genericMessage) return

    // 聊天消息
    if (genericMessage.type == 2 && genericMessage.roomId == roomId && genericMessage.body) {
      genericMessage.body.forEach(messagebody => {
        chatList.value.push({
          msgType: 2,
          senderName: messagebody.userName,
          content: messagebody.content
        })
        scrollToBottom()
      })
    }
    // 进入房间消息
    else if (genericMessage.type == 0 && genericMessage.roomId == roomId) {
      if (Array.isArray(genericMessage.body) && genericMessage.body.length > 0) {
        chatList.value.push({
          msgType: 0,
          content: genericMessage.body[0].content
        })
        scrollToBottom()
      }
    }
    // 礼物消息
    else if (genericMessage.type == 5 && genericMessage.roomId == roomId) {
      genericMessage.body.forEach(giftMessages => {
        chatList.value.push({
          msgType: 5,
          content: giftMessages.content
        })
        scrollToBottom()
      })
    }
  })

  // 心跳检测
  heartbeatInterval = setInterval(() => {
    try {
      if (socket.value && socket.value.readyState === WebSocket.OPEN) {
        socket.value.send("Heartbeat")
      }
    } catch (e) {
      if (!isConfirming.value) {
        isConfirming.value = true
        ElMessageBox.confirm(
          '长时间无操作，已退出IM聊天',
          '提示',
          {
            confirmButtonText: '重新恢复',
            showCancelButton: false,
            type: 'warning'
          }
        ).then(async () => {
          socket.value = await getWebSocket(userId.value ? userId.value : '')
          isConfirming.value = false
        })
      }
    }
  }, 20000)
};

onMounted(async () => {
  initPlayer();
  await initWebsocket();
});

// 发送评论
const sendComment = () => {
  if (comment.value) {
    const commentMsg = {
      type: 2,
      roomId: roomId,
      fromUserId: userId.value,
      fromUserName: userName.value,
      body: [{ 'content': comment.value }]
    }
    if (!socket.value || socket.value.readyState !== WebSocket.OPEN) {
      ElMessage.error('连接未就绪，请稍后再试');
      return;
    }
    socket.value.send(JSON.stringify(commentMsg));
    comment.value = '';
  }
};

// 发送礼物
const sendGift = (gift) => {
  if (!userId.value) {
    ElMessage({
      showClose: true,
      message: '未登录用户不能送礼。',
      type: 'error',
    })
    return;
  }
  
  const giftMsg = {
    type: 5,
    roomId: roomId,
    fromUserId: userId.value,
    fromUserName: userName.value,
    body: [{ 'content': `${userName.value}送给主播一个 ${gift.giftName}` }]
  }
  
  if (!socket.value || socket.value.readyState !== WebSocket.OPEN) {
    ElMessage.error('连接未就绪，请稍后再试');
    return;
  }
  socket.value.send(JSON.stringify(giftMsg));
};
</script>

<style lang="scss">
.mainWrapper {
  padding: 20px;
  box-sizing: border-box;
  background-color: #f5f5f5;
  min-height: 100vh;

  .breadcrumb {
    margin-bottom: 20px;
    display: inline-block;
  }

  .liveroomContainer {
    width: 100%;

    .mainContent {
      width: 100%;
      padding: 0;

      .anchorInfo {
        display: flex;
        align-items: center;
        padding: 15px;
        border: 1px solid #e0e0e0;
        border-radius: 8px;
        margin-bottom: 20px;
        background-color: #fff;

        .anchorAvatar {
          width: 50px;
          height: 50px;
          border-radius: 50%;
          border: 2px solid #ff8f19;
          margin-right: 15px;
        }

        .anchorName {
          color: #333;
          font-size: 16px;
          font-weight: 500;
          margin-right: 10px;
        }

        .butCont {
          margin-left: auto;
          padding: 0 20px;

          span {
            display: inline-block;
            width: 85px;
            text-align: center;
            height: 35px;
            line-height: 35px;
            border-radius: 20px;
            margin-left: 10px;
            cursor: pointer;
            background-color: #f0f0f0;
            transition: all 0.3s;

            &:hover {
              background-color: #e0e0e0;
            }
          }
        }
      }

      .videoChatWrapper {
        display: flex;
        gap: 20px;
        margin-bottom: 20px;
        height: 800px;
      }

      .videoSide {
        flex: 3;
        display: flex;
        flex-direction: column;
        gap: 20px;
      }

      .videoContainer {
        background-color: #120925;
        border-radius: 8px;
        overflow: hidden;
        flex: 1;

        video {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .giftArea {
        width: 100%;
        border: #120925 3px solid;
        border-radius: 8px;
        padding: 15px;
        box-sizing: border-box;

        .giftList {
          display: flex;
          gap: 10px;
          overflow-x: auto;
          padding-top: 10px;
          padding-bottom: 10px;

          .giftItem {
            display: flex;
            flex-direction: column;
            align-items: center;
            min-width: 80px;

            .giftImg {
              width: 80px;
              height: 80px;
              border: 2px solid transparent;
              border-radius: 5px;
              cursor: pointer;
              transition: all 0.3s;

              &:hover {
                border-color: #ffa925;
                transform: scale(1.05);
              }
            }

            .giftItemName {
              color: #fff;
              font-size: 15px;
              margin-top: 5px;
            }

            .giftItemPrice {
              color: #ffa84c;
              font-size: 12px;
            }
          }
        }
      }

      .chatArea {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 10px;
        height: 100%;

        .talkContentBox {
          flex: 1;
          background-color: #fff;
          border: 1px solid #e5e5e5;
          border-radius: 8px;
          overflow-y: auto;
          padding: 15px;
          box-sizing: border-box;

          // 统一消息容器样式
          .message-item {
            margin-bottom: 12px;
            line-height: 1.5;
            font-size: 14px;
            padding: 5px 0;
          }

          // 用户聊天消息样式
          .message-user {
            .message-sender {
              color: #409eff;
              font-weight: 500;
              margin-right: 8px;
            }
            .message-content {
              color: #333;
            }
          }

          // 系统消息样式
          .message-system {
            text-align: center;
            .message-content {
              color: #8c8c8c;
              font-size: 13px;
              background-color: #f5f5f5;
              padding: 3px 10px;
              border-radius: 12px;
            }
          }

          // 礼物消息样式
          .message-gift {
            text-align: center;
            .gift-icon {
              color: #ff4d4f;
              margin-right: 5px;
            }
            .message-content {
              color: #e6a23c;
              font-weight: 500;
            }
          }
        }

        .commentBox {
          background-color: #fff;
          border: 1px solid #e5e5e5;
          border-radius: 8px;
          padding: 15px;
          box-sizing: border-box;

          .el-input {
            margin-bottom: 10px;
          }

          .sendBtn {
            background-color: #1890ff;
            color: #fff;
            border: none;
            padding: 8px 20px;
            border-radius: 4px;
            cursor: pointer;
            display: inline-block;
            transition: background-color 0.3s;

            &:hover {
              background-color: #096dd9;
            }
          }

          .loginPrompt {
            width: 100%;
            text-align: center;
            padding: 15px 0;
            color: #666;
            background-color: #f5f5f5;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.3s;

            &:hover {
              background-color: #e9e9e9;
            }
          }
        }
      }
    }
  }

  // 红包相关样式
  .redPacketWrap {
    padding: 0;
    margin: 0;
    overflow: hidden;
    height: 100%;
    width: 100%;
    z-index: 9999;

    #wrapper {
      img {
        position: absolute;
        transform: translateY(-120%);
        animation: dropDowm 2s forwards;
        z-index: 9999;
        top: -100px;
      }
    }

    @keyframes dropDowm {
      0% {
        top: 0px;
        transform: translateY(-100%) rotate(0deg);
      }

      100% {
        top: 60%;
        transform: translateY(0%) rotate(360deg);
      }
    }

    #modol {
      display: none;

      &::before {
        content: '';
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, 0.5);
      }
    }

    #hb {
      width: 350px;
      height: 450px;
      border-radius: 20px;
      background-color: #e7223e;
      color: #fad755;
      position: fixed;
      left: 50%;
      top: 50%;
      margin-top: -225px;
      margin-left: -175px;
      font-size: 30px;
      font-weight: 900;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      z-index: 10000;

      #btn {
        background-color: #fad755;
        color: #e7223e;
        font-size: 18px;
        margin-top: 10px;
        padding: 10px;
        border: none;
        outline: none;
        cursor: pointer;
      }
    }
  }
}
</style>