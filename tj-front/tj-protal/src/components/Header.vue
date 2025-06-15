<template>
  <header class="bg-wt">
    <div class="container fx">
      <div class="logo">
        <router-link to="/"><img src="@/assets/logo.png" alt="" srcset=""></router-link>
      </div>
      <!-- 头部分类-start -->
      <div v-if="route.path != '/main/index' && route.path != '/login'" class="courseClass font-bt2"
           @mouseover="() => isShow = true" @mouseout="() => isShow = false">
        <i class="iconfont zhy-icon_fenlei_nor"></i> 分类
      </div>
      <div v-if="route.path != '/main/index' && route.path != '/login'" class="courseClassList" v-show="isShow"
           @mouseover="() => isShow = true" @mouseout="() => isShow = false">
        <div class="firstItems">
          <ClassCategory :data="courseClass" type="float"></ClassCategory>
        </div>
      </div>
      <!-- 头部分类-end -->
      <div class="fx-1 fx-ct relative">
        <el-input
            v-model="input"
            class="headerSearch "
            size="large"
            placeholder="请输入关键字"
            @input="handleInput"
            @keyup.enter="SearchHandle"
        >
          <template v-slot:prefix>
            <Search class="search" @click="SearchHandle"/>
          </template>
        </el-input>
      </div>
      <div class="fx-al-ct pt-rt" style="align-items: center;">
        <div class="car fx-al-ct font-bt2" v-if="userInfo" @click="() => $router.push('/personal/main/myMessage')">
          <el-badge v-if="notReadCount!=0" :value="notReadCount" style="margin-right: 20px;">
            <i class="iconfont">&#xe612;</i>
          </el-badge>
          <i v-if="notReadCount==0" class="iconfont">&#xe612;</i>
        </div>
        <div class="car fx-al-ct font-bt2" v-if="userInfo" @click="() => $router.push('/pay/carts')">
          <i class="iconfont">&#xe6f3;</i> 购物车
        </div>
        <!-- 学习中心 - start -->
        <div v-if="userInfo && userInfo.name">
          <span class="marg-lr-40 font-bt2" style="padding:27px 0"
                @click="() => {$router.push('/personal/main/myClass')}" @mouseover="()=> learningShow = true"
                @mouseout="() => learningShow = false">学习中心</span>
          <div class="learningCont" v-show="learningShow && learnClassInfo && learnClassInfo.courseAmount"
               @mouseover="()=> learningShow = true" @mouseout="() => learningShow = false">
            <div class="count"><em>{{ learnClassInfo && learnClassInfo.courseAmount }}</em> 门课程</div>
            <div class="info" v-if="learnClassInfo &&learnClassInfo.courseId">
              <div class="fx-sb">
                <span>正在学习：</span>
                <div class="fx">
                  <span class="bt"
                        @click="() => $router.push({path: '/learning/index', query: {id: learnClassInfo.courseId}})">继续学习</span>
                  <span class="bt bt-grey1" @click="() => $router.push('/personal/main/myClass')">全部课程</span>
                </div>
              </div>
              <div class="tit">{{ learnClassInfo && learnClassInfo.courseName }}</div>
              <div class="perc fx-sb"> {{ learnClassInfo && learnClassInfo.latestSectionName }}
                <i>{{ learnClassInfo && (Math.round(learnClassInfo.learnedSections * 100 / learnClassInfo.sections)) }}%</i>
              </div>
            </div>
          </div>
        </div>
        <!-- 登录注册 - start -->
        <div class="fx-al-ct" v-if="userInfo && userInfo.name">
          <img class="headIcon" :src="userInfo.icon" :onerror="onerrorImg" alt="">
          <div>{{ userInfo.name }}</div>
          <!-- <div class="font-bt2 pd-lf-10" @click="() => $router.push('/login')"> 退出 </div> -->
        </div>
        <div class="cur-pt" v-else>
          <span class="font-bt2" @click="() => $router.push({path: '/login', query: {md: 'register'}})">注册 </span><span>/</span>
          <span class="font-bt2" @click="() => $router.push('/login')"> 登录</span>
        </div>
      </div>
    </div>
  </header>
  
  <!-- 自动补全建议列表 - 移至header组件外部 -->
  <div
    v-show="suggestions.length > 0 && input.length > 0"
    class="suggestion-list fixed z-50 bg-white rounded shadow-lg"
    :style="{
      top: `${searchBoxTop + searchBoxHeight}px`,
      left: `${searchBoxLeft}px`,
      width: `${searchBoxWidth}px`,
      maxHeight: '200px', // 设置最大高度限制
      height: suggestionListHeight // 应用动态计算的高度
    }"
  >
    <ul>
      <li
        v-for="(suggestion, index) in suggestions"
        :key="index"
        :class="{'bg-gray-100': suggestion === activeSuggestion}"
        @mouseenter="activeSuggestion = suggestion"
        @click="selectSuggestion(suggestion)"
      >
        {{ suggestion }}
      </li>
    </ul>
  </div>
