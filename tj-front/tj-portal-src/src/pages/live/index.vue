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
              <span class="bt Btn">关注</span>
              <span class="bt Btn">分享</span>
            </div>
          </div>

          <!-- 视频和聊天区域左右布局 -->
          <div class="videoChatWrapper">
            <!-- 视频区域 -->
            <div class="videoContainer">
              <video class="video-js" ref="videoplayer" width="100%"
                style="background-color: rgb(18, 9, 37);width:100%;height:550px"></video>
            </div>

            <!-- 聊天区域 -->
            <div class="chatArea">
              <div class="talkContentBox" id="chatContentBox">
                <div class="chatContent giftMsg" v-for='chatItem in chatList' :key="chatItem"
                  v-show="chatItem.msgType == 5">
                  {{ chatItem.content }}
                </div>
                <div class="chatContent" v-for='chatItem in chatList' :key="chatItem" v-show="chatItem.msgType == 2">
                  <span class="userName">{{ chatItem.senderName }} :</span>
                  <span>{{ chatItem.content }}</span>
                </div>
                <div class="chatContent systemMsg" v-for='chatItem in chatList' :key="chatItem"
                  v-show="chatItem.msgType == 0">
                  {{ chatItem.content }}
                </div>
              </div>
              <div class="commentBox">
                <el-form v-if="userId">
                  <el-form-item>
                    <el-input v-model="comment" placeholder="发送直播评论"></el-input>
                  </el-form-item>
                  <el-form-item style="text-align:right;">
                    <el-button class="sendBtn" @click="sendComment()">发送消息</el-button>
                  </el-form-item>
                </el-form>
                <button class="loginPrompt" v-if="!userId">请先登录，才能开始聊天</button>
              </div>
            </div>
          </div>

          <!-- 礼物面板 - 底部罗列 -->
          <div class="giftArea">
            <div class="giftContentTitle">礼物面板</div>
            <div class="bankTab">
              <span @click="toShowCarTab()">查看购物车</span>
              <span @click="showBankInfoTab()">钱包余额:</span>
              <span>{{ currentBalance }}</span>
            </div>
            <div class="giftList">
              <div class="giftItem" v-for="item in giftList" :key="item.giftId">
                <img @click="sendGift(item)" :src="item.coverImgUrl" class="giftImg" alt="">
                <div class="giftItemName">{{ item.giftName }}</div>
                <div class="giftItemPrice">{{ item.price }}金币</div>
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
import { useRoute } from 'vue-router'
import videojs from 'video.js'
import { useUserStore } from '@/store'
import { getWebSocket } from "@/utils/websocket"
import { getEmitter } from '@/utils/messageEmitter'

// 组件导入
import Breadcrumb from "@/components/Breadcrumb.vue";

// 路由与房间信息
const route = useRoute()
const roomId = route.params.id
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
let socket = null
let heartbeatInterval = null

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
const currentBalance = ref(0)
const showBankInfo = ref(false)

// 初始化播放器
onMounted(() => {
  initPlayer()
  initWebsocket()
})

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
      poster: "/img/2.jpeg",
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

// 初始化WebSocket
const initWebsocket = () => {
  socket = getWebSocket(userId.value)

  const emitter = getEmitter()
  emitter.on("messageReceived", (genericMessage) => {
    if (!genericMessage || !genericMessage.type) return

    // 聊天消息
    if (genericMessage.type == 2 && genericMessage.roomId == roomId && genericMessage.body) {
      genericMessage.body.forEach(msg => {
        chatList.value.push({
          msgType: 2,
          senderName: msg.userName,
          content: msg.content
        })
      })
      scrollToBottom()
    }
    // 进入房间消息
    else if (genericMessage.type == 0 && genericMessage.roomId == roomId) {
      chatList.value.push({
        msgType: 0,
        content: `${genericMessage.fromUserName} 进入房间`
      })
      scrollToBottom()
    }
    // 礼物消息
    else if (genericMessage.type == 5 && genericMessage.roomId == roomId) {
      genericMessage.body.forEach(giftMsg => {
        chatList.value.push({
          msgType: 5,
          content: giftMsg.content
        })
      })
      scrollToBottom()
    }
  })
}

