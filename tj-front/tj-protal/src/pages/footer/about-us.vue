<template>
  <PageWrapper pageTitle="关于我们">
    <div class="about-content">
      <div class="about-hero">
        <div class="hero-text">
          <h1 class="animate-fade-in">关于我们</h1>
          <p class="animate-slide-up">智慧MOOC致力于普及中国最好的教育，提供高质量在线课程体验</p>
        </div>
      </div>
      
      <div class="about-mission section-padding">
        <h2 class="section-title">我们的使命</h2>
        <div class="mission-cards">
          <div class="mission-card animate-on-scroll">
            <div class="card-icon">
              <i class="fa fa-lightbulb-o"></i>
            </div>
            <h3>教育普及</h3>
            <p>打破地域限制，让优质教育资源惠及全国</p>
          </div>
          <div class="mission-card animate-on-scroll" style="animation-delay: 0.2s">
            <div class="card-icon">
              <i class="fa fa-university"></i>
            </div>
            <h3>名校合作</h3>
            <p>与顶尖高校和机构合作，提供高质量课程</p>
          </div>
          <div class="mission-card animate-on-scroll" style="animation-delay: 0.4s">
            <div class="card-icon">
              <i class="fa fa-users"></i>
            </div>
            <h3>学习社区</h3>
            <p>建立互动学习社区，促进知识分享与交流</p>
          </div>
        </div>
      </div>
      
      <div class="about-history section-padding bg-light">
        <h2 class="section-title">发展历程</h2>
        <div class="timeline">
          <div class="timeline-item animate-on-scroll">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <h3>2015年</h3>
              <p>智慧MOOC平台正式上线，首批合作高校达10所</p>
            </div>
          </div>
          <div class="timeline-item animate-on-scroll" style="animation-delay: 0.2s">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <h3>2017年</h3>
              <p>注册用户突破100万，课程数量超过500门</p>
            </div>
          </div>
          <div class="timeline-item animate-on-scroll" style="animation-delay: 0.4s">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <h3>2020年</h3>
              <p>推出移动学习应用，实现多平台无缝学习体验</p>
            </div>
          </div>
          <div class="timeline-item animate-on-scroll" style="animation-delay: 0.6s">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <h3>2023年</h3>
              <p>与国际顶尖学府达成合作，开启全球化教育战略</p>
            </div>
          </div>
        </div>
      </div>
      
      <div class="about-team section-padding">
        <h2 class="section-title">核心团队</h2>
        <div class="team-members">
          <div class="team-member animate-on-scroll">
            <div class="member-image">
              <img src="../../assets/team-member-1.jpg" alt="张教授 - CEO">
            </div>
            <h3>张教授</h3>
            <p>创始人兼CEO</p>
          </div>
          <div class="team-member animate-on-scroll" style="animation-delay: 0.2s">
            <div class="member-image">
              <img src="../../assets/team-member-2.jpg" alt="李博士 - 技术总监">
            </div>
            <h3>李博士</h3>
            <p>技术总监</p>
          </div>
          <div class="team-member animate-on-scroll" style="animation-delay: 0.4s">
            <div class="member-image">
              <img src="../../assets/team-member-3.jpg" alt="王老师 - 课程总监">
            </div>
            <h3>王老师</h3>
            <p>课程总监</p>
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

.about-content {
  width: 100%;
  
  .about-hero {
    height: 400px;
    background: linear-gradient(rgba(0,0,0,0.6), rgba(0,0,0,0.4)), url('@/assets/hero-bg.jpg');
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
  
  .about-mission {
    .mission-cards {
      display: flex;
      justify-content: center;
      gap: 40px;
      
      .mission-card {
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
  
  .about-history {
    .timeline {
      position: relative;
      
      &::before {
        content: '';
        position: absolute;
        top: 0;
        bottom: 0;
        left: 50%;
        width: 2px;
        background-color: #E0E0E0;
        transform: translateX(-50%);
      }
      
      .timeline-item {
        position: relative;
        margin-bottom: 60px;
        
        .timeline-dot {
          position: absolute;
          top: 0;
          left: 50%;
          width: 20px;
          height: 20px;
          background-color: #1E88E5;
          border-radius: 50%;
          transform: translateX(-50%);
          z-index: 1;
        }
        
        .timeline-content {
          width: 45%;
          padding: 20px;
          border-radius: 8px;
          background-color: white;
          box-shadow: 0 4px 15px rgba(0,0,0,0.08);
          
          h3 {
            font-size: 20px;
            margin-bottom: 10px;
            color: #1E88E5;
          }
        }
        
        &:nth-child(even) {
          .timeline-content {
            margin-left: auto;
          }
        }
      }
    }
  }
  
  .about-team {
    .team-members {
      display: flex;
      justify-content: center;
      gap: 40px;
      
      .team-member {
        text-align: center;
        
        .member-image {
          width: 180px;
          height: 180px;
          border-radius: 50%;
          overflow: hidden;
          margin: 0 auto 20px;
          box-shadow: 0 4px 15px rgba(0,0,0,0.1);
          
          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }
        
        h3 {
          font-size: 20px;
        }
        
        p {
          color: #757575;
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