</template>

<script setup>
import defaultImage from '@/assets/icon.jpeg'
import {onBeforeMount, onMounted, ref, watchEffect, computed, nextTick} from "vue";
import {Search} from "@element-plus/icons-vue";
import {useUserStore, isLogin, getToken, dataCacheStore} from '@/store'
import {getUserInfo} from "@/api/user"
import router from "../router";
import {useRoute} from "vue-router";
import {ElMessage} from "element-plus";
import ClassCategory from "./ClassCategory.vue";
import {getMyLearning, getClassCategorys, completeSuggest} from '@/api/class.js'
import {tryRefreshToken} from '../utils/refreshToken'
import { getNotRead } from '../api/message';
import { debounce } from 'lodash'; // 引入防抖函数

const store = useUserStore();
const userInfo = ref()
const isToken = sessionStorage.getItem('token') ? true : false
const input = ref('');
const route = useRoute()
const userStore = getToken();
const dataCache = dataCacheStore();
const notReadCount = ref(0) // 未读消息数据
const courseClass = ref([]) // 分类数据
const isShow = ref(false)  // 分类展示
const learnClassInfo = ref(null) // 我真正学习的课程信息-学习中心展示
const learningShow = ref(false) // 学习中心hover模块展示
const suggestions = ref([]); // 自动补全建议
const activeSuggestion = ref(''); // 当前选中的建议
const isLoading = ref(false); // 加载状态

// 搜索框位置信息
const searchBoxTop = ref(0);
const searchBoxLeft = ref(0);
const searchBoxWidth = ref(0);
const searchBoxHeight = ref(0);

// 计算建议列表的高度
const suggestionListHeight = computed(() => {
  if (suggestions.value.length === 0) return '0px';
  
  // 每个列表项的高度 + 垂直内边距
  const itemHeight = 40; // 可根据实际样式调整
  const totalHeight = suggestions.value.length * itemHeight;
  
  // 设置最大高度限制，避免过多条目导致界面过长
  const maxHeight = 200; // 最大高度，与CSS中保持一致
  
  return totalHeight > maxHeight ? `${maxHeight}px` : `${totalHeight}px`;
});

// 使用防抖处理输入事件，将防抖延迟缩短为 150ms
const debouncedFetchSuggestions = debounce(async (query) => {
  if (query.length < 2) { // 至少输入2个字符才触发搜索
    suggestions.value = [];
    return;
  }
  
  isLoading.value = true;
  try {
    const res = await completeSuggest(query);
    if (res.code === 200 && res.data && res.data.length > 0) {
      suggestions.value = res.data;
      updateSuggestionPosition(); // 更新建议列表位置
    } else {
      suggestions.value = [];
    }
  } catch (error) {
    console.error('获取自动补全建议失败', error);
    suggestions.value = [];
  } finally {
    isLoading.value = false;
  }
}, 150); // 150ms防抖延迟

