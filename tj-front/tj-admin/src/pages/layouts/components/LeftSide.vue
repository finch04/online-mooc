<!-- 架构页面 - 左侧导航 -->
<template>
  <div class="LeftSider fx-fd-col">
    <div  @click="() => $router.push('/')" class="logo cursor fx-ct"><img src="@/assets/newlogo.png" alt="" srcset="" /></div>
    <div class="nav-wrapper"> <!-- 新增包裹层 -->
      <div class="nav">
        <el-menu
          :default-active="activeIndex"
          :default-openeds="defaultOpeneds"
          class="el-menu-vertical-demo"
          :unique-opened="true"
          @open="handleOpen"
          @close="handleClose"
          @select="handleSelect"
        > 
          <!-- <div class="first-menu">
            <el-menu-item index="99" :key="99" @click="goPath(`/`)">
              <i class="iconfont" v-html="basePath[0].meta.icon"></i>
              <span>工作台</span>
            </el-menu-item>
          </div>  -->
          <el-sub-menu>
            <template #title>
              <i class="iconfont" v-html="workbench"></i>
              <span>工作台</span>
            </template>
            <el-menu-item index="99" :key="99" @click="goPath(`/`)">
              <span>工作台</span>
            </el-menu-item>
            <el-menu-item index="100" :key="100" @click="goPath(`/main/dashboard`)">
              <span>数据大屏</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu v-for="(item, index) in basePath"  :key="index"  :index="index.toString()" >
            <template #title>
              <i class="iconfont" v-html="item.meta.icon"></i>
              <span>{{item.meta.title}}</span>
            </template>
            <el-menu-item v-for="(it, ind) in item.children" :key="ind" :index="`${index}-${ind}`" @click="goPath(`${it.path}`)">
              {{it.meta.title}}
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>
    </div>
    <span class="decorate"></span>
  </div>
</template>
<script setup>
import { ref, computed, watchEffect, } from 'vue';
import { useRoute } from 'vue-router';
import router, { asyncRouterList } from '@/router';
import { catchDataesStore,useUserStore } from '@/store';
// 全部路由信息
const routers = asyncRouterList

// 当前路由下的信息
const route = useRoute()

const store = catchDataesStore()
const useStore =useUserStore()

const activeIndex = ref('99')// ref(store.getDefaultIndex)
const defaultOpeneds = ref()// ref(store.getDefaultOpeneds)


const workbench = ref('<svg t="1748945482496" class="icon" style="margin-bottom:18px" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5494" width="18" height="18"><path d="M953.342411 141.241379C953.312026 141.241379 953.37931 728.083791 953.37931 728.083791 953.37931 728.14313 70.657589 728.123657 70.657589 728.123657 70.687974 728.123657 70.62069 141.281245 70.62069 141.281245 70.62069 141.221923 953.342411 141.241379 953.342411 141.241379ZM0 728.083791C0 767.026723 31.652899 798.744346 70.657589 798.744346L953.342411 798.744346C992.425472 798.744346 1024 767.121902 1024 728.083791L1024 141.281245C1024 102.33833 992.347101 70.62069 953.342411 70.62069L70.657589 70.62069C31.574528 70.62069 0 102.243134 0 141.281245L0 728.083791ZM840.751492 963.941976C860.252849 963.941976 876.061837 948.132988 876.061837 928.631631L876.061837 926.987864C876.061837 907.486508 860.252849 891.677519 840.751492 891.677519L183.248508 891.677519C163.747151 891.677519 147.938163 907.486508 147.938163 926.987864L147.938163 928.631631C147.938163 948.132988 163.747151 963.941976 183.248508 963.941976 202.749881 963.941976 840.751492 963.941976 840.751492 963.941976ZM475.867772 892.499403 546.488461 892.499403 546.488461 798.744346 477.511539 798.744346 475.867772 892.499403Z" fill="#2c2c2c" p-id="5495"></path></svg>')

// 处理侧边栏数据
const sideMenu = computed(() => {
  const newMenuRouters = [];
  routers.forEach((menu) => {
  })
  return newMenuRouters;
});

