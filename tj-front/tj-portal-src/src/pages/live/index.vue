<template>
  <div class="mainWrapper">
    <!-- 面包屑导航与直播间信息 -->
    <div class="container">
      <div class="header-info">
        <div class="room-header">
          <img :src="liveRoomDetail.roomCover" class="room-cover" alt="直播间封面">
          <div class="room-title-container">
            <h2 class="room-title">{{ liveRoomDetail.roomTitle }}</h2>
            <div class="room-status">
              <span v-if="liveRoomDetail.status === 1" class="status-live">直播中</span>
              <span v-if="liveRoomDetail.status === 0" class="status-offline">未开播</span>
              <span v-if="liveRoomDetail.status === 2" class="status-closed">已关闭</span>
              <span v-if="liveRoomDetail.status === 3" class="status-banned">禁播</span>
              <span v-if="liveRoomDetail.isPrivate" class="private-tag">私有直播间</span>
            </div>
            <p class="room-desc">{{ liveRoomDetail.roomDesc }}</p>
            <!-- <p class="room-notice"><span class="notice-label">公告：</span>{{ liveRoomDetail.roomNotice || '暂无公告' }}</p> -->
          </div>
          <div class="butCont fx-ct">
            <span class="bt bt-round" style="padding:4px;width: 100px;" @click="goHome()">返回首页</span>
          </div>
        </div>
      </div>
    </div>
    <div class="liveroomContainer">
      <el-container class="mainContent">
        <el-main>
          <!-- 主播信息与直播间信息合并区域 -->
          <div class="info-bar">
            <div class="anchor-info">
              <img :src="liveRoomDetail.anchorIcon" class="anchor-avatar" alt="主播头像">
              <div class="anchor-detail">
                <h3 class="anchor-name">{{ liveRoomDetail.anchorName }}</h3>
                <p class="room-meta">
                  <span class="meta-item">开播时间：{{ formatTime(liveRoomDetail.createTime) }}</span>
                  <span class="meta-item">最后更新：{{ formatTime(liveRoomDetail.updateTime) }}</span>
                </p>
              </div>
            </div>

            <div class="room-stats">
              <div class="stat-item">
                <span class="stat-value">{{ liveRoomDetail.onlineCount || 0 }}</span>
                <span class="stat-label">当前在线</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ liveRoomDetail.maxOnlineCount || 0 }}</span>
                <span class="stat-label">最高在线人数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ liveRoomDetail.likeCount || 0 }}</span>
                <span class="stat-label">总点赞</span>
              </div>
              <!-- <div class="stat-item">
                <span class="stat-value">{{ liveRoomDetail.shareCount || 0 }}</span>
                <span class="stat-label">分享次数</span>
              </div> -->
            </div>

            <div class="action-buttons">
              <span class="bt Btn" @click="handleLike">👍点赞</span>
              <span class="bt-red Btn" @click="handleFollow">{{ liveRoomDetail.followed ? '已关注' : '关注' }}</span>
            </div>
          </div>

          <!-- 视频和聊天区域左右布局 -->
          <div class="videoChatWrapper">
            <!-- 视频区域 -->
            <div class="videoSide">
              <div class="videoContainer">
                <video class="video-js" ref="videoplayer" width="100%"
                  style="background-color: rgb(18, 9, 37);width:100%;height:600px"></video>
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
                <div v-for="(chatItem, index) in chatList" :key="index" class="message-item" :class="{
                  'message-user': chatItem.msgType === 2,
                  'message-system': chatItem.msgType === 0,
                  'message-gift': chatItem.msgType === 5,
                  'message-notice': chatItem.msgType === 6
                }">
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

                  <!-- 直播间公告 -->
                  <template v-if="chatItem.msgType === 6">
                    <span class="notice-icon">📢</span>
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
import { getLiveRoomById,getLiveRoomOnlineCount } from '@/api/live'

import { closeWebSocket } from '@/utils/websocket'
import { onBeforeRouteLeave } from 'vue-router'

// 路由离开当前页面时关闭连接
onBeforeRouteLeave((to, from, next) => {
  closeWebSocket()
  next()
})

// 浏览器关闭时关闭连接
window.addEventListener('beforeunload', () => {
  closeWebSocket()
})

// 组件卸载时也关闭连接（作为双重保障）
onUnmounted(() => {
  emitter.off("closeWebsocket", handleCloseWebsocket); // 精准移除当前监听器
  closeWebSocket()
  if (myPlayer.value) {
    myPlayer.value.dispose()
  }
  clearInterval(heartbeatInterval)
})

