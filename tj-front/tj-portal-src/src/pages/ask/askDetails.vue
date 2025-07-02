<!-- 提问、回复详情 -->
<template>
  <div class="askDetailsWrapper">
    <div class="container" v-if="askInfo">
        <div class="fx-sb ">
          <div class="fx-1 marg-rt-20">
            <!-- 问题主体- start -->
            <div class="askCont bg-wt marg-bt-20">
              <div class="userInfo">
                <img v-if="askInfo.userIcon" :src="askInfo.userIcon" alt="">
                <img v-else src="/src/assets/anonymity.png" alt="">
                {{askInfo.userName || '匿名'}}
              </div>
              <div class="askInfo">
                <div class="ft-20 ft-wt-600">{{askInfo.title}}</div>
                <div class="ft-cl-des marg-bt-10">{{askInfo.createTime}}</div>
                <div>{{askInfo.description}}</div>
              </div>
            </div> 
            <!-- 问题主体- end -->
            <!-- 回答题主- start -->
            <div class="answerCont bg-wt marg-bt-20">
              <div class="ft-20 ft-wt-600 marg-bt-20">我要回答</div>
              <div class="answer fx">
                <img :src="store.getUserInfo.icon" alt="" srcset="">
                <div class="fx-1">
                  <div class="answerCon">
                    <el-input v-model="description" rows="11" type="textarea" @input="ruleshandle" maxlength="500" show-word-limit placeholder="请发表高见" resize="none" />
                    <div class="aiCon">
                      <div class="aiTip"><span class="serverIcon"></span>创作<span class="line">|</span></div>
                      <div class="aiTag" :class="description?'tagHover':''">
                        <span @click="handleAi(1)"><i class="iconfont icon-a-AIbangxie2x1"></i>AI帮写</span>
                        <span @click="handleAi(2)"><i class="iconfont icon-a-AIxuxie2x1"></i>AI续写</span>
                        <span @click="handleAi(3)"><i class="iconfont icon-a-AIrunse2x1"></i>AI润色</span>
                        <span @click="handleAi(4)"><i class="iconfont icon-a-Aijingjian2x1"></i>AI精简</span>
                      </div>
                    </div>
                  </div>
                  <div class="fx-sb fx-al-ct">
                    <div><el-checkbox v-model="anonymity" label="匿名提问" size="large" /></div>
                    <div class="subCont">
                      <span class="bt ft-14" :class="{'bt-dis':!isSend}" @click="answerHandle('first')">回答</span>
                      </div>
                  </div> 
                </div>
              </div>
            </div>
            <!-- 回答题主- end -->
            <!-- 全部回答- start -->
            <div class="answerCont bg-wt marg-bt-20">
              <div class="ft-20 ft-wt-600 marg-bt-20">全部回答({{count}})</div>
              <div class="answerItems">
                <div class="items" v-for="item in questionData" :key="item.id">
                  <div class="fx-al-ct">
                    
                    <span v-if="item.userId==='9999'" class="serverIcon"></span>
                    <span v-else>
                      <img class="img" v-if="item.userIcon" :src="item.userIcon" alt="">
                      <img class="img" v-else src="/src/assets/anonymity.png" alt="">
                    </span>
                    
                    <span class="ft-cl-des">{{item.userName || '匿名'}}</span><span v-if="item.userId==='9999'" class="bt">AI自动回复</span>
                  </div>
                  <div class="cont">
                    <div class="marg-bt-10" v-html="md.render(item.content)"></div>
                    <div class="fx-sb">
                      <div class="ft-cl-des">{{item.createTime}}</div>
                      <div>
                        <span class="marg-rt-10 cur-pt" @click="openReply(item)"> <i class="iconfont zhy-a-btn_pinglun_nor2x"></i> 评论({{item.replyTimes}}) </span>
                        <span :class="{'cur-pt':true, activeLiked: item.liked}" @click="likedHandle(item)"> <i class="iconfont zhy-a-btn_zan_nor2x"></i> 点赞( {{item.likedTimes}})</span>
                      </div>
                    </div>
                  </div>
                  <!-- 插入回复框的位置 -->
                  <component :is="openReplyFormId == item.id ? ReplayForm : null" :key="item.id" :name="item.userName || '匿名'" :askInfoId="askInfo.id" @commentHandle="commentHandle"></component>
                  <!-- 回复列表 -->
                  <div class="replyCont" v-show="replyData && isReplay == item.id">
                    <div class="items" v-for="it in replyData" :key="it.id">
                      <div class="fx-al-ct">
                        <span v-if="item.userId==='9999'" class="serverIcon"></span>
                        <span v-else>
                          <img class="img" v-if="it.userIcon" :src="it.userIcon" alt="">
                          <img class="img" v-else src="/src/assets/anonymity.png" alt="">
                        </span>
                        <span class="ft-cl-des"> {{it.userName || '匿名'}} 回复 {{it.targetUserName || "匿名用户"}} </span>
                      </div>
                      <div class="cont">
                        <!--  v-html="md.render(it.content)" -->
                        <div class="marg-bt-10">{{it.content}}</div>
                        <div class="fx-sb">
                          <div class="ft-cl-des">{{it.createTime}}</div>
                          <div>
                            <span class="marg-rt-10 cur-pt" @click="replayHandle(it, 'targe')" > <i class="iconfont zhy-a-btn_pinglun_nor2x"></i> 回复</span>
                            <span :class="{'cur-pt':true, activeLiked: it.liked}" @click="likedHandle(it)"> <i class="iconfont zhy-a-btn_zan_nor2x"></i> 点赞 ({{it.likedTimes}})</span>
                          </div>
                        </div>
                      </div>
                      <!-- 插入回复框的位置 -->
                      <component :is="openReplyFormId == it.id ? ReplayForm : null" :name="it.userName || '匿名'" :id = "it.userId" :askInfoId="askInfo.id"  @commentHandle="commentHandle"></component>
                    <!-- 回复列表 -->
                    </div>
                    <div @click="() => {dialogTableVisible = true}" class="fx-ct ft-14 ft-cl-des cur-pt" v-if="replyCont > 5">
                      点击查看全部{{replyCont}}条回复
                    </div>
                  </div>
                </div>
                <div></div>
                <p @click="clickLoad" v-if="!noMore" class="fx-ct ft-14 ft-cl-des">点击查看更多</p>
                <p class="fx-ct ft-14 ft-cl-des" v-if="noMore">没有更多了</p>
              </div>
            </div>
          </div>
          <!-- 相关问题 写死 -->
          <RelatedQuestions :id="route.query.detailsId" :title="route.query.name"></RelatedQuestions>
      </div>
    </div>
    <el-dialog v-model="dialogTableVisible" :title="`全部回复(${replyCont})`" width="80%" top="5vh" >
      <div class="dialogReplyCont" v-infinite-scroll="load" style="overflow: auto" :infinite-scroll-disabled="disabled">
        <div class="items" v-for="it in replyData" :key="`ss${it.id}`">
          <div class="fx-al-ct">
            <span v-if="item.userId==='9999'" class="serverIcon"></span>
            <span v-else>
              <img class="img" v-if="it.userIcon" :src="item.userIcon" alt="">
              <img class="img" v-else src="/src/assets/anonymity.png" alt="">
            </span>
            
            <span class="ft-cl-des"> {{it.userName || '匿名'}} 回复 {{it.targetUserName || "匿名用户"}} </span>
          </div>
          <div class="cont">
          <!--  v-html="md.render(it.content)" -->
            <div class="marg-bt-10">{{it.content}}</div>
            <div class="fx-sb">
              <div class="ft-cl-des">{{it.createTime}}</div>
              <div>
                <span class="marg-rt-10 cur-pt" @click="replayHandle(it, 'target')" > <i class="iconfont zhy-a-btn_pinglun_nor2x"></i> 评论{{it.replyTimes}} </span> 
                <span :class="{'cur-pt':true, activeLiked: it.liked}" @click="likedHandle(item)"> <i class="iconfont zhy-a-btn_zan_nor2x"></i> 点赞 {{it.likedTimes}}</span>
              </div>
            </div>
          </div>
          <!-- 插入回复框的位置 -->
          <component :is="openReplyFormId == it.id ? ReplayForm : null" :key="it.id" :name="it.userName || '匿名'" :id = "it.userId" :askInfoId="askInfo.id" @commentHandle="commentHandle"></component>
        <!-- 回复列表 -->
        </div>
         <p class="fx-ct ft-14 ft-cl-des" v-if="replayloading">Loading...</p>
         <p class="fx-ct ft-14 ft-cl-des" v-if="replaynoMore">没有更多了</p>
      </div>
  </el-dialog>
  <!-- ailoading -->
  <el-dialog v-model="isAiLoading" title="" class="aiPopUp" width="248px" top="15vh" :show-close="false">
    <div class="aiLoad">
      <div class="iconLoad"><img src="@/assets/icon/loadIcon.gif" /></div>
      <div class="text">内容生成中，请稍后…</div>
      <div class="loadBtn"><span class="bt ft-14" @click="handleCloseAi">取消</span></div>
    </div>
  </el-dialog>
  
  <!-- end -->
  </div>
