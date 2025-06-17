<!-- 我的优化券 - 使用说明 -->
<template>
  <div class="couponWrapperExplain">
    <div class="personalCards">
        <BreadCrumb></BreadCrumb>
        <div class="title">优惠券使用说明</div>
        <div class="cont">
          <p class="tit">1.怎样获取优惠券？</p>
          <p>智慧MOOC会不定期推出课程优惠活动，通过优惠活动可以领取优惠券。</p>
          <p class="tit">2.怎样使用优惠券？</p>
          <p>先领取一张优惠券，然后在对应的课程页面，点击”立即购买“，在支付页面中选择需要使用的优惠券。使用优惠券后，付款“价格”会出现对应的变化。</p>
          <p class="tit">3.如果优惠券面额大于课程的价格，可以使用吗？</p>
          <p>不可以，优惠券面额≤课程价格时才能使用，购买时最低需支付0.02元。</p>
          <p class="tit">4.每个人可以领取多少张优惠券？</p>
          <p>每个人可以领取多张优惠券，但同一门课程的相同的优惠券，一个用户只能领取一张。</p>
        </div>
    </div>
  </div>
</template>
<script setup>

/** 数据导入 **/
// 组件导入
import BreadCrumb from './components/BreadCrumb.vue'

</script>
<style lang="scss" src="./index.scss"> </style>
