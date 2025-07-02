<!-- 架构页面 -->
<template>
  <div class="layoutsWrapper fx-fd-col">
    <div id="interactive-area">
      <Header></Header>
      <router-view class="fx-1" />
      <Footer></Footer>
    </div>
    
    <div class="aiSupernatant">
      <!-- 大图 -->
      <div v-if="!isShow && !dialogVisible" class="superCon">
        <el-tooltip
          popper-class="box-item"
          effect="dark"
          content=""
          placement="top"
        >
          <template #content>
            <div v-if="token">哈！天机不可泄露<br />除非你来问我</div>
            <div v-else>哇！我是天机AI助理<br />你没有登录可不能和我聊哦～</div>
          </template>
          <div>
            <span @click="handleBigClose" class="close"
              ><i class="iconfont icon-a-icon-close2x"></i
            ></span>
            <div @click="handleBtn">
              <div class="bigIcon"></div>
            </div>
          </div>
        </el-tooltip>
      </div>
      <!-- end -->
      <!-- 小图 -->
      <div
        v-if="isShow && !dialogVisible"
        class="smallIcon"
        @click="handleClick"
      >
        <span class="icon"></span>
      </div>
      <!-- end -->
    </div>
    <!-- 对话弹层 -->
    <!-- <el-dialog
      v-model="dialogVisible"
      modal-class="superDialog"
      custom-class="custom-dialog"
      title=""
      draggable
      :close-on-click-modal="false"
      @close="handleClose"
      :show-close="false"
       :modal="false"
       :lock-scroll="false"
    > -->
    <div v-if="dialogVisible" 
    class="superDialog draggable-dialog" 
       @mousedown.stop="startDrag" 
       @touchstart="startDrag"
       :style="dialogStyle">
       <div @mousedown.stop @touchstart.stop>
      <!-- <template #header> -->
        <div class="dialogHeader">
          <div :id="titleId" class="title">
            <span class="font">天机AI助理</span>
            <button
              :class="
                chatDataArr.length === 0 || outputLoading ? 'disabled' : ''
              "
              @click="handleCread"
            >
              <i class="iconfont icon-a-icon-cjhh2x"></i>创建会话
            </button>
            <button @click="handleHistory">
              <i class="iconfont icon-a-icon-lsjl2x"></i>历史记录
            </button>
          </div>
          <div class="text">
            <button @click="handleRight">
              <i class="iconfont icon-a-icon-yc2x"></i>
            </button>
            <button @click="handleMagnify">
              <i class="iconfont icon-a-icon-qp2x"></i>
            </button>
            <button @click="handleClose">
              <i class="iconfont icon-a-icon-sq2x"></i>
            </button>
          </div>
        </div>
      <!-- </template> -->
      <div class="chitchatBox">
        <!-- 聊天对话 -->
        <div v-if="chatDataArr.length > 0"
          
          ref="scrollContainer"
          class="chitchatCon">
          <div style="flex: 1;overflow: auto;width: 100%;">
        <div
          ref="scrollTarget"
           class="chatContainer"
           @scroll="handleScroll"
        >
          <div class="item" v-for="(item, index) in chatDataArr" :key="index">
            <div class="questionContainer" v-if="item.type === 'USER'">
              <div class="avatarWrap userAvatarWrap">
                <div class="questionAnswerCon">
                  <div class="content">
                    {{ item.content
                    }}<span class="userIcon"
                      ><img :src="userInfo.icon" :onerror="onerrorImg" alt=""
                    /></span>
                  </div>
                </div>
              </div>
            </div>
            <div class="answerContainer" v-if="item.type === 'ASSISTANT'&&(compiledMarkdown !== '' || item.content !== '')">
              <div class="avatarWrap aiAvatarWrap">
                <div class="answerCon">
                  <div class="content">
                    <span
                      class="serverIcon"
                      v-if="compiledMarkdown !== '' || item.content !== ''"
                    ></span>
                    <div v-if="isSend && index === chatDataArr.length - 1">
                      <div
                        v-if="compiledMarkdown !== ''"
                        class="markdown-body"
                        :class="{
                          'show-cursor': isTypingDone,
                          'hide-cursor': !isTypingDone,
                        }"
                      >
                        <span
                          ref="textElement"
                          v-html="md.render(compiledMarkdown)"
                          class="text"
                        ></span>
                      </div>
                    </div>
                    <div v-else-if="item.content !== ''" style="display:flex">
                      <div class="markdown-body">
                        <div v-html="md.render(item.content)" class="htmlBody"></div>
                      </div>
                    </div>
                    <!-- -->
                    <div v-if="item.cardDetails&&item.cardDetails.length > 0" class="cardList">
                      <div
                        v-for="(value, index) in item.cardDetails"
                        :key="index"
                        class="item"
                      >
                        <router-link
                          :to="'/details/index?id=' + value.id"
                          target="_blank"
                        >
                          <div class="cardBox">
                            <div class="cardCon">
                              <div class="lImg">
                                <img :src="value.coverUrl" />
                              </div>
                              <div class="rText">
                                <div class="tit">{{ value.name }}</div>
                                <div class="text">
                                  <span class="ft-16 ft-cl-err"
                                    >￥{{
                                      (Number(value.price) / 100).toFixed(2)
                                    }}</span
                                  >
                                </div>
                              </div>
                            </div>
                            <div class="footText">
                              <span>共{{ value.cataTotalNum }}节</span>
                              <span>有{{ value.studyNum }}人在学习</span>
                            </div>
                          </div>
                        </router-link>
                      </div>
                    </div>
                    <div
                      class="conIconBox"
                      v-if="(compiledMarkdown !== ''&&isEnd) || item.content !== ''"
                    >
                      <span
                        class="praiseIcon"
                        :class="isPraise ? 'isPraise' : ''"
                        @click="handlePraise"
                        ><i class="iconfont icon-a-dianzan2x"></i
                      ></span>
                      <span class="treadIcon" @click="handleTread"
                        ><i class="iconfont icon-a-diancai2x"></i
                      ></span>
                      <span class="playIcon" @click="handlePlay"
                        ><i class="iconfont icon-a-yuyinbofang2x"></i
                      ></span>
                      <span class="copyIcon" @click="handleCopy"
                        ><i class="iconfont icon-a-fuzhi2x"></i
                      ></span>
                      <span
                        class="updateIcon"
                        @click="handleUp()"
                        v-if="
                          index === chatDataArr.length - 1 && !outputLoading
                        "
                        ><i class="iconfont icon-a-zhongxinshengcheng2x"></i
                      ></span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <!--  -->
          <div v-if="isLoading" class="answerContainer answerLoad">
            <span class="serverIcon"></span>给我点时间，我正在思考你的问题<span class="three-dots"></span>
          </div>


        </div>
      </div>
      </div>
        <!-- end -->
        <!-- 初次进入对话弹层 -->
        <div v-else-if="baseData" class="baseBox">
          <div class="chatLogo"><img src="@/assets/pic-hello.png" /></div>
          <div class="head">
            <div class="font tit">{{ baseData.title }}</div>
            <div class="subTit">{{ baseData.describe }}</div>
          </div>
          <div class="newQuestion">
            <div class="tit">
              <div class="lRight">试试这样问我：</div>
              <div class="barter">
                <span @click="handleUpdate"
                  ><i class="iconfont icon-a-icon-hyh2x"></i>换一换</span
                >
              </div>
            </div>
          </div>
          <div class="newList">
            <div
              class="item"
              v-for="(item, index) in baseData.examples"
              :key="index"
            >
              <span class="icon" :class="'iconTj' + index"></span>
              <div class="tit">{{ item.title }}</div>
              <div class="text">{{ item.describe }}</div>
            </div>
          </div>
        </div>

        <!-- end -->
        <div class="dialogForm">
          <div class="dialogCon">
            <!-- 聊天表单 -->
            <!--  :class="textVal.length > 9950 ? 'textPad' : ''" -->
            <div class="textarea">
              <!-- <el-input type='textarea' :autosize="{ minRows: 1, maxRows: 5 }" maxlength="10000" :show-word-limit="textVal.length>9500" :class="textVal.length>9500?'textPad':''" v-model="textVal" @keydown.enter.prevent="onEnter" @keydown="handleKeyDown" @input="handleInput" placeholder="请将您的问题告诉我，Shift+Enter换行" :style="{ minHeight: '30px', maxHeight: '300px' }" autocomplete="off"></el-input> -->
              <textarea
                ref="textareaRef"
                maxlength="10000"
                v-model="textVal"
                @keydown.enter.prevent="onEnter"
                @keydown="handleKeyDown"
                @input="handleInput"
                @focus="handleInput"
                @mouseenter="handleMouseEnter(2)" 
                @mouseleave="handleMouseLeave(2)"
                class="hide-scrollbar"
                placeholder="请将您的问题告诉我，Shift+Enter换行"
                :style="{
                  height: dynamicHeight,
                  overflowY: overflowY,
                  resize: 'none'
                }"
                autocomplete="off"
              ></textarea>
            </div>
            <!-- end -->
            <!-- 操作按钮 -->
            <div class="dialogFooter">
              <div class="leftInfo">
                <span class="uploadImgIcon" @click="handleUpdateImg"
                  ><i class="iconfont icon-tupian"></i
                ></span>
                <span class="uploadTextIcon" @click="handleUpdateText"
                  ><i class="iconfont icon-a-wenjianjia1"></i
                ></span>
                <span class="textNum" :class="textVal.length > 9950 ? 'textShow' : ''"><span :class="textVal.length === 10000 ? 'red' : ''">{{textNum}}</span>/10000</span>
              </div>
              <div class="rightInfo">
                <span class="voiceIcon" @click="handleUpdateYy"
                  ><i class="iconfont icon-yuyin"></i
                ></span>
                <span class="line"></span>
                <el-button
                  class="stopIcon"
                  @click="handleStop"
                  v-if="outputLoading&&compiledMarkdown !== ''"
                  ><span class="bg"></span><span class="btn"></span
                ></el-button>

                <el-button
                  class="sendIcon"
                  v-else
                  @click="handleSend"
                  @keyup.enter.prevent="handleEnter"
                  :disabled="!textVal"
                  ><span class="bg"></span><span class="btn"></span
                ></el-button>
              </div>
            </div>
            <!-- end -->
          </div>
        </div>
      </div>
      </div>
    <!-- </el-dialog> -->
    </div>
    <!-- end -->
  </div>
