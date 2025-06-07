<template>
  <div class="mainWrapper">
    <div class="container banner fx" style="overflow: hidden;">
      <div class="categorys bg-wt">
        <!-- 头部分类 -->
        <ClassCategory :data="classCategorys"></ClassCategory>
      </div>
      <!-- 头部幻灯片 -->
      <Swiper :data="imags"></Swiper>
    </div>
    <!-- 兴趣模块  -->
    <div class="container" v-if="interest.size > 0">
      <Interest :data="interest" :key="intKey" @setInterest="setInterest"></Interest>
    </div>
    <!-- 兴趣模块 -->
    <!-- 直播公开课 -->
    <div class="bg-wt pd-tp-30">
      <OpenClass title="直播公开课" class="container bg-wt" :data="freeClassData"></OpenClass>
    </div>
    <!-- 新课推荐 -->
    <div class="pd-tp-30 bg-wt">
      <OpenClass title="新课推荐" class="container" :data="freeClassData"></OpenClass>
    </div>
    <!-- 广告位 -->
    <div class="globalTopBanner" style="display: block;">
      <img src="/src/assets/adv.png" />
    </div>
    <!-- 精品好课 -- start -->
    <div class="pd-tp-30">
      <OpenClass title="精品好课" class="container" :data="goodClassData"></OpenClass>
    </div>
    <!-- 精品好课 -- end -->
    <!-- 精品新课 -- start -->
    <div class="pd-tp-30">
      <OpenClass title="精品新课" class="container" :data="newClassData"></OpenClass>
    </div>
    <!-- 精品好课 -- end -->
    <!-- 兴趣选择设置 -- start -->
    <div class="interest">
      <el-dialog v-model="interestDialog" width="80%" :show-close="false">
        <template #header="{ close, titleId }">
          <div class="dialogHead fx-sb">
            <div>
              <span class="titleClass marg-rt-10">设置学习兴趣</span>
              <span class="ft-cl-des">打造你的专属在线学习平台</span>
            </div>
            <div class="butCont fx">
              <span class="bt-grey marg-rt-15" @click="close">下次再选</span>
              <span class="bt" @click="saveInterest">保存</span>
            </div>
          </div>
        </template>
        <CheckInterest :data="classCategorys" :initValue="interest" @setInterestList="setInterestList"></CheckInterest>
      </el-dialog>
    </div>
    <div class="floatCont fx-fd-col fx-ct">
      <router-link to="/main/coupon">
        <img src="@/assets/coup.png" alt="优惠券" />
      </router-link>
      <!-- 添加AI聊天图标按钮 -->
      <router-link to="/main/ai">
        <svg t="1749275479538" class="icon" viewBox="0 0 1391 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4235" width="120" height="120" style="margin-right: 60px;"><path d="M980.736 172.527c-8.79-4.396-12.585 3.983-17.72 8.241-1.75 1.374-3.244 3.16-4.738 4.808-12.844 14.01-27.85 23.214-47.476 22.115-28.674-1.648-53.159 7.555-74.794 29.945-4.601-27.61-19.883-44.093-43.148-54.67-12.191-5.494-24.485-10.989-33.019-22.94-5.94-8.516-7.555-17.994-10.542-27.334-1.889-5.632-3.778-11.401-10.13-12.363-6.92-1.099-9.616 4.808-12.329 9.753-10.817 20.192-15.024 42.445-14.595 64.972 0.928 50.687 21.91 91.071 63.565 119.78 4.739 3.296 5.958 6.593 4.464 11.4-2.833 9.89-6.233 19.506-9.203 29.396-1.89 6.319-4.722 7.692-11.367 4.945-22.854-9.753-42.6-24.176-60.062-41.62-29.618-29.259-56.404-61.539-89.8-86.813a394.916 394.916 0 0 0-23.815-16.621c-34.083-33.791 4.464-61.538 13.393-64.835 9.323-3.434 3.245-15.247-26.923-15.11s-57.761 10.44-92.926 24.176c-5.15 2.06-10.56 3.571-16.088 4.808-31.937-6.182-65.075-7.555-99.708-3.572-65.195 7.418-117.273 38.874-155.562 92.582-45.982 64.56-56.817 137.912-43.544 214.422 13.908 80.632 54.24 147.39 116.191 199.587 64.251 54.12 138.238 80.632 222.647 75.55 51.27-3.023 108.344-10.028 172.732-65.66 16.243 8.242 33.293 11.539 61.556 14.011 21.789 2.06 42.754-1.099 58.98-4.533 25.429-5.494 23.66-29.533 14.474-33.928-74.536-35.44-58.173-21.017-73.042-32.692 37.877-45.742 94.951-93.27 117.273-247.252 1.768-12.225 0.274-19.917 0-29.807-0.138-6.044 1.201-8.38 7.984-9.066 18.664-2.198 36.796-7.418 53.434-16.758 48.282-26.923 67.77-71.154 72.372-124.176 0.687-8.104-0.137-16.483-8.534-20.741M559.911 649.722c-72.218-57.967-107.263-77.06-121.737-76.236-13.53 0.824-11.092 16.621-8.122 26.923 3.108 10.165 7.177 17.17 12.86 26.099 3.916 5.907 6.629 14.698-3.931 21.291-23.266 14.698-63.702-4.945-65.59-5.906-47.081-28.297-86.453-65.66-114.183-116.758-26.785-49.176-42.324-101.923-44.9-158.241-0.687-13.599 3.245-18.407 16.484-20.88a159.683 159.683 0 0 1 52.901-1.373c73.73 10.99 136.504 44.643 189.113 97.94 30.031 30.356 52.747 66.62 76.167 102.06 24.88 37.636 51.666 73.488 85.749 102.883 12.036 10.302 21.634 18.132 30.837 23.901-27.73 3.16-74.003 3.846-105.648-21.703m34.632-227.334c0-6.044 4.74-10.852 10.697-10.852q2.026 0.035 3.64 0.687a10.766 10.766 0 0 1 6.903 10.165 10.714 10.714 0 0 1-10.68 10.852 10.594 10.594 0 0 1-10.56-10.852m107.538 56.318c-6.886 2.885-13.788 5.358-20.433 5.632-10.268 0.55-21.497-3.708-27.575-8.928-9.478-8.105-16.244-12.638-19.077-26.786-1.219-6.044-0.55-15.384 0.533-20.741 2.438-11.539-0.275-18.956-8.242-25.687-6.49-5.495-14.75-7.006-23.815-7.006-3.383 0-6.49-1.51-8.791-2.747a9.014 9.014 0 0 1-3.915-12.637c0.944-1.923 5.546-6.594 6.628-7.418 12.31-7.143 26.51-4.807 39.628 0.55 12.174 5.082 21.36 14.423 34.633 27.61 13.53 15.934 15.968 20.33 23.66 32.28 6.096 9.34 11.642 18.956 15.42 29.944 2.317 6.869-0.67 12.5-8.654 15.934" fill="#4D6BFE" p-id="4236"></path></svg>
      </router-link>
      <el-backtop :bottom="40" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { isLogin, dataCacheStore } from '@/store';
