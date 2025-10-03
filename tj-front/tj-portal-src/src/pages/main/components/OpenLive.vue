<!-- 顶部Banner区域左侧直播列表 -->
<template>
    <div class="openLive">
       <MainTitle class="marg-bt-20" :title="title"></MainTitle>
       <div class="liveCont fx-wp">
         <LiveCards class="items marg-bt-20" v-for="(item, index) in data" :data="item" :key="index" :type="type"></LiveCards>
       </div>
    </div>
</template>
<script setup>
import MainTitle from '@/components/MainTitle.vue'
import LiveCards from '@/components/LiveCards.vue' // 引入直播卡片组件

const props = defineProps({
    title:{
    type: String,
    default:'直播模块'
    },
    data:{
    type: Array,
    default:[]
    },
    type:{
    type: String,
    default:'default' // 支持传递类型给子组件，保持与ClassCards一致的用法
    }
})

</script>
<style lang="scss" scoped>
.openLive {
    padding-bottom: 40px;
    .liveCont{
    justify-content: flex-start;
    .items{
        margin-right: 20px;
        width: calc(25% - 15px); // 保持与课程卡片相同的宽度计算方式
    }
    .items:nth-child(4n){
        margin-right: 0; // 每行第4个元素移除右侧边距
    }
    }
}
</style>