</template>
<script setup>
import Footer from '@/components/Footer.vue';
import Header from '@/components/Header.vue';
import {
  onMounted,
  onUnmounted,
  onUpdated,
  ref,
  reactive,
  watchEffect,
  computed,
  nextTick,
  watch,
} from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/store';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { getClassDetails } from '@/api/classDetails.js';

import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js/lib/core';
// 导入语言模块和支持的表情符号
import javascript from 'highlight.js/lib/languages/javascript';
import 'highlight.js/styles/monokai-sublime.css'; // 选择你喜欢的主题
import 'highlight.js/styles/github.css'; // GitHub风格
import 'highlight.js/styles/dark.css'; // Solarized Dark风格
// import emojiPlugin from 'markdown-it-emoji';
import tocPlugin from 'markdown-it-table-of-contents';
import anchorPlugin from 'markdown-it-anchor';
import defaultImage from '@/assets/icon.jpeg';
import 'highlight.js/styles/default.css'; // 你可以根据需要选择其他主题
// import markdownContent from '@/assets/your-markdown-file.md';
// 获取接口
import {
  newSession,
  getChat,
  getChatHistory,
  chatStop,
} from '@/api/intelligent.js';
import { Failed } from '@element-plus/icons-vue';
const store = useUserStore(); //新建会话时把获取的信息存起来
const router = useRouter();
const route = useRoute();
const isShow = ref(false); // 默认小图显示
const dialogVisible = ref(false); //对话弹层
const dialogState = reactive({
      x: 0,
      y: 0,
      dragging: false,
      initialX: 0,
      initialY: 0,
      windowWidth: 0,
      windowHeight: 0,
    });

    // 计算对话框的位置
    const dialogStyle = computed(() => ({
      position: 'absolute',
      left: `${dialogState.x}px`,
      top: `${dialogState.y}px`,
      width: '460px',
      height: '700px',
      backgroundColor: '#fff',
      border: '1px solid #ccc',
      boxShadow: '0 4px 6px 2px #6c70762b',
      borderradius: '10px',
      cursor: 'move',
      bottom: `${dialogState.windowHeight - dialogState.y}px`,
      right: `${dialogState.windowWidth - dialogState.x}px`
    }));