// 滚动到最新消息
const scrollToBottom = () => {
  nextTick(() => {
    const chatBox = document.getElementById("chatContentBox")
    chatBox.scrollTop = chatBox.scrollHeight
  })
}

// 发送评论
const sendComment = () => {
  if (!comment.value) return

  const commentMsg = {
    type: 2,
    roomId: roomId,
    fromUserId: userId.value,
    fromUserName: userName.value,
    body: [{ content: comment.value }]
  }
  socket.send(JSON.stringify(commentMsg))
  comment.value = ''
}

// 发送礼物
const sendGift = (gift) => {
  if (!userId.value) {
    ElMessage({
      showClose: true,
      message: '未登录用户不能送礼',
      type: 'error',
    })
    return
  }

  const giftMsg = {
    type: 5,
    roomId: roomId,
    fromUserId: userId.value,
    fromUserName: userName.value,
    body: [{ content: `${userName.value}送给主播一个 ${gift.giftName}` }]
  }
  socket.send(JSON.stringify(giftMsg))
}

// 显示钱包信息
const showBankInfoTab = () => {
  showBankInfo.value = true
}

// 查看购物车
const toShowCarTab = () => {
  ElMessage({
    message: '购物车功能暂未实现',
    type: 'info'
  })
}
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
        // background-color: grey;
        border: 1px solid black;
        border-radius: 8px;
        margin-bottom: 20px;

        .anchorAvatar {
          width: 50px;
          height: 50px;
          border-radius: 50%;
          border: 2px solid #ff8f19;
          margin-right: 15px;
        }

        .anchorName {
          color: #ffad2c;
          font-size: 13px;
          margin-right: 10px;
        }

        .anchorLabel {
          width: 23px;
          height: 23px;
          margin-right: 10px;
        }

        .butCont {
          padding: 20px;
          span {
            display: inline-block;
            width: 85px;
            text-align: center;
            height: 35px;
            border-radius: 20px;
            margin-left: 10px;
          }
        }
      }

      .videoChatWrapper {
        display: flex;
        gap: 20px;
        margin-bottom: 20px;
        height: 550px;
        /* 增加视频区域高度 */
      }

      .videoContainer {
        flex: 3;
        /* 视频区域占比更大 */
        background-color: #120925;
        border-radius: 8px;
        overflow: hidden;
        height: 100%;

        video {
          width: 100%;
          height: 100%;
          object-fit: cover;
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

          .chatContent {
            margin-bottom: 10px;

            &.giftMsg {
              text-align: center;
              font-size: 13px;
              color: #868686;
            }

            &.systemMsg {
              color: #666;
              font-size: 13px;
            }

            .userName {
              color: #8694ff;
              font-size: 13px;
              margin-right: 5px;
            }
          }
        }

        .commentBox {
          background-color: #fff;
          border: 1px solid #e5e5e5;
          border-radius: 8px;
          padding: 15px;

          .el-form {
            display: flex;
            gap: 10px;

            .el-form-item {
              flex: 1;

              .el-input {
                width: 100%;
              }
            }

            .sendBtn {
              background-color: #1890ff;
              color: #fff;
              border: none;
              padding: 0 20px;
              border-radius: 4px;
              cursor: pointer;
            }
          }

          .loginPrompt {
            width: 100%;
            text-align: center;
            padding: 10px 0;
            color: #666;
          }
        }
      }

      .giftArea {
        width: 100%;
        // background-color: grey;
        border: #120925 3px solid;
        border-radius: 8px;
        padding: 15px;

        .giftContentTitle {
          color: #ffa735;
          font-size: 16px;
          text-align: center;
          margin-bottom: 15px;
        }

        .bankTab {
          color: #ffbc2e;
          text-align: right;
          font-size: 14px;
          margin-bottom: 15px;
          padding-right: 10px;

          span {
            margin-left: 10px;
            cursor: pointer;
          }
        }

        .giftList {
          display: flex;
          gap: 10px;
          overflow-x: auto;
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

              &:hover {
                border-color: #ffa925;
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