import { ElMessage } from 'element-plus';
import { getClassCategorys, getRecommendClassList, setInterests, getInterests } from '@/api/class.js';
import ClassCategory from '@/components/ClassCategory.vue';
import CheckInterest from './components/CheckInterest.vue';
import Interest from './components/Interest.vue';
import OpenClass from './components/OpenClass.vue';
import Swiper from './components/Swiper.vue';
import banner1 from '@/assets/banner1.jpg';
import banner2 from '@/assets/banner2.jpg';
import banner3 from '@/assets/banner3.jpg';
const dataCache = dataCacheStore();
// 分类数据
const classCategorys = ref([]);
// banner幻灯片图片
const imags = [banner1, banner2, banner3];
// 精品公开课的数据
const freeClassData = ref([]);
// 兴趣弹窗
const interestDialog = ref(false);
// 精品好课数据
const goodClassData = ref([]);
// 精品新课数据
const newClassData = ref([]);
// mounted生命周期
onMounted(async () => {
  // 获取三级分类信息
  getClassCategoryData();
  // 获取精品公开课
  getFreeClassListData();
  getGoodClassListData();
  getNewClassListData();
  // 获取兴趣列表 （二级分类）
  if (await isLogin()) {
    getInterestData();
  }
});

/** 方法定义 **/

// 获取一、二、三级分类信息
const getClassCategoryData = async () => {
  await getClassCategorys()
    .then((res) => {
      if (res.code === 200) {
        classCategorys.value = res.data;
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
};
// 打开修改兴趣弹窗
const setInterest = (val) => {
  interestCatch.value = interest.value;
  interestDialog.value = val;
};
// 精品公开课接口 - 公开课取消
const getFreeClassListData = async () => {
  await getRecommendClassList("free")
    .then((res) => {
      if (res.code === 200) {
        freeClassData.value = res.data;
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
};
// 精品公开课接口
const getGoodClassListData = async () => {
  await getRecommendClassList('best')
    .then((res) => {
      if (res.code === 200) {
        goodClassData.value = res.data;
      } else {
        ElMessage(res.meg);
      }
    })
    .catch(() => {
      ElMessage("分类请求出错！");
    });
};
// 新课推荐
const getNewClassListData = async () => {
  await getRecommendClassList('new')
    .then((res) => {
      if (res.code === 200) {
        newClassData.value = res.data;
      } else {
        ElMessage(res.meg);
      }
    })
    .catch(() => {
      ElMessage({
        message: "分类请求出错！",
        type: "error",
      });
    });
};
// 保存设置的兴趣变量
const interest = ref(new Set());
const intKey = ref(1);
// 获取兴趣
const getInterestData = async () => {
  await getInterests()
    .then((res) => {
      if (res.code === 200) {
        if (res.data.length === 0) {
          interestDialog.value = true;
        }
        interest.value = new Set(res.data);
      } else {
        console.log(res.msg)
        interestDialog.value(true);
      }
    })
    .catch(() => {
      ElMessage({
        message: "兴趣列表获取失败！",
        type: "error",
      });
    });
};

// 更改兴趣的时候记录
const interestCatch = ref(new Set());
const setInterestList = (list) => {
  interestCatch.value = list;
};

// 保存兴趣
const saveInterest = async () => {
  let str = "";
  for (let val of interestCatch.value) {
    if (typeof val == "string") {
      str == "" ? (str = val) : (str += `,${val}`);
    } else {
      str == "" ? (str = val.id) : (str += `,${val.id}`);
    }
  }
  if (str == "") {
    ElMessage({
      message: "您还没有选择兴趣，请先选择兴趣后再保存！",
      type: "success",
    });
    return;
  }
  await setInterests({ interestedIds: str })
    .then((res) => {
      if (res.code === 200) {
        ElMessage({
          message: "兴趣保存成功！",
          type: "success",
        });
        getInterestData();
        intKey.value++;
        interestDialog.value = false;
      } else {
        ElMessage({
          message: res.data.msg,
          type: "error",
        });
      }
    })
    .catch(() => { });
};
</script>

<style lang="scss" src="./index.scss"></style>