</template>
<script setup>
/** 数据导入 **/
import { onMounted, reactive, ref, computed } from "vue";
import { ElMessage } from "element-plus";
import { useRoute } from "vue-router";
import { getQuestionsDetails, postAnswers, getReply, putLiked } from "@/api/classDetails.js";
import {textSession,getChatTemplates,getChatText} from '@/api/intelligent.js';
import RelatedQuestions from './components/RelatedQuestions.vue'
import ReplayForm from './components/ReplayForm.vue'
import { useUserStore } from '@/store'
import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js/lib/core';
// 导入语言模块和支持的表情符号
import javascript from 'highlight.js/lib/languages/javascript';
import 'highlight.js/styles/monokai-sublime.css'; // 选择你喜欢的主题
import 'highlight.js/styles/github.css'; // GitHub风格
import 'highlight.js/styles/dark.css'; // Solarized Dark风格
import tocPlugin from 'markdown-it-table-of-contents';
import anchorPlugin from 'markdown-it-anchor';
import 'highlight.js/styles/default.css'; // 你可以根据需要选择其他主题
const store = useUserStore();

const route = useRoute()
const askInfo = ref()
// 注册语言
hljs.registerLanguage('javascript', javascript);

// 初始化 markdown-it 实例并配置插件
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true, 
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        // 去除多余空格
        // const trimmedStr = str
        //   .split('\n')
        //   .map((line) => line.trimStart())
        //   .join('\n')
        //   .trim();
          console.log(str,458)
        return `<pre class="hljs"><code>${
          hljs.highlight(str, { language: lang }).value
        }</code></pre>`;
      } catch (__) {}
    }

    return '';
  },
})
  // .use(tocPlugin)
  // .use(anchorPlugin);