// 设置对话框初始位置为右下角
const setInitialPosition = () => {
      dialogState.windowWidth = window.innerWidth;
      dialogState.windowHeight = window.innerHeight;
      dialogState.x = dialogState.windowWidth - 460; // 假设宽度是300px
      dialogState.y = dialogState.windowHeight - 700; // 假设高度是200px
    };
const outputLoading = ref(false); //ai内容生成中
const isTypewriting = ref(true); //打字是否结束
const textVal = ref(''); //表单内容
const chatDataArr = ref([]); //聊天内容
const scrollTarget = ref(null); //对话框的ref
const scrollContainer = ref(null);
const baseData = ref({});
// const recognitionResult = ref("");
const isLoading = ref(false);
const currentIndex = ref(0);
const textData = ref('');
const typingSpeed = ref(80); //打字速度，单位毫秒
const typingDone = ref(false);
let typingInterval = ref(null); //定时器
const isSend = ref(false);
const isEnd = ref(false) //内容是否展示完毕，用来显示点赞，更新按钮
// 初始化 markdown-it 实例并添加插件
const textElement = ref(null);
const isPraise = ref(false); //是否点过赞
const detailsId = ref(null); //详情页id
const hrefLink = ref(null);
const userInfo = ref(store.getUserInfo); //用户数据
const token = store.token;
const isClick = ref(false) //内容正在生成中，回车是否可以发送内容
const textareaRef =ref(false) //textarea设置ref
const textNum = ref()//文本框输入的字数
const maxNum = ref(10000)
const dynamicHeight = ref('24px'); // 动态高度
const overflowY = ref('hidden'); // 滚动条控制
const maxLines = 5; // 最大显示行数

