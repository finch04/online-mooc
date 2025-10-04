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
                <div v-if="userId">
                    <el-input  v-model="comment" placeholder="发送直播评论" max="40" ></el-input>
                </div>
                 <span  v-if="userId" class="bt sendBtn" style="margin-top: 10px;" @click="sendComment()">发送消息</span>
                <button class="loginPrompt" v-if="!userId">请先登录，才能开始聊天</button>
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

// 1. 将 socket 声明为 ref 响应式变量，方便跟踪状态
const socket = ref(null);

// 2. 修正初始化逻辑，移除外部重复调用
const initWebsocket = async () => {
  // 等待 getWebSocket 执行完成，并将结果赋值给 socket
  socket.value = await getWebSocket(userId.value?userId.value:''); // 注意：userId 是 ref，需用 .value 访问

};

onMounted(async () => { // onMounted 支持异步函数
  initPlayer();
  await initWebsocket(); // 等待 websocket 初始化完成
});

// 3. 发送评论时检查 socket 状态
const sendComment = () => {
  if (!comment.value) return;
  // 检查 socket 是否存在且处于打开状态
  if (!socket.value || socket.value.readyState !== WebSocket.OPEN) {
    ElMessage.error('连接未就绪，请稍后再试');
    return;
  }
  const commentMsg = {
    type: 2,
    roomId: roomId,
    fromUserId: userId.value,
    fromUserName: userName.value,
    body: [{ content: comment.value }]
  };
  
  console.log('发送评论：', commentMsg);
  socket.value.send(JSON.stringify(commentMsg)); // 使用 .value 访问 ref 变量
  comment.value = '';
};

// 4. 发送礼物时同样检查 socket 状态
const sendGift = (gift) => {
  if (!userId.value) {
    ElMessage.error('未登录用户不能送礼');
    return;
  }
  // 检查 socket 是否存在且处于打开状态
  if (!socket.value || socket.value.readyState !== WebSocket.OPEN) {
    ElMessage.error('连接未就绪，请稍后再试');
    return;
  }

  const giftMsg = {
    type: 5,
    roomId: roomId,
    fromUserId: userId.value,
    fromUserName: userName.value,
    body: [{ content: `${userName.value}送给主播一个 ${gift.giftName}` }]
  };
  socket.value.send(JSON.stringify(giftMsg)); // 使用 .value 访问 ref 变量
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
        height: 800px /* 可根据实际情况调整 */
      }

      .videoSide {
        flex: 3; /* 视频和礼物区域占比 */
        display: flex;
        flex-direction: column;
        gap: 20px;
      }

      .videoContainer {
        background-color: #120925;
        border-radius: 8px;
        overflow: hidden;
        flex: 1; /* 视频区域占视频侧的大部分高度 */

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

      .chatArea {
        flex: 1; /* 聊天区域占比，可根据需要调整 */
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
            flex-direction: column;
            gap: 10px;
            height: 100%;

            .el-form-item {
              flex: 1;
              margin-bottom: 10px;

              .el-input {
                width: 100%;
                height: 100%;
                textarea {
                  min-height: 80px !important;
                }
              }
            }

            .sendBtn {
              background-color: #1890ff;
              color: #fff;
              border: none;
              padding: 8px 20px;
              border-radius: 4px;
              cursor: pointer;
              align-self: flex-end;
            }
          }

          .loginPrompt {
            width: 100%;
            text-align: center;
            padding: 20px 0;
            color: #666;
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