// // 自定义渲染器：重写 h1, h2, h3 标签的渲染规则，并去除空格
// ['h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'h7', 'div', 'p'].forEach((tag) => {
//   md.renderer.rules[`${tag}_open`] = function (
//     tokens,
//     idx,
//     options,
//     env,
//     self
//   ) {
//     const token = tokens[idx];

//     // 添加额外的属性、类名等
//     return `<${token.tag} class="custom-${tag}">`;
//   };

//   md.renderer.rules[`${tag}_close`] = function (
//     tokens,
//     idx,
//     options,
//     env,
//     self
//   ) {
//     const token = tokens[idx];
//     return `</${token.tag}>`;
//   };

//   // 修改文本内容去除首尾空格
//   md.renderer.rules[`${tag}`] = function (tokens, idx, options, env, self) {
//     const token = tokens[idx];
//     if (token.type === `${tag}`) {
//       // 去除首尾空格
//       token.content = token.content.trim();
//     }
//     return self.renderToken(tokens, idx, options);
//   };
// });
// // 自定义渲染规则，针对链接标签
// md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
//   // 添加 target="_blank" 和 rel="noopener" 到所有的 <a> 标签
//   tokens[idx].attrPush(['target', '_blank']);
//   tokens[idx].attrPush(['rel', 'noopener']);
//   // 使用默认的方式渲染标签
//   return self.renderToken(tokens, idx, options);
// };
// // 覆盖默认的元素渲染规则、元素默认会有一行的空白间距
// // 覆盖段落渲染规则
// md.renderer.rules.paragraph_open = () => '<p>';
// md.renderer.rules.paragraph_close = () => '</p>';

// // 覆盖列表渲染规则
// md.renderer.rules.bullet_list_open = () => '<ul>';
// md.renderer.rules.bullet_list_close = () => '</ul>';
// md.renderer.rules.list_open = () => '<ol>';
// md.renderer.rules.list_close = () => '</ol>';
// md.renderer.rules.list_item_open = () => '<li>';
// md.renderer.rules.list_item_close = () => '</li>';

