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
            @focus="showSuggestions"
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

<!-- 自动补全建议列表 -->
<div
  v-show="showSuggestionList"
  class="suggestion-list fixed z-50 bg-white rounded shadow-lg"
  :style="{
    top: `${searchBoxTop + searchBoxHeight}px`,
    left: `${searchBoxLeft}px`,
    width: `${searchBoxWidth}px`,
  }"
>
  <!-- 搜索历史区域 - 无滚动条，自动扩展 -->
  <div v-if="searchHistory.length > 0" class="search-history-container">
    <div class="history-header flex justify-between items-center px-4 py-2 border-b">
      <span class="text-sm font-medium text-gray-700"  style="font-size: 12px;">搜索历史</span>
      <button 
        class="flex items-center text-red-500 text-sm hover:underline" 
        @click="clearSearchHistoryHandle"
      >
        <i class="iconfont mr-1">×</i> 清空历史
      </button>
    </div>
    <div class="search-history px-4 py-2">
      <span 
        v-for="(history, index) in searchHistory" 
        :key="index" 
        class="history-tag inline-flex items-center bg-gray-100 px-3 py-1 rounded-full text-sm mr-2 mb-2 cursor-pointer hover:bg-gray-200 transition-colors"
        @click="searchByHistory(history)"
      >
        {{ history }}
        <button 
          class="ml-1 text-gray-500 hover:text-red-500" 
          @click.stop="deleteHistory(history)"
        >
          <i class="iconfont">×</i>
        </button>
      </span>
    </div>
  </div>
  
  <!-- 搜索建议区域 - 有滚动条，固定高度 -->
  <div v-if="suggestions.length > 0" class="suggestions-container">
    <div class="suggestions-header px-4 py-2 border-b">
      <span class="text-sm font-medium text-gray-700" style="font-size: 12px;">搜索建议</span>
    </div>
    <ul class="suggestions-list px-4 py-2 max-h-[280px] overflow-y-auto">
      <li
        v-for="(suggestion, index) in suggestions"
        :key="index"
        :class="{'bg-gray-100': suggestion === activeSuggestion}"
        @mouseenter="activeSuggestion = suggestion"
        @click="selectSuggestion(suggestion)"
        class="flex items-center py-2 px-2 rounded hover:bg-gray-100 cursor-pointer transition-colors"
      >
        {{ suggestion }}
      </li>
    </ul>
  </div>
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
import {getMyLearning, getClassCategorys, completeSuggest,getSearchHistory,clearSearchHistory,deleteSearchHistory} from '@/api/class.js'
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
const searchHistory = ref([]); // 搜索历史
const showSuggestionList = ref(false); // 显示建议列表

// 搜索框位置信息
const searchBoxTop = ref(0);
const searchBoxLeft = ref(0);
const searchBoxWidth = ref(0);
const searchBoxHeight = ref(0);