// 注册语言
hljs.registerLanguage('javascript', javascript);

// 初始化 markdown-it 实例并配置插件
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    console.log(str,lang,456)
    if (lang && hljs.getLanguage(lang)) {
      try {
        // 去除多余空格
        const trimmedStr = str.split('\n')
        .map(line => line.trimStart())
        .join('\n')
        .trim();
        return `<pre class="hljs"><code>${
          hljs.highlight(trimmedStr, { language: lang }).value
        }</code></pre>`;
      } catch (__) {}
    }

    return '';
  },
})
  .use(tocPlugin)
  .use(anchorPlugin);
// 自定义渲染器：重写 h1, h2, h3 标签的渲染规则，并去除空格
['h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'h7','div','p'].forEach((tag) => {
  md.renderer.rules[`${tag}_open`] = function (
    tokens,
    idx,
    options,
    env,
    self
  ) {
    const token = tokens[idx];

    // 添加额外的属性、类名等
    return `<${token.tag} class="custom-${tag}">`;
  };

  md.renderer.rules[`${tag}_close`] = function (
    tokens,
    idx,
    options,
    env,
    self
  ) {
    const token = tokens[idx];
    return `</${token.tag}>`;
  };

  // 修改文本内容去除首尾空格
  md.renderer.rules[`${tag}`] = function (tokens, idx, options, env, self) {
    const token = tokens[idx];
    if (token.type === `${tag}`) {
      // 去除首尾空格
      token.content = token.content.trim();
    }
    return self.renderToken(tokens, idx, options);
  };
});
// 自定义渲染规则，针对链接标签
md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
  // 添加 target="_blank" 和 rel="noopener" 到所有的 <a> 标签
  tokens[idx].attrPush(['target', '_blank']);
  tokens[idx].attrPush(['rel', 'noopener']);
  // 使用默认的方式渲染标签
  return self.renderToken(tokens, idx, options);
};
// 覆盖默认的元素渲染规则、元素默认会有一行的空白间距
// 覆盖段落渲染规则
md.renderer.rules.paragraph_open = () => '<p>';
md.renderer.rules.paragraph_close = () => '</p>';

// 覆盖列表渲染规则
console.log(md.renderer.rules)
md.renderer.rules.bullet_list_open = () => '<ul>';
md.renderer.rules.bullet_list_close = () => '</ul>';
md.renderer.rules.list_open = () => '<ol>';
md.renderer.rules.list_close = () => '</ol>';
md.renderer.rules.list_item_open = () => '<li>';
md.renderer.rules.list_item_close = () => '</li>';

// 覆盖代码块渲染规则
md.renderer.rules.code_block = (tokens, idx) => {
  const content = tokens[idx].content.trim();
  return `<pre><code>${content}</code></pre>`;
};
// 添加自定义规则
md.core.ruler.push('custom-class-rule', function (state) {
  const tokens = state.tokens;
  tokens.forEach(token => {
    //  && token.level === 1
    if (token.type === 'heading_open') {
      // 为 h1 标签添加 custom-class 类
      token.attrSet('class', ['custom-class-tit']);
    }else if(token.type === 'bullet_list_open'){
      token.attrSet('class', ['custom-class-ullist']);
    }else if(token.type === 'ordered_list_open'){
      token.attrSet('class', ['custom-class-ollist']);
    }
  });
});

const ids = ref([]);
const compiledMarkdown = ref('');
const markdownData = ref(''); //md代码
const isTypingDone = computed(
  () => !typingDone.value && compiledMarkdown.value.length > 0
);
// 获取 textarea 的行高（包含 padding）
const getLineHeight = () => {
  const style = window.getComputedStyle(textareaRef.value);
  const lineHeight = parseInt(style.lineHeight, 10);
  const paddingTop = parseInt(style.paddingTop, 10);
  const paddingBottom = parseInt(style.paddingBottom, 10);
  return lineHeight + paddingTop + paddingBottom;
};
// const compiledMarkdown = computed(
//   () => {
//     // 渲染 Markdown 内容
//     const rendered = md.render(markdownContent.value.trim());

// // 去除多余的换行符
// return rendered.replace(/\n\s*\n/g, '\n');
//   }
// );
// markdownContent.value
onMounted(() => {
  window.addEventListener('keydown', handleEnter);
  setInitialPosition();
  window.addEventListener('resize', setInitialPosition);
  // clearInterval(typingInterval.value);
  // 本地么有存储sessionId需要重新获取
  baseData.value = store.sessionInfo ? store.sessionInfo : {};
  if (baseData.value === null || baseData.value.sessionId === undefined) {
    getSession();
  } else {
    if (store.chatDataList) {
      chatDataArr.value = store.chatDataList;
      nextTick(() => {
        scrollToBottom();
        // targetLind()
      });
      console.log(chatDataArr.value);
    }
  }
  
});
onUpdated(()=>{
  scrollToBottom()
})
// // 给默认聊天数组的超连接加上_blank，以新窗口打开
// const targetLind = ()=>{
//   if(scrollTarget.value){
//           const links = scrollTarget.value.querySelectorAll('a');
//           links.forEach(function(link) {
//             link.setAttribute("target", "_blank");

