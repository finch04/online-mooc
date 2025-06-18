<template>
    <PageWrapper pageTitle="如何选课">
      <div class="select-courses-content">
        <div class="select-courses-hero">
          <div class="hero-text">
            <h1 class="animate-fade-in">如何选课</h1>
            <p class="animate-slide-up">轻松挑选适合您的优质课程</p>
          </div>
        </div>
  
        <div class="select-courses-steps section-padding">
          <h2 class="section-title">选课步骤</h2>
          <div class="step-cards">
            <div class="step-card animate-on-scroll">
              <div class="card-icon">
                <i class="fa fa-search"></i>
              </div>
              <h3>第一步：浏览课程目录</h3>
              <p>登录智慧MOOC后，在首页或课程目录中浏览各类课程。</p>
            </div>
            <div class="step-card animate-on-scroll" style="animation-delay: 0.2s">
              <div class="card-icon">
                <i class="fa fa-info-circle"></i>
              </div>
              <h3>第二步：查看课程详情</h3>
              <p>点击感兴趣的课程，查看课程介绍、教学大纲、教师信息等详情。</p>
            </div>
            <div class="step-card animate-on-scroll" style="animation-delay: 0.4s">
              <div class="card-icon">
                <i class="fa fa-plus"></i>
              </div>
              <h3>第三步：加入课程</h3>
              <p>如果课程适合您，点击“加入课程”按钮，将课程添加到您的学习列表。</p>
            </div>
            <div class="step-card animate-on-scroll" style="animation-delay: 0.6s">
              <div class="card-icon">
                <i class="fa fa-play-circle"></i>
              </div>
              <h3>第四步：开始学习</h3>
              <p>成功加入课程后，您可以随时开始学习课程内容。</p>
            </div>
          </div>
        </div>
  
        <div class="select-courses-tips section-padding bg-light">
          <h2 class="section-title">选课小贴士</h2>
          <div class="tip-cards">
            <div class="tip-card animate-on-scroll">
              <div class="card-icon">
                <i class="fa fa-lightbulb-o"></i>
              </div>
              <h3>明确学习目标</h3>
              <p>在选课之前，先明确自己的学习目标和兴趣，以便选择合适的课程。</p>
            </div>
            <div class="tip-card animate-on-scroll" style="animation-delay: 0.2s">
              <div class="card-icon">
                <i class="fa fa-star"></i>
              </div>
              <h3>参考课程评价</h3>
              <p>查看其他学员的课程评价，了解课程的质量和难度。</p>
            </div>
            <div class="tip-card animate-on-scroll" style="animation-delay: 0.4s">
              <div class="card-icon">
                <i class="fa fa-clock-o"></i>
              </div>
              <h3>合理安排时间</h3>
              <p>根据自己的时间安排，选择适合自己学习进度的课程。</p>
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
  
  .select-courses-content {
    width: 100%;
  
    .select-courses-hero {
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
  
    .select-courses-steps {
      .step-cards {
        display: flex;
        justify-content: center;
        gap: 30px;
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
  
    .select-courses-tips {
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