// 处理输入事件
const handleInput = (value) => {
  input.value = value;
  debouncedFetchSuggestions(value);
};

// 选择建议
const selectSuggestion = (suggestion) => {
  input.value = suggestion;
  suggestions.value = [];
  dataCache.setSearchKey(input.value)
  router.push({path: '/search', query: {"key": input.value}})
};

// 更新建议列表位置
const updateSuggestionPosition = () => {
  nextTick(() => {
    const searchBox = document.querySelector('.headerSearch');
    if (searchBox) {
      const rect = searchBox.getBoundingClientRect();
      searchBoxTop.value = rect.top + window.scrollY;
      searchBoxLeft.value = rect.left + window.scrollX;
      searchBoxWidth.value = rect.width;
      searchBoxHeight.value = rect.height;
    }
  });
};

onBeforeMount(async () => {
  // 尝试获取用户信息
  const ui = store.getUserInfo;
  if (!ui) {
    if (await isLogin()) {
      let res = await getUserInfo();
      if (res.code === 200 && !!res.data) {
        userInfo.value = res.data
        // 记录到store 并调转到首页
        store.setUserInfo(res.data);
      }
    } else {
      userStore.logout();
    }
  } else {
    userInfo.value = ui
  }

  courseClass.value = dataCache.getCourseClassDataes
  // 先从store里拿如何没有就请求分类信息获取
  if (courseClass.value.length === 0) {
    getCourseClassHandle()
  }
  if (Object.keys(route.query).length > 0) {
    input.value = route.query.key
  }
  if (await isLogin()) {
    // 查询我正在学习的课程
    getLearnClassInfoHandle()
    //查询未读消息数
    getNotReadCount()
  }
})

onMounted(() => {
  // 初始化建议列表位置
  updateSuggestionPosition();
  
  // 监听窗口大小变化，更新建议列表位置
  window.addEventListener('resize', updateSuggestionPosition);
  
  // 监听滚动事件，更新建议列表位置
  window.addEventListener('scroll', updateSuggestionPosition);
});

onBeforeMount(() => {
  // 移除事件监听
  window.removeEventListener('resize', updateSuggestionPosition);
  window.removeEventListener('scroll', updateSuggestionPosition);
});

// 监听路由 清空搜索框的值
watchEffect(() => {
  if (route.path !== '/search/index') {
    input.value = '';
    suggestions.value = [];
  } else {
    input.value = dataCache.getSearchKey;
  }
})

// 查询未读消息
const getNotReadCount = async () => {
  await getNotRead()
      .then((res) => {
        if (res.code === 200) {
          notReadCount.value = res.data;
        } else {
          ElMessage({
            message: res.data.msg,
            type: "error",
          });
        }
      })
      .catch(() => {
        ElMessage({
          message: "未读消息查询出错！",
          type: "error",
        });
      });
}

// 查询我正在学习的课程
const getLearnClassInfoHandle = async () => {
  await getMyLearning()
      .then((res) => {
        if (res.code === 200) {
          learnClassInfo.value = res.data;
        } else {
          ElMessage({
            message: res.data.msg,
            type: "error",
          });
        }
      })
      .catch(() => {
        ElMessage({
          message: "学习状态查询出错！",
          type: "error",
        });
      });
}

// 获取课程分类
const getCourseClassHandle = async () => {
  await getClassCategorys()
      .then((res) => {
        if (res.code == 200) {
          courseClass.value = res.data;
          dataCache.setCourseClassDataes(res.data)
        } else {
          ElMessage({
            message: res.data.msg,
            type: "error",
          });
        }
      })
      .catch(() => {
        ElMessage({
          message: "分类请求出错！",
          type: "error",
        });
      });
}

// 默认头像
const onerrorImg = () => {
  userInfo.value.icon = defaultImage;
}

// 搜索事件
const SearchHandle = () => {
  console.log('搜索事件',input.value)
  if (input.value == '') {
    ElMessage({
      type: 'error',
      message: '请输入搜索关键词！'
    })
    return false
  }
  dataCache.setSearchKey(input.value)
  router.push({path: '/search', query: {"key": input.value}})
}

