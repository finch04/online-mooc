<template>
    <PageWrapper pageTitle="如何注册">
      <div class="register-content">
        <div class="register-hero">
          <div class="hero-text">
            <h1 class="animate-fade-in">如何注册</h1>
            <p class="animate-slide-up">简单几步，开启您的智慧MOOC学习之旅</p>
          </div>
        </div>
  
        <div class="register-steps section-padding">
          <h2 class="section-title">注册步骤</h2>
          <div class="step-cards">
            <div class="step-card animate-on-scroll">
              <div class="card-icon">
                <i class="fa fa-user-plus"></i>
              </div>
              <h3>第一步：进入注册页面</h3>
              <p>在智慧MOOC首页，点击右上角的“注册”按钮，进入注册页面。</p>
            </div>
            <div class="step-card animate-on-scroll" style="animation-delay: 0.2s">
              <div class="card-icon">
                <i class="fa fa-envelope"></i>
              </div>
              <h3>第二步：填写信息</h3>
              <p>填写您的手机号、用户名和密码。</p>
            </div>
            <div class="step-card animate-on-scroll" style="animation-delay: 0.4s">
              <div class="card-icon">
                <i class="fa fa-check"></i>
              </div>
              <h3>第三步：验证手机号</h3>
              <p>点击发送到您邮箱的验证链接，完成手机号验证。</p>
            </div>
            <div class="step-card animate-on-scroll" style="animation-delay: 0.6s">
              <div class="card-icon">
                <i class="fa fa-unlock"></i>
              </div>
              <h3>第四步：完成注册</h3>
              <p>手机号验证成功后，您就可以使用注册的账号登录智慧MOOC了。</p>
            </div>
          </div>
        </div>
  
        <div class="register-tips section-padding bg-light">
          <h2 class="section-title">注册小贴士</h2>
          <div class="tip-cards">
            <div class="tip-card animate-on-scroll">
              <div class="card-icon">
                <i class="fa fa-lightbulb-o"></i>
              </div>
              <h3>使用常用手机号</h3>
              <p>请使用您常用的手机号进行注册，以便接收重要通知和课程信息。</p>
            </div>
            <div class="tip-card animate-on-scroll" style="animation-delay: 0.2s">
              <div class="card-icon">
                <i class="fa fa-lock"></i>
              </div>
              <h3>设置强密码</h3>
              <p>为了您的账号安全，请设置包含字母、数字和特殊字符的强密码。</p>
            </div>
            <div class="tip-card animate-on-scroll" style="animation-delay: 0.4s">
              <div class="card-icon">
                <i class="fa fa-info-circle"></i>
              </div>
              <h3>及时验证手机号</h3>
              <p>请在注册后尽快验证手机号，以免影响您的正常使用。</p>
            </div>
          </div>
        </div>
      </div>
    </PageWrapper>
    <link href="https://cdn.jsdelivr.net/npm/font-awesome@4.7.0/css/font-awesome.min.css" rel="stylesheet">
  </template>
  
  <script setup>
  import PageWrapper from './components/PageWrapper.vue'
  import { ref, onMounted } from 'vue'
  
  // 监听滚动事件，用于触发动画
  const animateElements = ref(null)
  
  onMounted(() => {
    animateElements.value = document.querySelectorAll('.animate-on-scroll')
  
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('animate-visible')
        }
      })
    }, { threshold: 0.1 })
  
    animateElements.value.forEach(el => {
      observer.observe(el)
    })
  })
  </script>
  
  <style lang="scss" scoped>
  @import "./index.scss";
  
  .register-content {
    width: 100%;
  
    .register-hero {
      height: 400px;
      background: linear-gradient(rgba(0,0,0,0.6), rgba(0,0,0,0.4)), url('@/assets/help-bg.jpg');
      background-size: cover;
      background-position: center;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 60px;
  
      .hero-text {
        text-align: center;
        color: white;
  
        h1 {
          font-size: 48px;
          margin-bottom: 20px;
          font-weight: 700;
        }
  
        p {
          font-size: 20px;
          max-width: 800px;
          margin: 0 auto;
        }
      }
    }
  
    .section-padding {
      padding: 80px 0;
    }
  
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
  
    .register-steps {
      .step-cards {
        display: flex;
        justify-content: center;
        gap: 40px;
        flex-wrap: wrap;
  
        .step-card {
          flex: 1;
          max-width: 300px;
          background-color: white;
          padding: 30px;
          border-radius: 10px;
          box-shadow: 0 4px 15px rgba(0,0,0,0.08);
          text-align: center;
          transition: transform 0.3s ease;
  
          .card-icon {
            width: 60px;
            height: 60px;
            background-color: #E3F2FD;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
  
            i {
              font-size: 24px;
              color: #1E88E5;
            }
          }
  
          h3 {
            font-size: 18px;
            margin-bottom: 15px;
          }
  
          &:hover {
            transform: translateY(-10px);
          }
        }
      }
    }
  
    .register-tips {
      .tip-cards {
        display: flex;
        justify-content: center;
        gap: 40px;
        flex-wrap: wrap;
  
        .tip-card {
          flex: 1;
          max-width: 300px;
          background-color: white;
          padding: 30px;
          border-radius: 10px;
          box-shadow: 0 4px 15px rgba(0,0,0,0.08);
          text-align: center;
          transition: transform 0.3s ease;
  
          .card-icon {
            width: 60px;
            height: 60px;
            background-color: #E3F2FD;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
  
            i {
              font-size: 24px;
              color: #1E88E5;
            }
          }
  
          h3 {
            font-size: 20px;
            margin-bottom: 15px;
          }
  
          &:hover {
            transform: translateY(-10px);
          }
        }
      }
    }
  
    .bg-light {
      background-color: #FAFAFA;
    }
  
    /* 动画效果 */
    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }
  
    @keyframes slideUp {
      from { transform: translateY(30px); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }
  
    .animate-fade-in {
      animation: fadeIn 1s ease forwards;
    }
  
    .animate-slide-up {
      animation: slideUp 1s ease forwards;
    }
  
    .animate-on-scroll {
      opacity: 0;
      transform: translateY(30px);
      transition: opacity 0.6s ease, transform 0.6s ease;
    }
  
    .animate-visible {
      opacity: 1;
      transform: translateY(0);
    }
  }
  </style>