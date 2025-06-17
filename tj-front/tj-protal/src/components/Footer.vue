<!-- 公用底部部组件 -->
<template>
  <footer>
    <div class="container">
      <div class="links">
          <span v-for="item in Links" :key="item.title" @click="goDetails(item.link)">{{item.title}}</span>
        </div>
        <div class="about fx-sb">
          <div class="info">
            <p>智慧MOOC致力于普及中国最好的教育它与中国一流大学和机构合作提供在线课程</p>
            <p>&#169 online-mooc.com 保留所有权利 -沪ICP备15025210号</p>
          </div>
          <div class="logo">
            <img src="@/assets/logo.png" alt="" srcset="">
          </div>
        </div>
    </div>
  </footer>
</template>
<script setup>
  import { useRouter } from 'vue-router'
  const router = useRouter()
  // 站内-友情链接
  const Links = [
  { title: '关于我们', link: '/about-us' },
  { title: '管理团队', link: '/management-team' },
  { title: '工作机会', link: '/job-opportunities' },
  { title: '客户服务', link: '/customer-service' },
  { title: '帮助文档', link: '/help-document' },
  { title: '如何注册', link: '/how-to-register' },
  { title: '如何选课', link: '/how-to-select-courses' },
  { title: '合作机构', link: '/cooperative-institutions' },
  { title: '合作导师', link: '/cooperative-tutors' }
]
  // 转入详情页
  const goDetails = (link) => {
    router.push(link)
  }
</script>
<style lang="scss" scoped>
footer {
  width: 100%;
  background-color: var(--color-white);
  border-top: 1px solid #EEEEEE;
  text-align: left;
  padding: 35px 0;
  font-size: 14px;
  .links {
    margin-bottom: 25px;
    cursor: pointer;
    span{
      margin-right: 30px;
    }
  }
  .about{
    line-height: 30px;
    color: var(--color-font2);
    img{
      width: 144px;
      height: 48px;
    }
  }
}
</style>
