<!-- 猜你喜欢模块 -->
<template>
    <div class="like bg-wt">
        <div class="title">猜你喜欢</div>
        <div class="likeCards">
            <div class="newClassCard marg-bt-20" style="width:100%" v-for="(item, index) in data"   :key="index" @click="goDetails(item.id)">
                <div class="image">
                    <img :src="item.coverUrl" alt="" />
                </div>
                <div class="pd-10">
                    <div class="title marg-bt-10 ft-14" v-html="item.name"></div>
                    <span>有{{item.sold}}人在学习</span>
                    <div class="ft-cl-des fx-sb">
                        <span>共{{item.sections || 0}}节 </span> 
                        <span v-if="Number(item.price) != 0" class="ft-16 ft-cl-err">￥{{(Number(item.price)/100).toFixed(2)}}</span> 
                        <span v-else class="ft-16 ft-cl-err">免费</span> 
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup>
import router from '../../../router';
import { useRoute } from 'vue-router';

const route = useRoute();

// 引入父级传参
const props =defineProps({
  data:{
    type: Object,
    default:{}
  }
})

const goDetails = id => {
  router.push({path: '/details', query:{id}})
}




</script>
<style lang="scss" scoped>
.like{
    padding: 30px;
    border-radius: 8px;
    .title{
        font-weight: 600;
        font-size: 20px;
        margin-bottom: 10px;
    }
    .likeCards{
        .title{
            font-size: 14px;
            font-weight: 400;
            margin-bottom: 20px;
        }
    }
}

.newClassCard{
    position: relative;
    width: 23%;
    background: #FFFFFF;
    border: 1px solid #EEEEEE;
    border-radius: 8px;
    font-size: 12px;
    line-height: 24px;
    cursor: pointer;
    &:hover{
      box-shadow: 0 4px 6px 2px rgba(108,112,118,0.17);
      
      top: -3px;
    }
    .title{
        line-height: 22px;
        :deep(em){
          font-style: normal;
          color:var(--color-main);
        }
    }
    .image{
        width: 100%;
        height: 160px;
        overflow: hidden;
        position: relative;
        img{
            width: 100%;
            border-radius: 8px 8px 0 0;
        }
    }
    em{
      position: relative;
      top: -3px;
    }
}
</style>