//           });
//         }
// }

// 新建窗口获取id
const getSession = async () => {
  const res = await newSession();
  baseData.value = res.data;
  store.setSessionInfo(res.data);
  // getList()
};
const getList = async () => {
  const params = {
    sessionId: baseData.value.sessionId,
  };
  const res = await getChatHistory(params);
  if (res.code === 200) {
    chatDataArr.value = res.data;
  }
};
const planData = ref(); //课程详情
// 获取课程详情
const getCourseLearningData = async (ids) => {
  ids
    .forEach(async (val) => {
      await getClassDetails(val).then((res) => {
        const { data } = res;
        if (res.code === 200 && data) {
          planData.value = data;
          console.log(data)
          const arrs = chatDataArr.value[chatDataArr.value.length-1]
          // chatDataArr.value.forEach((ele) => {
          //   if (ele.type === 'ASSISTANT') {
          //     ele.cardDetails.push(data);
          //   }
          // });
          if(arrs.type === 'ASSISTANT'){
            arrs.cardDetails.push(data)
          }
          chatDataArr.value[chatDataArr.value.length-1] = arrs
          store.setChatDataList(chatDataArr.value);
          nextTick(()=>{
            scrollToBottom()
          })
        }
        
      }).catch(() => {
      ElMessage({
        message: '用户学习信息数据请求出错！',
        type: 'error',
      });
    });;
    })

    
};
// 创建新对话
const handleCread = () => {
  if (outputLoading.value) return;
  // 清空本地对话的数据
  markdownContent.value = '';
  compiledMarkdown.value = '';
  textData.value = '';
  isLoading.value = false;
  // outputLoading.value =false
  typingInterval.value = null;
  // console.log(ctrl);
  if (ctrl) {
    ctrl.abort(); // 结束会话
    handleStop();
  }

  if (chatDataArr.value.length > 0) {
    baseData.value = {};
    chatDataArr.value = [];
    store.setSessionInfo({});
    store.setChatDataList([]);
    getSession();
  }
};
// 点击小图显示大图
const handleClick = () => {
  isShow.value = false;
};

