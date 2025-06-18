<template>
    <PageWrapper pageTitle="帮助文档">
      <div class="help-content">
        <h2 class="section-title">常见问题解答</h2>
        <div class="faq-item">
          <div class="item-header">
            <i class="fa fa-question-circle"></i>
            <h3>如何注册账号？</h3>
          </div>
          <p>点击首页右上角的“注册”按钮，填写相关信息，如用户名、密码、邮箱等，然后点击“提交”即可完成注册。</p>
        </div>
        <div class="faq-item">
          <div class="item-header">
            <i class="fa fa-question-circle"></i>
            <h3>如何购买课程？</h3>
          </div>
          <p>在课程列表中选择你想要购买的课程，点击“立即购买”，选择支付方式完成支付即可。</p>
        </div>
        <div class="faq-item">
          <div class="item-header">
            <i class="fa fa-question-circle"></i>
            <h3>如何观看课程？</h3>
          </div>
          <p>登录账号后，在“我的课程”中找到你购买的课程，点击进入课程详情页，即可开始观看课程视频。</p>
        </div>

        <h2 class="section-title">使用指南</h2>
        <div class="guide-item">
          <div class="item-header">
            <i class="fa fa-book"></i>
            <h3>课程学习指南</h3>
          </div>
          <p>在课程学习过程中，你可以随时暂停、播放视频，还可以拖动进度条调整观看位置。同时，课程下方会有相关的学习资料供你下载。</p>
        </div>
        <div class="guide-item">
          <div class="item-header">
            <i class="fa fa-book"></i>
            <h3>社区交流指南</h3>
          </div>
          <p>在学习社区中，你可以发布学习心得、提问交流。请遵守社区规则，文明发言。</p>
        </div>
      </div>
    </PageWrapper>
    <link href="https://cdn.jsdelivr.net/npm/font-awesome@4.7.0/css/font-awesome.min.css" rel="stylesheet">
  </template>

  <script setup>
  import PageWrapper from './components/PageWrapper.vue'
  </script>

  <style lang="scss" scoped>
  @import "./index.scss";

  .help-content {
    width: 100%;
    padding: 80px 10%; // 减少左右内边距

    .section-title {
      text-align: center;
      font-size: 32px;
      margin-bottom: 60px;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        bottom: -15px;
        left: 50%;
        transform: translateX(-50%);
        width: 80px;
        height: 3px;
        background-color: #1E88E5;
      }
    }

    .faq-item,
    .guide-item {
      display: flex;
      flex-direction: column; // 调整为垂直布局
      gap: 10px;
      margin-bottom: 40px;

      .item-header {
        display: flex;
        align-items: center;
        gap: 10px;

        i {
          font-size: 24px;
          color: #1E88E5;
        }

        h3 {
          font-size: 20px;
          margin: 0;
        }
      }

      p {
        color: #757575;
        line-height: 1.6;
      }
    }
  }
  </style>