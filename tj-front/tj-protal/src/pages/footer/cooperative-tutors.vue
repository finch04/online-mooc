<template>
    <PageWrapper pageTitle="合作导师">
      <div class="about-content">
        <div class="about-hero">
          <div class="hero-text">
            <h1 class="animate-fade-in">合作导师</h1>
            <p class="animate-slide-up">我们汇聚了一批优秀的合作导师，为学员提供专业的指导和支持。</p>
          </div>
        </div>
        <div class="about-mission section-padding">
          <h2 class="section-title">导师团队</h2>
          <div class="mission-cards">
            <div class="mission-card animate-on-scroll" v-for="(tutor, index) in tutors" :key="index">
              <div class="card-icon">
                <img :src="tutor.image" :alt="tutor.name">
              </div>
              <h3>{{ tutor.name }}</h3>
              <p class="tutor-subject">{{ tutor.subject }}</p>
              <p class="tutor-description">{{ tutor.description }}</p>
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
  
  const tutors = [
    {
      name: '赵老师',
      subject: '计算机科学',
      image: '/src/assets/team-member-1.jpg',
      description: '拥有多年计算机教学经验，擅长编程语言和算法设计，指导学员在各类编程竞赛中取得优异成绩。'
    },
    {
      name: '钱老师',
      subject: '经济学',
      image: '/src/assets/team-member-2.jpg',
      description: '经济学领域的专家，对宏观经济和微观经济有深入研究，通过案例分析和实践教学，帮助学员掌握经济学原理。'
    },
    {
      name: '孙老师',
      subject: '艺术设计',
      image: '/src/assets/team-member-3.jpg',
      description: '资深艺术设计师，具有丰富的设计实践经验，注重培养学员的创新思维和审美能力。'
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
            width: 180px;
            height: 180px;
            border-radius: 50%;
            overflow: hidden;
            margin: 0 auto 20px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  
            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
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