// 点击关闭大鸡
const handleBigClose = (event) => {
  event.preventDefault();
  dialogVisible.value = false;
  isShow.value = true;
};
// 打开对话弹层
const handleBtn = (event) => {
  event.preventDefault();
  if (token) {
    dialogVisible.value = true;
    nextTick(() => {
      handleInput();
    });

    isShow.value = false;
  } else {
    router.push('/login');
  }
};
// 关闭弹层
const handleClose = () => {
  isShow.value = false;
  dialogVisible.value = false;
};
const eventSource = ref();
const ctrl = new AbortController();
// 发送信息
const handleSend = async () => {
  planDataIds.value = []
  compiledMarkdown.value = '';
  markdownContent.value = '';
  textVal.value = textVal.value.replace(/\s+/g, '');
  if (textVal.value === '') return;
  const userObj = {
    type: 'USER',
    content: textVal.value,
  };

  chatDataArr.value.push(userObj);
  let assistantObj = {
    type: 'ASSISTANT',
    content: '',
  };
  chatDataArr.value.push(assistantObj);
  isLoading.value = true;
  outputLoading.value = true;
  // 先显示发送的消息
  nextTick(()=>{
    scrollToBottom()
  })
  
  if (baseData.value === undefined) return;
  nextTick(()=>{
    eventSource.value = fetchEventSource(
    `http://api.tianji.com/ais/chat?sessionId=${baseData.value.sessionId}&q=${textVal.value}`,
    {
      method: 'POST',
      headers: {
        Authorization: store.getToken,
      },
      signal: ctrl.signal,
      openWhenHidden: true, // 切换标签页时连接不关闭
      async onopen(response) {
        // 处理登录失效
        if (response.status === 401) {
          message.warning('登录过期');
          return;
        }
      },

      onmessage(msg) {
        isLoading.value = false;
        isSend.value = true;
        if (msg.data === '&complete&') {
          ctrl.abort(); // 结束会话
          isSend.value=false
            // // ctrl.close(); // 结束会话
        // // 获取的后台内容
        findATagsWithAttributes(markdownData.value);
        if (planDataIds.value.length > 0) {
          getCourseLearningData(planDataIds.value);
        }
        assistantObj = {
          type: 'ASSISTANT',
          content: textData.value,
          cardDetails:[]
          // courseId:detailsId.value?detailsId.value:null
        };
        // console.log(assistantObj);
        chatDataArr.value[chatDataArr.value.length - 1] = assistantObj;
        store.setChatDataList(chatDataArr.value);
        // handleInput();
        outputLoading.value = false;
        textData.value = '';
        isClick.value = false
        isEnd.value = true //内容已经展示完毕
        // 解决消息底部显示不全的问题
        nextTick(()=>{
          scrollToBottom()
        })
        } else {
          textData.value += msg.data;
          const datas = msg.data;
          appendAndType(datas);
          markdownData.value = md.render(textData.value);
          isClick.value = true
        }
        
      },
      onerror(err) {
        console.log(err,123987)
        textData.value = '';
        markdownContent.value = '';
        // compiledMarkdown.value = '';
        textData.value = '';
        isLoading.value = false;
        outputLoading.value = false;
        ElMessage({
          message: '内容输出异常，请重新生成！',
          type: 'error',
        })
        throw err

      },
      onclose(date) {
        console.log('guanle123');
        isClick.value = false
        ctrl.abort(); // 结束会话
        isEnd.value = false
        outputLoading.value = false;
        // compiledMarkdown.value=''
        detailsId.value = null;
        console.log('guanle');
        
      }
    }
  );
  //  发送后清空表单
  textVal.value = '';
  dynamicHeight.value = `24px`;
  })
  
};
const planDataIds = ref([]);
// 给回答后的数据a标签加上btarget=_blank，用新窗口打开
const findATagsWithAttributes = (htmlString) => {
  // 使用正则表达式匹配 <a> 标签，确保不重复添加 target 属性
  // console.log(htmlString);
  htmlString.replace(/<a([^>]*)>/gi, (match, $1) => {
  //   console.log(match, $1)
    planDataIds.value.push(getId(match.toString()));
    // planDataIds.value = arr.filter((item, index) => arr.indexOf(item) === index);

  });
};
const getTextBetweenChars = (str, startChar, endChar) => {
  const startIndex = str.indexOf(startChar);
  if (startIndex === -1) return ''; // 如果起始字符不存在，返回空字符串

  const endIndex = str.indexOf(endChar, startIndex + 1);
  if (endIndex === -1) return ''; // 如果结束字符不存在，返回空字符串

  return str.substring(startIndex + 1, endIndex); // 返回起始字符和结束字符之间的部分
};
// 获取id
const getId = (link) => {
  getTextBetweenChars(link, '?', '"');
  // 解析查询参数
  const urlParams = new URLSearchParams(getTextBetweenChars(link, '?', '"'));
  const id = urlParams.get('id');
  const courseId = urlParams.get('courseId');
  return id ? id : courseId;
};
const markdownContent = ref('');
const appendAndType = (fragment) => {
  // console.log(fragment,11555)
  // if(!fragment){
  //   console.log(123)
  //   ctrl.abort(); // 结束会话
  //   clearInterval(typingInterval.value);
  //   return false
  // }

  // 如果当前有正在运行的打字机效果，则等待其完成
  // console.log(typingInterval.value, 789);
  clearInterval(typingInterval.value);

  // 将新的 Markdown 片段追加到现有的内容后面
  // console.log(fragment,1)
  // console.log(markdownContent.value,2)
  
  const existingContent = markdownContent.value;
  markdownContent.value = existingContent + fragment;
// 应用打字机效果到新追加的部分
// typeWriterEffect(fragment, 100);
  

  // 立即更新已有的内容，以便开始新的打字机效果
  compiledMarkdown.value = md.render(markdownContent.value);
  scrollToBottom();
};
// vue3+markdown +sse实现markdown 格式的内容，实现打字机效果
const typeWriterEffect = (newText, delay) => {
  console.log(newText,1)
  let index = 0;
  typingInterval.value = setInterval(() => {
    if (index <= newText.length) {
      markdownContent.value += newText.charAt(index);
      compiledMarkdown.value = md.render(markdownContent.value);
      index++
    } else {
      clearInterval(typingInterval.value);
      
      isTypewriting.value = true;
      outputLoading.value = false;
    }
  }, delay);
  scrollToBottom();
  // typingInterval.value = setInterval(() => {

  //   if (index.value < text.length) {
  //     console.log(index,text)
  //     compiledMarkdown.value += text.charAt(index);
  //     index++;
  //   }
  //   // else {
  //   //   console.log(index,text)
  //   //   index.value = 0
  //   //   isTypewriting.value = true
  //   //   outputLoading.value = false
  //   //   clearInterval(typingInterval.value);
  //   //   // ctrl.abort(); // 结束会话
  //   // }
  //   scrollToBottom()
  // }, delay)
};
const scrollToBottom = () => {
  const scrollTar = scrollTarget.value;
  // console.log(scrollTar);
  if (scrollTar) {
    // console.log(123);
    scrollTar.scrollTop = scrollTar.scrollHeight;
  }
};

