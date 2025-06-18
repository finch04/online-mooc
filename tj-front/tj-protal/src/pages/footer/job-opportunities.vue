<template>
  <PageWrapper pageTitle="工作机会">
    <div class="job-opportunities">
      <!-- 英雄区域 -->
      <header class="hero">
        <div class="hero-content">
          <h1 class="animate-fade-in">工作机会</h1>
          <p class="animate-slide-up">加入我们的团队，一起为普及优质教育贡献力量！</p>
        </div>
      </header>
      
      <!-- 职位列表区域 -->
      <main class="job-list section-padding">
        <h2 class="section-title">当前职位</h2>
        <div class="job-cards">
          <div class="job-card animate-on-scroll" v-for="(job, index) in jobs" :key="index">
            <div class="card-header">
              <div class="job-icon">
                <i class="fa fa-briefcase"></i>
              </div>
              <h3>{{ job.title }}</h3>
            </div>
            <div class="card-body">
              <p class="job-location"><i class="fa fa-map-marker"></i> {{ job.location }}</p>
              <p class="job-description">{{ job.description }}</p>
            </div>
            <div class="card-footer">
              <a href="#" class="apply-button">申请职位</a>
            </div>
          </div>
        </div>
      </main>
    </div>
  </PageWrapper>
  <link href="https://cdn.jsdelivr.net/npm/font-awesome@4.7.0/css/font-awesome.min.css" rel="stylesheet">
</template>

<script setup>
import PageWrapper from './components/PageWrapper.vue'
import { ref, onMounted } from 'vue'

const jobs = [
  {
    title: '前端开发工程师',
    location: '北京',
    description: '负责智慧 MOOC 平台前端页面的开发和优化，与设计团队和后端团队紧密合作，打造良好的用户体验。'
  },
  {
    title: '课程运营专员',
    location: '上海',
    description: '负责课程的推广、运营和管理，与高校和机构沟通合作，确保课程的顺利上线和更新。'
  },
  {
    title: '市场营销专员',
    location: '广州',
    description: '制定和执行市场营销策略，提升智慧 MOOC 平台的品牌知名度和用户数量，负责线上线下的营销活动。'
  }
]

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

.job-opportunities {
  width: 100%;
  position: relative;
  
  /* 英雄区域样式 */
  .hero {
    height: 400px;
    background: linear-gradient(rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0.5)), url('@/assets/hero-bg.jpg');
    background-size: cover;
    background-position: center;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 60px;
    position: relative;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: linear-gradient(to bottom, transparent, rgba(0,0,0,0.7));
    }
    
    .hero-content {
      text-align: center;
      color: white;
      z-index: 1;
      padding: 0 20px;
      
      h1 {
        font-size: 3.5rem;
        margin-bottom: 1rem;
        font-weight: 700;
        text-shadow: 0 2px 4px rgba(0,0,0,0.5);
      }
      
      p {
        font-size: 1.25rem;
        max-width: 800px;
        margin: 0 auto;
        line-height: 1.6;
      }
    }
  }
  
  /* 职位列表样式 */
  .section-padding {
    padding: 80px 0;
  }
  
  .section-title {
    text-align: center;
    font-size: 2.5rem;
    margin-bottom: 4rem;
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      bottom: -1.5rem;
      left: 50%;
      transform: translateX(-50%);
      width: 5rem;
      height: 0.2rem;
      background-color: #1E88E5;
      border-radius: 0.1rem;
    }
  }
  
  .job-cards {
    display: flex;
    justify-content: center;
    flex-wrap: wrap;
    gap: 2.5rem;
    max-width: 1200px;
    margin: 0 auto;
  }
  
  .job-card {
    flex: 1 1 300px;
    background-color: white;
    border-radius: 0.8rem;
    box-shadow: 0 10px 25px rgba(0,0,0,0.05);
    overflow: hidden;
    transition: all 0.3s ease;
    position: relative;
    
    &:hover {
      transform: translateY(-10px);
      box-shadow: 0 15px 30px rgba(0,0,0,0.1);
    }
    
    .card-header {
      padding: 2rem;
      background-color: #f8f9fa;
      display: flex;
      align-items: center;
      gap: 1.5rem;
      
      .job-icon {
        width: 3.5rem;
        height: 3.5rem;
        background-color: #E3F2FD;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        
        i {
          font-size: 1.5rem;
          color: #1E88E5;
        }
      }
      
      h3 {
        font-size: 1.5rem;
        margin: 0;
        flex-grow: 1;
      }
    }
    
    .card-body {
      padding: 2rem;
      
      .job-location {
        color: #6c757d;
        margin-bottom: 1rem;
        display: flex;
        align-items: center;
        gap: 0.5rem;
      }
      
      .job-description {
        color: #495057;
        line-height: 1.6;
        margin-bottom: 1.5rem;
      }
    }
    
    .card-footer {
      padding: 1.5rem 2rem;
      background-color: #f8f9fa;
      text-align: center;
    }
    
    .apply-button {
      display: inline-block;
      background-color: #1E88E5;
      color: white;
      padding: 0.75rem 1.5rem;
      border-radius: 0.375rem;
      text-decoration: none;
      font-weight: 500;
      transition: background-color 0.3s ease;
      border: none;
      cursor: pointer;
      
      &:hover {
        background-color: #1565C0;
        transform: translateY(-2px);
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero {
    height: 300px;
  }
  
  .hero-content h1 {
    font-size: 2.5rem;
  }
  
  .section-title {
    font-size: 2rem;
    margin-bottom: 3rem;
  }
  
  .job-cards {
    gap: 1.5rem;
  }
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
</style>