// // 覆盖代码块渲染规则
// md.renderer.rules.code_block = (tokens, idx) => {
//   const content = tokens[idx].content.trim();
//   return `<pre><code>${content}</code></pre>`;
// };
// // 添加自定义规则
// md.core.ruler.push('custom-class-rule', function (state) {
//   const tokens = state.tokens;
//   tokens.forEach((token) => {
//     //  && token.level === 1
//     if (token.type === 'heading_open') {
//       // 为 h1 标签添加 custom-class 类
//       token.attrSet('class', ['custom-class-tit']);
//     } else if (token.type === 'bullet_list_open') {
//       token.attrSet('class', ['custom-class-ullist']);
//     } else if (token.type === 'ordered_list_open') {
//       token.attrSet('class', ['custom-class-ollist']);
//     }
//   });
// });
onMounted(() => {
  // 获取问题详情
  getQuestionsDetailsData()
  // 获取回答的列表
  getAllQuestionsData()
  getTextType()
})

const dialogTableVisible = ref(false)
// 获取问题详情
const getQuestionsDetailsData = async () => {
  await getQuestionsDetails(route.query.id)
    .then((res) => {
      if (res.code == 200) {
        const { data } = res
        askInfo.value = data
      } else {
        ElMessage({
          message:res.data.msg,
          type: 'error'
        });
      }
    })
    .catch(() => {
      ElMessage({
        message: "课程问题数据请求出错！",
        type: 'error'
      });
    });
} 
// 获取全部回答
const questParams = reactive({
  questionId: route.query.id,
  pageNo: 1,
  pageSize: 10
})
// 数据加载相关变量
const count = ref(0)
const loading = ref(false)
const questionData = ref([])
const noMore = computed(() => questionData.value.length >= count.value)
const clickLoad = () => {
  loading.value = false
  questParams.pageNo++
  getAllQuestionsData('more')
}
// 获取回答列表
const getAllQuestionsData = async (val) => {
  await getReply(questParams)
    .then((res) => {
      if (res.code == 200) {
        if (val == 'more') {
          questionData.value = questionData.value.concat(res.data.list)
        } else if(questParams.pageNo == 1){
          questionData.value = res.data.list
        } else {
          questionData.value = questionData.value.splice(0, (questParams.pageNo - 1) * questParams.pageSize).concat(res.data.list)
        }
        count.value = Number(res.data.total)
        loading.value = false
      } else {
        ElMessage({
          message:res.data.msg,
          type: 'error'
        });
      }
    })
    .catch(() => {
      ElMessage({
        message: "数据请求出错！",
        type: 'error'
      });
    });
} 

// 我要回答 - 回答题主
const description = ref('')
// 是否匿名
const anonymity = ref(false)
// 是否可以提交回答
const isSend = ref(false)
const ruleshandle = () => {
  isSend.value = description.value != '' ?  true : false
}
// 打开当前的回答的全部回复
const isReplay = ref();
const openReply = (item) => {
  // 获取回答的答复的列表
  if (item.id != isReplay.value) {
    getReplyData(item.id, 'one')
    replayHandle(item, 'answer')
  }
}

// 展示回复窗口
const openReplyFormId = ref()
// 存储一级回复数据
const answerInfo = ref({id:''}) 
// 存储二级以后的回复数据
const targetInfo = ref({id:''}) 
const replayHandle = (item, type) => {
  openReplyFormId.value = item.id
  targetInfo.value = item ;
  if (type == 'answer' ) {
    answerInfo.value = item
  };
}

// 回复数据请求参数
const replyParams = reactive({
  pageNo:1,
  pageSize:5,
  answerId: ''
})
const replyData = ref([]);
const replyCont = ref();
const replyLoding = ref(true)
const replaynoMore = computed(() => replyData.value.length >= replyCont.value)
const disabled = computed(() => replyLoding.value || replaynoMore.value)