// 停止生成
const handleStop = () => {
  // if (typingDone.value) {
    ctrl.abort(); // 结束会话
  outputLoading.value = false;
  // isSend.value = false
  hadlechatStop();
  clearInterval(typingInterval.value);
  chatDataArr.value[chatDataArr.value.length - 1].content =
  compiledMarkdown.value;
  store.setChatDataList(chatDataArr.value);
  typingDone.value = false;
  
  // }
};
const hadlechatStop = async () => {
  const params = {
    sessionId: baseData.value.sessionId,
  };
  const res = await chatStop(params);
  // if (res.code === 200) {
  //   ElMessage({
  //     message: "此功能暂未开发！",
  //     type: 'error'
  //   });
  // }
};
// Shift+Enter换行
const onEnter = (event) => {
  // 阻止回车键默认行为
  event.preventDefault();

  // 检查shift是否被按下
  if (event.shiftKey) {
    // 换行逻辑
    textVal.value += '\n';
  }
};
// 输入文本时禁止触发回车
const handleKeyDown = (event) => {
  if (event.keyCode === 13 && !event.shiftKey) {
    // 当按下回车键但没有按下Shift键时，不允许换行
    event.preventDefault();
  }
};
// 触发发送信息按钮
const handleEnter = (e) => {
  // 如果正在生成禁止回车发送
  if(isClick.value) return
  if (e.target.value !== undefined) {
    textVal.value = e.target.value.replace(/\s+/g, '');
    if (textVal.value !== '' && !e.shiftKey) {
      if (e.keyCode === 13 || e.keyCode === 108) {
        clearInterval(typingInterval.value);
        scrollToBottom();
        handleSend();
      }
    }
  }
};
//触发输入文本框数据滚动到最后一条
const handleInput = (event) => {
  const scrollTar = scrollTarget.value;
  // if (scrollTar) {
    // const scrollBehavior = scrollTar.scrollHeight - scrollTar.clientHeight;
    // scrollTar.scrollTo({
    //   top: scrollBehavior,
    //   behavior: 'smooth', // 平滑滚动
    // });
    scrollToBottom();
    const textarea = textareaRef.value;
      textarea.style.height = `auto`;
      // 重置高度为 auto 以计算真实高度
      
      const currentScrollHeight = textarea.scrollHeight;
      const lineHeight = getLineHeight();
      const maxHeight = lineHeight * maxLines;
      
      if (currentScrollHeight > maxHeight) {
        // 超过 5 行：固定高度并显示滚动条
        dynamicHeight.value = `${maxHeight}px`;
        textarea.style.height = `${maxHeight}px`;
        overflowY.value = 'auto';
      } else {
        // 不足 5 行：动态调整高度
        dynamicHeight.value = `${currentScrollHeight - 20}px`;
        textarea.style.height = `${currentScrollHeight - 20}px`;
        overflowY.value = 'hidden';
      }
      // 限制输入最多的字数
      if (textVal.value.length > maxNum.value) {
        textVal.value = textVal.value.slice(0, maxNum.value);
      }
      textNum.value = textVal.value.length;
  // }
};
// 默认头像
const onerrorImg = () => {
  userInfo.value.icon = defaultImage;
};
// 换一换
const handleUpdate = () => {
  ElMessage({
    message: '此功能暂未开发！',
    type: 'warning',
  });
};
// 上传图片
const handleUpdateImg = () => {
  handleUpdate();
};
// 上传图片
const handleUpdateText = () => {
  handleUpdate();
};
// 上传图片
const handleUpdateYy = () => {
  handleUpdate();
};
// 历史记录
const handleHistory = () => {
  handleUpdate();
};
// 右侧窗口吸附
const handleRight = () => {
  handleUpdate();
};
// 全屏
const handleMagnify = () => {
  handleUpdate();
};
// handlePraise\handleTread\handlePlay\handleCopy\handleUp
// 点赞
const handlePraise = () => {
  isPraise.value = true;
  handleUpdate();
};
// 点踩
const handleTread = () => {
  handleUpdate();
};
// 语音播放
const handlePlay = () => {
  handleUpdate();
};
// 复制
const handleCopy = () => {
  handleUpdate();
};
// 更新
const handleUp = () => {
  // handleUpdate();
  console.log(isClick.value)
  if(isClick.value) return
  handleSend()
};
// 鼠标进入时的操作
const handleMouseEnter = (type) => {
  let val=null
  if(type===1){
    val = scrollTarget.value
  }else{
    val = textareaRef.value
  }
  val.classList.add('show-scrollbar');
  val.classList.remove('hide-scrollbar');
};