// 计算建议列表的高度 - 保持总高度不超过400px
const suggestionListHeight = computed(() => {
  // 搜索历史最大高度120px，搜索建议最大高度280px
  const hasHistory = searchHistory.value.length > 0;
  const hasSuggestions = suggestions.value.length > 0;
  
  if (!hasHistory && !hasSuggestions) return '0px';
  
  let height = 0;
  if (hasHistory) height += 120;
  if (hasSuggestions) height += 280;
  
  return `${Math.min(height, 400)}px`;
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

// 显示建议列表
const showSuggestions = async () => {
  showSuggestionList.value = true;
  await getSearchHistoryHandle();
  updateSuggestionPosition();
};

// 获取搜索历史
const getSearchHistoryHandle = async () => {
  try {
    const res = await getSearchHistory();
    if (res.code === 200 && res.data && res.data.length > 0) {
      searchHistory.value = res.data;
    } else {
      searchHistory.value = [];
    }
  } catch (error) {
    console.error('获取搜索历史失败', error);
    searchHistory.value = [];
  }
};

// 清空搜索历史
const clearSearchHistoryHandle = async () => {
  try {
    const res = await clearSearchHistory();
    if (res.code === 200) {
      searchHistory.value = [];
      ElMessage({
        message: '搜索历史已清空',
        type: 'success'
      });
    } else {
      ElMessage({
        message: res.msg,
        type: 'error'
      });
    }
  } catch (error) {
    console.error('清空搜索历史失败', error);
    ElMessage({
      message: '清空搜索历史出错！',
      type: 'error'
    });
  }
};

// 删除单条搜索历史
const deleteHistory = async (keyword) => {
  try {
    const res = await deleteSearchHistory(keyword);
    if (res.code === 200) {
      searchHistory.value = searchHistory.value.filter(item => item !== keyword);
      ElMessage({
        message: '已删除该搜索历史',
        type: 'success'
      });
    } else {
      ElMessage({
        message: res.data.msg,
        type: 'error'
      });
    }
  } catch (error) {
    console.error('删除搜索历史失败', error);
    ElMessage({
      message: '删除搜索历史出错！',
      type: 'error'
    });
  }
};

// 根据搜索历史进行搜索
const searchByHistory = (history) => {
  input.value = history;
  suggestions.value = [];
  dataCache.setSearchKey(input.value)
  router.push({path: '/search', query: {"key": input.value}})
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

  // 添加点击监听
  document.addEventListener('click', closeSuggestionsOnClickOutside);
});

onBeforeMount(() => {
  // 移除事件监听
  window.removeEventListener('resize', updateSuggestionPosition);
  window.removeEventListener('scroll', updateSuggestionPosition);
  document.removeEventListener('click', closeSuggestionsOnClickOutside);
});

// 监听路由 清空搜索框的值
watchEffect(() => {
  if (route.path !== '/search/index') {
    input.value = '';
    suggestions.value = [];
    showSuggestionList.value = false;
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

  // 如果点击发生在搜索框和建议列表之外的区域，则关闭建议列表
  if (
    searchBox && 
    suggestionList && 
    !searchBox.contains(event.target) && 
    !suggestionList.contains(event.target)
  ) {
    showSuggestionList.value = false; // 直接控制整个建议列表的显示状态
    suggestions.value = []; // 清空建议
  }
};

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
  overflow: hidden; // 隐藏超出内容

  // 搜索历史容器样式
  .search-history-container {
    .history-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 16px;
      border-bottom: 1px solid #f0f0f0;
      
      button {
        display: flex;
        align-items: center;
        color: #ff4d4f;
        font-size: 12px;
        background: none;
        border: none;
        cursor: pointer;
        
        &:hover {
          text-decoration: underline;
        }
      }
    }
    
    .search-history {
      padding: 8px 16px;
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
  }
  
  // 搜索建议容器样式
  .suggestions-container {
    .suggestions-header {
      padding: 8px 16px;
      border-bottom: 1px solid #f0f0f0;
    }
    
    .suggestions-list {
      padding: 8px 16px;
    }
  }
  
  // 搜索历史标签样式优化
  .history-tag {
    display: inline-flex;
    align-items: center;
    background-color: #f5f5f5;
    padding: 4px 12px;
    border-radius: 16px;
    margin: 2px;
    font-size: 12px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 100%;
    transition: background-color 0.2s;
    
    &:hover {
      background-color: #e0e0e0;
    }
    
    .iconfont {
      margin-right: 4px;
    }
    
    button {
      margin-left: 4px;
      padding: 0;
      background: none;
      border: none;
      cursor: pointer;
    }
  }
  
  // 搜索建议列表样式优化
  .suggestions-list {
    list-style: none;
    margin: 0;
    padding: 0;
    
    li {
      display: flex;
      align-items: center;
      padding: 8px 12px;
      cursor: pointer;
      transition: background-color 0.2s;
      border-radius: 4px;
      
      &:hover, &.bg-gray-100 {
        background-color: #f5f5f5;
      }
      
      .iconfont {
        margin-right: 8px;
        color: #999;
      }
    }
  }
}
</style>  