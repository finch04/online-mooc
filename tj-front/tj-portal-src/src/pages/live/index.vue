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
                <span class="stat-value">{{ liveRoomDetail.fansCount || 0 }}</span>
                <span class="stat-label">主播粉丝数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ liveRoomDetail.onlineCount || 0 }}</span>
                <span class="stat-label">直播间当前在线人数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ liveRoomDetail.maxOnlineCount || 0 }}</span>
                <span class="stat-label">直播间最高在线人数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ liveRoomDetail.likeCount || 0 }}</span>
                <span class="stat-label">直播间总点赞数</span>
              </div>
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
                <!-- 加载动画：默认显示，直播中且流加载成功后隐藏 -->
                <div class="video-loading" v-show="showLoading"
                  :class="{ 'loading-low-zindex': liveRoomDetail.status === 1 }">
                  <img src="/img/loading.gif" alt="加载中" class="loading-img">
                  <p class="loading-text">{{ loadingText }}</p>
                </div>
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
import { nextTick, onMounted, onUnmounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import videojs from 'video.js'
import { useUserStore } from '@/store'
import { getWebSocket } from "@/utils/websocket"
import { getEmitter } from '@/utils/messageEmitter'
import { getLiveRoomById, getStat, follow, like } from '@/api/live'

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

// 动态生成直播URL：依赖liveRoomDetail中的streamKey
const liveUrl = computed(() => {
  // 若streamKey为空，返回空字符串（避免无效URL）
  if (!liveRoomDetail.value?.streamKey) return ''
  // 拼接HLS格式的直播URL（根据后端实际协议调整，如RTMP则改为rtmp://xxx）
  return `http://192.168.150.101/hls/${liveRoomDetail.value.streamKey}.m3u8`
})

// 视频播放器
const videoplayer = ref(null)
const myPlayer = ref(null)

// 直播间详情信息
const liveRoomDetail = ref({
  streamKey: '',
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
  isPrivate: false,
  followed: false,
  fansCount: 0,
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

// 加载动画&直播间提示相关
const showLoading = ref(true)
const loadingText = computed(() => {
  const status = liveRoomDetail.value.status
  // 根据直播间状态返回不同提示
  if (status === 0) return '直播间未开播，正在等待主播上线...'
  if (status === 2) return '直播间已关闭，无法观看'
  if (status === 3) return '直播间已被禁播，无法观看'
  // 直播中但流未加载成功：显示“直播加载中”
  return '直播加载中，请稍候...'
})
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

// 关注/取消关注主播
const handleFollow = () => {
  const params = {
    follow: !liveRoomDetail.value.followed ? 1 : 0,
    anchorId: liveRoomDetail.value.anchorId
  }
  follow(params).then(res => {
    if (res.code === 200) {
      liveRoomDetail.value.followed = !liveRoomDetail.value.followed
      if (liveRoomDetail.value.followed) {
        liveRoomDetail.value.fansCount++
      } else {
        liveRoomDetail.value.fansCount--
      }
      ElMessage.success(liveRoomDetail.value.followed ? '关注成功' : '取消关注成功')
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 点赞直播间
const handleLike = () => {
  like(roomId).then(res => {
    if (res.code === 200) {
      liveRoomDetail.value.likeCount++
      ElMessage.success('点赞成功')
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const initPlayer = () => {
  // 1. 非直播状态处理
  if ([0, 2, 3].includes(liveRoomDetail.value.status)) {
    showLoading.value = true;
    console.warn('非直播状态，不初始化播放器', liveRoomDetail.value.status);
    return;
  }

  // 2. 直播地址无效处理
  if (!liveUrl.value) {
    showLoading.value = true;
    ElMessage.warning('直播流地址无效，请联系管理员');
    return;
  }

  // 清理旧播放器实例
  if (myPlayer.value) {
    myPlayer.value.dispose();
    myPlayer.value = null;
  }

  // 3. 创建新播放器实例
  myPlayer.value = videojs(videoplayer.value, {
    autoplay: false,
    // poster: "transparent",
    controls: true,
    controlBar: true,
    bigPlayButton: true,
    sources: [{ 
      src: liveUrl.value, 
      type: 'application/x-mpegURL' 
    }],
    html5: {
      hls: {
        maxBufferLength: 30,
        maxMaxBufferLength: 600
      }
    }
  }, function onPlayerReady() {
    videojs.log('播放器初始化完成!');
    // 初始化时显示加载动画
    showLoading.value = true;
  });

  // 4. 资源请求相关事件 - 控制加载动画显示
  myPlayer.value.on("loadstart", function() {
    console.log("开始请求数据");
    showLoading.value = false; // 开始请求时显示加载动画
  });

  myPlayer.value.on("progress", function() {
    console.log("正在请求数据中");
    showLoading.value = false; // 数据请求过程中保持显示
  });

  myPlayer.value.on("waiting", function() {
    console.log("等待数据加载");
    showLoading.value = false; // 等待数据时显示加载动画
  });

  myPlayer.value.on("stalled", function() {
    console.log("网速异常，数据加载中断");
    showLoading.value = true; // 网络异常时显示加载动画
  });

  // 5. 资源加载完成相关事件 - 控制加载动画隐藏
  myPlayer.value.on("loadedmetadata", function() {
    console.log("获取资源长度完成");
    showLoading.value = false; // 元数据加载完成，隐藏动画
  });

  myPlayer.value.on("canplaythrough", function() {
    console.log("视频源数据加载完成，可流畅播放");
    showLoading.value = false; // 资源完全加载，隐藏动画
  });

  myPlayer.value.on("play", function() {
    console.log("视频开始播放");
    showLoading.value = false; // 开始播放时隐藏动画
  });

  myPlayer.value.on("playing", function() {
    console.log("视频播放中");
    showLoading.value = false; // 播放中保持隐藏
  });

  // 6. 错误处理
  myPlayer.value.on("error", function() {
    console.log("加载错误");
    showLoading.value = true; // 错误时显示动画
    const error = myPlayer.value.error();
    let errorMessage = '直播加载失败，请重试';
    if (error) {
      switch(error.code) {
        case 2: errorMessage = '无法加载视频源'; break;
        case 4: errorMessage = '无法解析视频源'; break;
        case 10: errorMessage = '视频加载超时'; break;
        default: errorMessage = `错误代码: ${error.code}`;
      }
    }
    ElMessage.error(errorMessage);
  });

  // 7. 播放控制相关事件
  myPlayer.value.on("pause", function() {
    console.log("视频暂停播放");
    // 暂停时不改变加载动画状态
  });

  myPlayer.value.on("ended", function() {
    console.log("视频播放结束");
    showLoading.value = true; // 播放结束重新显示动画
  });

  myPlayer.value.on("seeking", function() {
    console.log("视频跳转中");
    showLoading.value = true; // 跳转时显示动画
  });

  myPlayer.value.on("seeked", function() {
    console.log("视频跳转结束");
    showLoading.value = false; // 跳转完成隐藏动画
  });

  // 8. 播放按钮点击事件（最终保障）
  const bigPlayButton = myPlayer.value.getChild('bigPlayButton');
  if (bigPlayButton) {
    bigPlayButton.on('click', () => {
      console.log('播放按钮被点击');
      showLoading.value = false;
    });
  }
};




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
const getLiveRoomStatInterval = () => {
  setInterval(async () => {
    try {
      const res = await getStat(roomId)
      if (res.data) {
        // liveRoomDetail.value.fansCount = res.data.fansCount
        liveRoomDetail.value.onlineCount = res.data.onlineCount
        liveRoomDetail.value.maxOnlineCount = res.data.maxOnlineCount
        liveRoomDetail.value.likeCount = res.data.likeCount
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
  socket.value = await getWebSocket(roomId, userId.value ? userId.value : '');

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
  initPlayer();
  getLiveRoomStatInterval()
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
