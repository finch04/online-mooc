<template>
    <PageWrapper pageTitle="合作机构">
      <div class="about-content">
        <div class="about-hero">
          <div class="hero-text">
            <h1 class="animate-fade-in">合作机构</h1>
            <p class="animate-slide-up">我们与众多顶尖高校和机构合作，共同推动优质教育资源的共享。</p>
          </div>
        </div>
        <div class="about-mission section-padding">
          <h2 class="section-title">合作伙伴</h2>
          <div class="mission-cards">
            <div class="mission-card animate-on-scroll" v-for="(institution, index) in institutions" :key="index">
              <div class="card-icon">
                <img :src="institution.logo" :alt="institution.name">
              </div>
              <h3>{{ institution.name }}</h3>
              <p class="institution-description">{{ institution.description }}</p>
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
  
  const institutions = [
    {
      name: '天津中德应用技术大学',
      logo: 'https://www.tsguas.edu.cn/images/common/f_logo.png',
      description: '国内顶尖高校，在多个学科领域具有卓越的教学和科研实力，与我们合作提供了丰富的优质课程。'
    },
    {
      name: '开源鸿蒙社',
      logo: '/src/assets/osc.jpg',
      description: '开源鸿蒙社致力于推广和发展开源技术，为感兴趣的学生提供一个交流和合作的平台。作为一个开源社区，鼓励成员贡献自己的代码、文档和经验，同时也提供培训，帮助大家更好地掌握开源技术。'
    },
    {
      name: 'leyen',
      logo: 'https://pic.wjcly.com/my-pic/main/img/10064a3952ffcc7a2824c396240c2084.40122873.webp',
      description: '在科研创新方面具有领先地位，与我们合作开展了一系列前沿课程的研发和推广。'
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
  
  .about-content {
    width: 100%;
  
    .about-hero {
      height: 400px;
      background: linear-gradient(rgba(0, 0, 0, 0.6), rgba(0, 0, 0, 0.4)), url('@/assets/hero-bg.jpg');
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
          box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
          text-align: center;
          transition: transform 0.3s ease;
  
          .card-icon {
            width: 150px;
            height: 150px;
            margin: 0 auto 20px;
  
            img {
              width: 100%;
              height: auto;
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