// 点击页面其他地方关闭建议列表
const closeSuggestionsOnClickOutside = (event) => {
  const searchBox = document.querySelector('.headerSearch');
  const suggestionList = document.querySelector('.suggestion-list');
  
  if (
    searchBox && 
    suggestionList && 
    !searchBox.contains(event.target) && 
    !suggestionList.contains(event.target)
  ) {
    suggestions.value = [];
  }
};

// 添加点击监听
onMounted(() => {
  document.addEventListener('click', closeSuggestionsOnClickOutside);
});

// 移除点击监听
onBeforeMount(() => {
  document.removeEventListener('click', closeSuggestionsOnClickOutside);
});
</script>

<style lang="scss" scoped>
header {
  width: 100%;
  background-color: var(--color-white);
  text-align: left;
  padding: 11px 0;
  font-size: 14px;

  .courseClass {
    position: relative;
    line-height: 48px;
    margin-left: 26px;
    display: flex;
    font-size: 14px;

    .iconfont {
      font-size: 24px;
      margin-right: 5px;
    }
  }

  .courseClassList {
    position: absolute;
    z-index: 999;
    top: 50px;
    left: 102px;

    .firstItems {
      background-color: #fff;
    }
  }

  .headerSearch {
    width: 427px;
    height: 40px;
    background: #edf0f4;
    border-radius: 8px;

    :deep(.el-input__wrapper) {
      background-color: transparent;
    }

    .search {
      position: absolute;
      cursor: pointer;
      right: 0;
      width: 15px;
      height: 15px;
    }
  }

  .car {
    img {
      width: 24px;
      height: 25px;
    }

    .iconfont {
      font-size: 24px;
      margin-right: 6px;
    }
  }

  .learningCont {
    position: absolute;
    z-index: 999;
    width: 330px;
    height: 200px;
    border-radius: 8px;
    left: -40px;
    top: 45px;
    background-color: #fff;
    box-shadow: 0 4px 6px 2px rgba(108, 112, 118, 0.17);

    &::before {
      position: absolute;
      z-index: -1;
      content: '';
      top: -5px;
      left: 43%;
      display: inline-block;
      width: 15px;
      height: 15px;
      background-color: #fff;
      transform: rotate(45deg);
      box-shadow: 0 4px 6px 2px rgba(108, 112, 118, 0.17);
    }

    .count {
      background-color: #fff;
      padding: 20px 20px 20px 20px;
      line-height: 40px;
      border-bottom: 1px solid #EEEEEE;
      display: flex;

      em {
        color: var(--color-main);
        font-family: PingFangSC-S0pxibold;
        font-weight: 600;
        font-size: 28px;
        margin-right: 4px;
        font-style: normal;
      }
    }

    .info {
      padding: 13px 20px 20px 20px;
      line-height: 28px;
      color: #80878C;

      .bt {
        line-height: 28px;
        height: 28px;
        font-size: 12px;
        padding: 0 10px;
        margin-left: 10px;
      }

      .tit {
        font-weight: 600;
        font-size: 14px;
        line-height: 28px;
        color: #19232B;
      }

      .perc {
        i {
          font-style: normal;
          color: #80878C;
        }
      }
    }
  }

  .headIcon {
    width: 30px;
    height: 30px;
    border-radius: 100%;
    margin-right: 10px;
  }
}

// 自动补全样式
.suggestion-list {
  position: absolute;
  z-index: 999;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 4px 6px 2px rgba(108, 112, 118, 0.17);
  border: 1px solid #eee;
  overflow: auto; // 当内容超出高度时显示滚动条
  
  // 确保列表项之间有分隔线
  ul {
    list-style: none;
    margin: 0;
    padding: 0;
    
    li {
      padding: 10px 16px;
      cursor: pointer;
      transition: background-color 0.2s;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      &:hover {
        background-color: #f5f5f5;
      }
    }
  }
}
</style>