// 弹窗滚动加载- 回复数据
const load = () => {
  replyLoding.value = false
  replyParams.pageNo++
  getReplyData()
}
// 获取回复数据
const getReplyData = async (id, st) => {
    replyLoding.value = true
    replyParams.answerId = id
    await getReply(replyParams)
    .then((res) => {
      if (res.code == 200) {
       replyLoding.value = false
       replyData.value = st == 'one' ? res.data.list : replyData.value.concat(res.data.list)

       replyCont.value = Number(res.data.total)
       isReplay.value = id
      } else {
        ElMessage({
          message:res.data.msg,
          type: 'error'
        });
      }
    })
    .catch(() => {
      ElMessage({
        message: "回复数据请求出错！",
        type: 'error'
      });
    });
}

// 提交回复数据
const params = reactive({
  questionId: '', // askInfo.value.id, // 当前问题的ID
  targetReplyId:'',
  targetUserId: '',
  answerId:'',
  content:'',
  anonymity:''
})
// 子组件 emit 回调函数 提交评论数据
function commentHandle (val){
  params.content = val.content
  params.anonymity = val.anonymity
  answerHandle() // 评论
}
// 提交回复
const answerHandle = async (type) => {
  params.questionId = askInfo.value.id
  params.targetUserId = targetInfo.value.userId || askInfo.value.userId
  if(params.content == ''){
    params.content = description.value
    params.anonymity = anonymity.value
  }
  params.answerId = answerInfo.value.id
  params.targetReplyId = targetInfo.value.id
  if (params.content == '') {
    ElMessage({
          message:'请输入您的内容！',
          type: 'success'
        });
    return 
  }
 await postAnswers(params)
    .then((res) => {
      if (res.code == 200) {
        ElMessage({
          message:'回复成功！',
          type: 'success'
        });
        
        // 第一层的回答
        if (type == 'first'){
          getAllQuestionsData()
        } else if(dialogTableVisible) {
          getReplyData(isReplay.value, 'one')
        } else {
          getReplyData(isReplay.value, 'one')
        }
        params.content = ''
        description.value = ''
        params.anonymity = ''
        anonymity.value = ''
        isSend.value = false
      } else {
        ElMessage({
          message:res.data.msg,
          type: 'error'
        });
      }
    })
    .catch(() => {
      ElMessage({
        message: "课程问题数据请求出错！",
        type: 'error'
      });
    });
}
// 点赞
const likedHandle = async (item) => {
await putLiked({bizId:item.id, liked:!item.liked, bizType: "QA"})
    .then((res) => {
      if (res.code == 200) {
        item.liked = !item.liked
        item.liked ? item.likedTimes++ : item.likedTimes--
      } else {
        ElMessage({
          message:res.data.msg,
          type: 'error'
        });
      }
    })
    .catch(() => {
      ElMessage({
        message: "点赞请求出错！",
        type: 'error'
      });
    });
}
// 文本聊天类型
const textTypeData=ref({})
const getTextType =async()=>{
  const res = await getChatTemplates()
  if(res.code===200){
    textTypeData.value=res.data
  }
}
const isAiLoading = ref(false)
let index = ref(0); // 当前显示的字符索引
let timer = ref(null); // 定时器
// 创作
const handleAi=async(num)=>{
  
  if(description.value){
    isAiLoading.value = true
    let data = ''
    if(num===1){
      // 帮写
      data=textTypeData.value.helpedWrite.replace('$input', description.value)
    }else if(num===1){
      // 续写
      data=textTypeData.value.continuedWrite.replace('$input', description.value)
    }else if(num===1){
      // 润色
      data=textTypeData.value.polish.replace('$input', description.value)
    }else{
      // 精简
      data=textTypeData.value.streamline.replace('$input', description.value)
    }
     await getChatText(data).then(res=>{
      if(res.code===200){
        // startTypewriter(res.data)
        description.value =  res.data
        isAiLoading.value = false
        ElMessage({
          message:'内容生成成功！',
          type: 'success'
        });
      }
      
     }).catch(err=>{
      ElMessage({
          message:'内容生成失败！',
          type: 'error'
        });
     })
    
  }
}
// 打字机效果
// 开始打字机效果
const startTypewriter = (val) => {
      timer.value = setInterval(() => {
        if (index.value < val.length) {
          description.value += val.charAt(index); // 逐字添加
          index.value++;
        } else {
          clearInterval(timer.value); // 停止定时器
        }
      }, 100); // 每 100ms 显示一个字
    };
// 关闭load弹层
const handleCloseAi=()=>{
  isAiLoading.value = false
}


</script>
<style lang="scss" src="./index.scss"></style>