import 'video.js/dist/video-js.min.css'
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

// 直播间详情信息
const liveRoomDetail = ref({
  anchorId: '',
  anchorName: '加载中...',
  anchorIcon: '/img/avatar.png',
  roomTitle: '直播间标题加载中...',
  roomDesc: '',
  roomNotice: '',
  roomCover: '/img/default-cover.png',
  status: 0,
  onlineCount: 0,
  maxOnlineCount: 0,
  likeCount: 0,
  shareCount: 0,
  isPrivate: false,
  followed: false,
  createTime: '',
  updateTime: ''
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

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '未知'
  const date = new Date(timeStr)
  return date.toLocaleString()
}

// 登录跳转方法
const login = () => {
  router.push('/login')
}
const goHome = () => {
  router.push('/')
}

// 关注主播
const handleFollow = () => {
  // 实际项目中需要调用关注接口
  liveRoomDetail.value.followed = !liveRoomDetail.value.followed
  ElMessage.success(liveRoomDetail.value.followed ? '关注成功' : '取消关注成功')
}

// 点赞直播间
const handleLike = () => {
  // 实际项目中需要实现点赞逻辑
  ElMessage.info('点赞功能开发中...')
}



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

// 获取直播间详情
const getLiveRoomDetail = async () => {
  try {
    const res = await getLiveRoomById(roomId)
    if (res.data) {
      liveRoomDetail.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取直播间信息失败')
    console.error('获取直播间详情错误:', error)
  }
}
//3秒轮询 获取实时在线人数
const getLiveRoomOnlineCountInterval = () => { 
  setInterval(async () => {
    try {
      const res = await getLiveRoomOnlineCount(roomId)
      if (res.data) {
        liveRoomDetail.value.onlineCount = res.data
      }
    } catch (error) {
      console.error('获取实时在线人数错误:', error)
    }
  }, 3000)
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
  socket.value = await getWebSocket(roomId,userId.value ? userId.value : '');

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
    // 直播间公告消息
    else if (genericMessage.type == 6 && genericMessage.roomId == roomId) {
      if (Array.isArray(genericMessage.body) && genericMessage.body.length > 0) {
        genericMessage.body.forEach(notice => {
          chatList.value.push({
            msgType: 6,
            content: notice.content
          })
          scrollToBottom()
        })
      }
    }
  })

  heartbeatInterval = setInterval(() => {
    try {
      console.log("心跳检测将发起")
      if (socket.value && socket.value.readyState === WebSocket.OPEN) {
        socket.value.send("Heartbeat")
      } else {
        handleDisconnect()
      }
    } catch (e) {
      console.error("心跳检测出错:", e)
      handleDisconnect()
    }
  }, 20000) // 发送心跳包的间隔必须大于后端设置最长心跳时间的间隔 先设置20秒
}
// 处理断开连接的统一方法
const handleDisconnect = () => {
  if (!isConfirming.value) {
    isConfirming.value = true

    // 显示提示对话框
    const messageBox = ElMessageBox({
      message: '长时间无操作，已退出IM聊天，将在3秒后返回首页',
      title: '提示',
      confirmButtonText: '立即刷新',
      showCancelButton: false,
      type: 'warning',
      closeOnClickModal: false,
      closeOnPressEscape: false
    })

    // 3秒后自动返回首页
    const timer = setTimeout(() => {
      // 返回首页的逻辑，这里假设首页路由是'/'
      window.location.href = '/'
      clearTimeout(timer)
    }, 3000)

    // 处理立即刷新按钮
    messageBox.then(async () => {
      clearTimeout(timer) // 清除自动返回的计时器
      // 刷新当前页面
      window.location.reload()
    }).finally(() => {
      isConfirming.value = false
    })
  }
}

// 心跳监听
const handleCloseWebsocket = () => {
  ElMessage({
    message: 'WebSocket连接已关闭',
    type: 'info',
    showClose: true
  });
};

onMounted(async () => {
  await getLiveRoomDetail() // 先获取直播间详情
  getLiveRoomOnlineCountInterval()
  initPlayer();
  await initWebsocket();// 绑定事件并记录监听器
  // 绑定事件并记录监听器
  emitter.on("closeWebsocket", handleCloseWebsocket);
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
      message: '未登录用户不能送礼',
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

<style lang="scss" src="./index.scss"></style>