// 鼠标离开时的操作
const handleMouseLeave = (type) => {
  let val=null
  if(type===1){
    val = scrollTarget.value
  }else{
    val = textareaRef.value
  }
  val.classList.add('hide-scrollbar');
  val.classList.remove('show-scrollbar');
};
const hancleStop=(event)=>{
  event.preventDefault()
}
// 元素的位置样式
const startDrag = (event) => {
  event.preventDefault()
      if (event.type === 'mousedown') {
        dialogState.initialX = event.clientX - dialogState.x;
        dialogState.initialY = event.clientY - dialogState.y;
      } else if (event.type === 'touchstart') {
        dialogState.initialX = event.touches[0].clientX - dialogState.x;
        dialogState.initialY = event.touches[0].clientY - dialogState.y;
      }
      dialogState.dragging = true;

      document.addEventListener('mousemove', drag);
      document.addEventListener('mouseup', stopDrag);

      document.addEventListener('touchmove', drag);
      document.addEventListener('touchend', stopDrag);
    };

    const drag = (event) => {
      if (!dialogState.dragging) return;

      let currentX, currentY;
      if (event.type === 'mousemove') {
        currentX = event.clientX - dialogState.initialX;
        currentY = event.clientY - dialogState.initialY;
      } else if (event.type === 'touchmove') {
        currentX = event.touches[0].clientX - dialogState.initialX;
        currentY = event.touches[0].clientY - dialogState.initialY;
      }

      // 确保对话框不会被拖出窗口边界
      dialogState.x = Math.max(0, Math.min(currentX, dialogState.windowWidth - 300));
      dialogState.y = Math.max(0, Math.min(currentY, dialogState.windowHeight - 200));
    };

    const stopDrag = () => {
      dialogState.dragging = false;
      document.removeEventListener('mousemove', drag);
      document.removeEventListener('mouseup', stopDrag);

      document.removeEventListener('touchmove', drag);
      document.removeEventListener('touchend', stopDrag);
    };
onUnmounted(() => {
  ctrl.abort(); // 结束会话
  // ctrl.close(); // 结束会话
});
</script>
<style lang="scss" src="./index.scss" scoped></style>
<style scoped>
/* 引用 highlight.js 的样式 */
@import 'highlight.js/styles/monokai-sublime.css';

/* 自定义样式 */
.markdown-body {
  white-space: pre; /* 保留空白符 */
  overflow: hidden; /* 隐藏溢出内容 */
  display: inline-block;
  white-space: pre-wrap; /* 保留换行符 */
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4,
.markdown-body h5,
.markdown-body h6 {
  margin-top: 1em;
  margin-bottom: 0.5em;
  font-weight: bold;
}

.markdown-body a {
  color: #0366d6;
  text-decoration: none;
}

.markdown-body a:hover {
  text-decoration: underline;
}

.markdown-body code {
  padding: 2px 4px;
  font-size: 90%;
  color: #c7254e;
  background-color: #f9f2f4;
  border-radius: 4px;
}

.markdown-body pre {
  padding: 16px;
  overflow: auto;
  font-size: 85%;
  line-height: 1.45;
  background-color: #f7f7f7;
  border-radius: 3px;
}

.markdown-body table {
  display: block;
  width: 100%;
  overflow: auto;
}

.markdown-body th {
  font-weight: bold;
}

.markdown-body td,
.markdown-body th {
  padding: 6px 13px;
  border: 1px solid #ddd;
}

.markdown-body img {
  max-width: 100%;
  box-sizing: content-box;
}
</style>
<style lang="scss">
.el-popper {
  &.box-item {
    padding: 9px 20px;
    text-align: center;
    line-height: 18px;
    &.is-dark {
      background-image: linear-gradient(
        268deg,
        #43b7fc 5%,
        #9a20f7 100%
      ) !important;
      box-shadow: 0 2px 7px 0 #002db636 !important;
      border-radius: 11px 11px 11px 0 0 0 11px !important;
      border: 0 none;
      & > .el-popper__arrow:before {
        background: #6c70f9 !important;
        border: 0 none;
        box-shadow: none;
      }
    }
  }
}

.chatContainer h1,
.chatContainer h2,
.chatContainer h3,
.chatContainer h4,
.chatContainer h5,
.chatContainer h6 {
  color: #2c3e50;
}

.chatContainer code {
  background-color: #f5f5f5;
  padding: 2px 4px;
  border-radius: 4px;
}

.chatContainer pre {
  background-color: #272822;
  color: #f8f8f2;
  padding: 15px;
  overflow-x: auto;
  border-radius: 8px;
}
// .custom-dialog .el-dialog__wrapper {
//   pointer-events: none;
// }
// .custom-dialog .el-dialog {
//   pointer-events: auto;
// }
</style>