// 处理导航数据
const getMenuList = (list, basePath) => {
  if (!list) {
    return [];
  }
  return list
    .map((item) => {
      const path = basePath ? `${basePath}/${item.path}` : item.path;
      return {
        path,
        title: item.meta?.title,
        icon: item.meta?.icon || '',
        children: getMenuList(item.children, path),
        meta: item.meta,
        redirect: item.redirect,
      };
    })
    .filter((item) => item.meta && item.meta.hidden !== true);
};
// 展示基础路由
const basePath = getMenuList(routers)
// 进入导航
const goPath = (path) => {
  useStore.setTabNumber(0)
  router.push(path)
}
const handleOpen = (key) => {
  store.setDefaultOpeneds(key)
}
const handleClose = (key) => {
  store.setDefaultOpeneds(key)
}
const handleSelect = (key) => {
  store.setDefaultIndex(key)
}
// 处理  页面点击 菜单跟着动
watchEffect(()=>{
  if (basePath){
    const path = route.path.toString()
    // 如果是首页
    if(path == '/main/index' || path == '/'){
        activeIndex.value = '99'
        return 
      }else if(path == '/main/dashboard'){
        activeIndex.value = '100'
        return 

      }
    // 如果是 三级子页  
    if(route.meta && route.meta.fmeta){
      const cpath = route.meta.fmeta.path
      basePath.forEach((item,index) => {
        const regA = new RegExp(item.path)
        // 非首页 在当前的路由下查找 
        if (cpath.search(regA) != -1){
          defaultOpeneds.value == [index.toString()] ? null : defaultOpeneds.value = [index.toString()]
          item.children.forEach((val, ind) => {
            if (val.path == cpath){
              activeIndex.value == `${index}-${ind}` ? null : activeIndex.value = `${index}-${ind}`
            } 
          })
        }
      })
      return ;
    }
    // 非首页的二级页面
    basePath.forEach((item,index) => {
      const regA = new RegExp(item.path)
      // 非首页 在当前的路由下查找 
      if (path.search(regA) != -1){
        defaultOpeneds.value == [index.toString()] ? null : defaultOpeneds.value = [index.toString()]
        item.children.forEach((val, ind) => {
          if (val.path == route.path){
            activeIndex.value == `${index}-${ind}` ? null : activeIndex.value = `${index}-${ind}`
          } 
        })
      }
    })
  }
})
</script>
<style lang="scss" scoped>
.LeftSider {
  position: relative;
  position: fixed;
  overflow: hidden;
  z-index: 999;
  width: 226px;
  height: 100vh;
  background-image: linear-gradient(130deg, #FAF8F4 0%, #F4EEE6 53%);
  &::before, &::after{
    position: absolute;
    content: '';
    // 设置背景图
    background-image: url(@/assets/leftbackground.png);
    // 设置背景图大小
    background-size: 183px 168px;
    z-index: 0;
    width: 183px;
    height: 168px;
  }
   &::after{
    // left: auto;
    // top: -140px;
    // right: -160px;
  }
  .decorate{
    top: 50vh;
    position: absolute;
    display: inline-block;
    width: 63px;
    height: 195px;
    opacity: 0.29;
    background: #FF6B3D;
    border-radius: 97px;
    filter: blur(50px);
  }
  .logo{
    position: relative;
    z-index: 9;
    margin-top: 30px;
    img{
      width: 98px;
      height: 88px;
    }
    margin-bottom: 47px;
  }
  .title{
    position: relative;
    z-index: 9;
    text-align: center;
    line-height: 60px;
    font-size: 18px;
    margin-bottom: 15px;
    color: #000000;
  }
  .nav-wrapper { // 新增样式
    height: calc(100vh - 150px); // 减去logo和装饰元素的高度
    overflow-y: auto;
  }
  .nav{
    position: relative;
    z-index: 9;
    font-size: 14px;
    margin-right: 20px;
    .navIcon{
      margin-right: 16px;
    }
    .iconfont{
      font-size: 20px;
      margin-right: 10px;
    }
    .item{
      padding-left: 46px;
      padding-right: 43px;
      .navTopTit{
        display: flex;
        justify-content: space-between;
        padding: 10px 0;
        cursor: pointer;
        .vanIcon{
          width: 16px;
          height: 16px;
        }
      }
      .navListTit{
        // padding-left: 20px;
      }
    }
  }
  #svg{
    fill:red;
  }
  .first-menu{
    height: 46px;
    line-height: 46px;
    .el-menu-item{
      padding-left: 46px !important;
      height: 46px;
      line-height: 46px;
      border-radius: 0 100px 100px 0;
      &:hover{
        background-color: #765A58;
      }
    }
    .is-active{
      color:#fff;
      background: #765A58;
      border-radius: 0 100px 100px 0;
      height: 46px;
      &:hover{
        color: #E4DEDE;
      }
    }
  }
  :deep(.el-sub-menu__title){
    padding-left: 46px !important;
    border-radius: 0 100px 100px 0;
  }
  :deep(.el-sub-menu__icon-arrow){
    font-size: 15px;
    // color: #765A58;
  }
  :deep(.is-active .el-sub-menu__icon-arrow){
    font-size: 15px;
    // color: #fff;
    font-weight: 600;
  }
  :deep(.is-active > .el-sub-menu__title){
    color:#fff;
    background: #765A58;
    border-radius: 0 100px 100px 0;
    height: 46px;
  }
  :deep(.is-active > .el-sub-menu__title:hover){
    background: #765A58;
    border-radius: 0 100px 100px 0;
    color:#E4DEDE;
  }
  :deep(.el-menu-item:hover, .el-sub-menu__title:hover){
    background: transparent;
    color:#FF6B3D;
  }
  :deep(#svg){
    fill:red;
  }
  :deep(.el-sub-menu__title:hover){
    background: transparent;
    color:#FF6B3D;
  }
  :deep(.el-menu){
    background-color: transparent;
    border: none;
  }
  :deep(.el-menu-item){
    padding-left: 77px !important;
  }
}
</style>