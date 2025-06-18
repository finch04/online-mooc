<template>
    <PageWrapper pageTitle="客户服务">
      <div class="customer-service-content">
        <h2 class="section-title">客户服务</h2>
        <div class="service-item">
          <div class="service-header">
            <i class="fa fa-phone"></i>
            <h3>客服热线</h3>
          </div>
          <p>如果你有任何问题，可以拨打我们的客服热线：<a href="tel:123456789">123-456-789</a>，我们的客服人员将竭诚为你服务。</p>
        </div>
        <div class="service-item">
          <div class="service-header">
            <i class="fa fa-envelope"></i>
            <h3>邮箱咨询</h3>
          </div>
          <p>你也可以通过发送邮件至 <a href="mailto:1418875140@qq.com">1418875140@qq.com</a> 进行咨询，我们会在 24 小时内回复你。</p>
        </div>
        <div class="service-item">
          <div class="service-header">
            <i class="fa fa-users"></i>
            <h3>投诉建议</h3>
          </div>
          <p>如果你对我们的服务有任何投诉或建议，请填写 <a href="https://sv8mbh28596.feishu.cn/share/base/form/shrcngaMz9j2FcjN2ucY2E3bith">产品调研表单</a>，我们会认真对待并及时处理。</p>
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
  
  .customer-service-content {
    width: 100%;
    padding: 80px 10%;

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

    .service-item {
      display: flex;
      align-items: flex-start;
      gap: 20px;
      margin-bottom: 40px;

      .service-header {
        display: flex;
        align-items: center;
        gap: 10px;

        i {
          font-size: 24px;
          color: #1E88E5;
          width: 24px;
          height: 24px;
        }

        h3 {
          font-size: 20px;
          margin: 0;
        }
      }

      p {
        color: #757575;
      }
    }
  }
  </style>