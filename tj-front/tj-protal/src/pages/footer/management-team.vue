<template>
  <PageWrapper pageTitle="管理团队">
    <div class="management-team">
      <!-- 英雄区域 -->
      <header class="hero">
        <div class="hero-content">
          <h1 class="animate-fade-in">核心管理团队</h1>
          <p class="animate-slide-up">汇聚教育、技术与运营领域专家，引领智慧MOOC平台持续创新</p>
        </div>
      </header>
      
      <!-- 团队简介 -->
      <section class="team-intro section-padding">
        <div class="intro-content">
          <p>智慧MOOC管理团队由教育行业资深专家、技术精英和运营骨干组成，平均拥有15年以上行业经验，曾任职于985高校、头部互联网企业和知名教育机构，致力于推动优质教育资源的数字化与普及化。</p>
        </div>
      </section>
      
      <!-- 团队成员区域 -->
      <main class="team-members section-padding">
        <h2 class="section-title">核心管理人员</h2>
        <div class="member-grid">
          <div class="member-card animate-on-scroll" v-for="(member, index) in managementMembers" :key="index">
            <div class="member-avatar">
              <img :src="member.image" :alt="member.name" class="avatar-img">
              <div class="avatar-overlay">
                <div class="member-title">{{ member.position }}</div>
              </div>
            </div>
            <div class="member-details">
              <h3>{{ member.name }}</h3>
              <div class="member-badges">
                <span v-for="tag in member.tags" :key="tag" class="badge">{{ tag }}</span>
              </div>
              <p class="member-bio">{{ member.bio }}</p>
              <div class="member-experience">
                <h4>专业经历</h4>
                <ul>
                  <li v-for="(exp, idx) in member.experience" :key="idx">{{ exp }}</li>
                </ul>
              </div>
              <div class="social-links">
                <a href="#" target="_blank" class="social-link" aria-label="LinkedIn">
                  <i class="fa fa-linkedin"></i>
                </a>
                <a href="#" target="_blank" class="social-link" aria-label="微博">
                  <i class="fa fa-weibo"></i>
                </a>
                <a href="#" target="_blank" class="social-link" aria-label="邮箱">
                  <i class="fa fa-envelope"></i>
                </a>
              </div>
            </div>
          </div>
        </div>
      </main>
      
      <!-- 团队荣誉 -->
  <section class="team-awards section-padding bg-light">
    <h2 class="section-title">团队荣誉</h2>
    <div class="award-grid">
      <div class="award-card animate-on-scroll" v-for="(award, index) in awards" :key="index">
        <div class="award-icon">
          <i class="fa fa-trophy"></i> <!-- 奖杯图标已确认完整 -->
        </div>
        <h3>{{ award.title }}</h3>
        <p>{{ award.description }}</p>
        <div class="award-year">{{ award.year }}</div>
      </div>
    </div>
  </section>
    </div>
  </PageWrapper>
  <link href="https://cdn.jsdelivr.net/npm/font-awesome@4.7.0/css/font-awesome.min.css" rel="stylesheet">
</template>

<script setup>
import PageWrapper from './components/PageWrapper.vue'
import { ref, onMounted, reactive } from 'vue'

const managementMembers = reactive([
  {
    name: '张教授',
    position: '创始人兼CEO',
    image: '/src/assets/team-member-1.jpg',
    tags: ['教育专家', '课程设计', '战略规划'],
    bio: '北京师范大学教育学博士，曾任某985高校教务处处长，拥有20年高等教育管理经验，主导完成多项国家级教育改革项目。',
    experience: [
      '2010-2018 某985高校 教务处处长',
      '2018-2021 教育部在线教育研究中心 研究员',
      '2021至今 智慧MOOC 创始人兼CEO'
    ]
  },
  {
    name: '李博士',
    position: '技术总监',
    image: '/src/assets/team-member-2.jpg',
    tags: ['技术架构', '大数据', '云计算'],
    bio: '清华大学计算机科学与技术博士，前阿里巴巴资深技术专家，主导开发过亿级用户规模的在线教育平台架构。',
    experience: [
      '2012-2016 百度 高级工程师',
      '2016-2020 阿里巴巴 资深技术专家',
      '2020至今 智慧MOOC 技术总监'
    ]
  },
  {
    name: '王老师',
    position: '课程总监',
    image: '/src/assets/team-member-3.jpg',
    tags: ['课程开发', '教学研究', '学习体验'],
    bio: '华东师范大学课程与教学论硕士，前网易云课堂课程负责人，累计开发上线500+门精品在线课程，学员满意度超95%。',
    experience: [
      '2014-2017 某教育出版社 编辑',
      '2017-2021 网易云课堂 课程负责人',
      '2021至今 智慧MOOC 课程总监'
    ]
  },
  {
    name: '刘经理',
    position: '运营总监',
    image: '/src/assets/team-member-1.jpg', // 复用现有图片
    tags: ['用户运营', '活动策划', '增长黑客'],
    bio: '中国传媒大学市场营销学士，前腾讯教育运营负责人，擅长在线教育用户增长与运营体系搭建，曾实现单月用户增长300%。',
    experience: [
      '2015-2018 新浪教育 运营主管',
      '2018-2021 腾讯教育 运营负责人',
      '2021至今 智慧MOOC 运营总监'
    ]
  }
])

const awards = reactive([
  {
    title: '年度在线教育创新团队',
    description: '由教育部在线教育研究中心颁发，表彰在教育技术创新领域的突出贡献',
    year: '2023'
  },
  {
    title: '最佳教育科技团队',
    description: '中国教育装备行业协会评选，认可团队在教育数字化转型中的卓越表现',
    year: '2022'
  },
  {
    title: '用户满意度金奖',
    description: '由中国消费者协会颁发，用户满意度调查中教育平台类排名第一',
    year: '2021'
  }
])

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
  }, { threshold: 0.15 })

  animateElements.value.forEach(el => {
    observer.observe(el)
  })
})
</script>

<style lang="scss" scoped>
/* 全局样式变量 */
:root {
  --primary-color: #1E88E5;
  --primary-light: #E3F2FD;
  --primary-dark: #1565C0;
  --secondary-color: #43A047;
  --text-primary: #333;
  --text-secondary: #666;
  --text-tertiary: #999;
  --bg-light: #F5F7FA;
  --card-shadow: 0 15px 30px rgba(0,0,0,0.08);
  --card-shadow-hover: 0 20px 40px rgba(0,0,0,0.12);
  --transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.management-team {
  width: 100%;
  position: relative;
  
  /* 英雄区域 */
  .hero {
    height: 350px;
    background: linear-gradient(to bottom, rgba(0,0,0,0.85), rgba(0,0,0,0.7)), url('@/assets/hero-bg.jpg');
    background-size: cover;
    background-position: center;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 0;
    position: relative;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: linear-gradient(to bottom, transparent 30%, rgba(0,0,0,0.8));
    }
    
    .hero-content {
      text-align: center;
      color: white;
      z-index: 1;
      padding: 0 20px;
      max-width: 1000px;
      
      h1 {
        font-size: 5rem;
        margin-bottom: 1.5rem;
        font-weight: 800;
        line-height: 1.1;
        text-shadow: 0 5px 15px rgba(0,0,0,0.7);
      }
      
      p {
        font-size: 1.3rem;
        max-width: 800px;
        margin: 0 auto;
        line-height: 1.8;
        letter-spacing: 1px;
      }
    }
  }
  
  /* 团队简介 */
  .team-intro {
    background: linear-gradient(to right, var(--primary-light), white);
    padding: 60px 0;
    
    .intro-content {
      max-width: 1000px;
      margin: 0 auto;
      padding: 0 20px;
      font-size: 1.1rem;
      line-height: 2;
      color: var(--text-secondary);
    }
  }
  
  /* 公共样式 */
  .section-padding {
    padding: 80px 0;
  }
  
  .section-title {
    text-align: center;
    font-size: 2.8rem;
    margin-bottom: 6rem;
    position: relative;
    color: var(--text-primary);
    
    &::after {
      content: '';
      position: absolute;
      bottom: -2.5rem;
      left: 50%;
      transform: translateX(-50%);
      width: 8rem;
      height: 0.4rem;
      background: linear-gradient(to right, transparent, var(--primary-color), transparent);
      border-radius: 0.4rem;
    }
  }
  
  .bg-light {
    background-color: var(--bg-light);
  }
  
  /* 团队成员网格 */
  .member-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 3.5rem;
    max-width: 1300px;
    margin: 0 auto;
    padding: 0 20px;
  }
  
  .member-card {
    background: white;
    border-radius: 1.2rem;
    overflow: hidden;
    box-shadow: var(--card-shadow);
    transition: var(--transition);
    position: relative;
    z-index: 1;
    opacity: 0;
    transform: translateY(30px);
    
    &.animate-visible {
      opacity: 1;
      transform: translateY(0);
    }
    
    &:before {
      content: '';
      position: absolute;
      top: -5px;
      left: -5px;
      right: -5px;
      bottom: -5px;
      background: linear-gradient(45deg, var(--primary-color), transparent);
      z-index: -1;
      opacity: 0;
      transition: var(--transition);
      filter: blur(10px);
    }
    
    &:hover {
      transform: translateY(-20px);
      box-shadow: var(--card-shadow-hover);
      
      &:before {
        opacity: 0.3;
      }
    }
    
    .member-avatar {
      position: relative;
      width: 100%;
      padding-top: 100%; /* 1:1 比例 */
      
      .avatar-img {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: var(--transition);
      }
      
      .avatar-overlay {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: linear-gradient(to bottom, transparent 40%, rgba(0,0,0,0.7));
        display: flex;
        align-items: flex-end;
        padding: 20px;
        opacity: 0;
        transition: var(--transition);
        
        .member-title {
          color: white;
          font-size: 1.2rem;
          font-weight: 600;
        }
      }
      
      &:hover .avatar-overlay {
        opacity: 1;
      }
    }
    
    .member-details {
      padding: 2.5rem;
      
      h3 {
        font-size: 1.6rem;
        margin-bottom: 1.5rem;
        color: var(--text-primary);
        position: relative;
        display: inline-block;
      }
      
      .member-badges {
        display: flex;
        flex-wrap: wrap;
        gap: 0.8rem;
        margin-bottom: 1.5rem;
      }
      
      .badge {
        background-color: var(--primary-light);
        color: var(--primary-color);
        padding: 0.3rem 0.8rem;
        border-radius: 20px;
        font-size: 0.8rem;
        font-weight: 500;
      }
      
      .member-bio {
        color: var(--text-secondary);
        line-height: 1.8;
        margin-bottom: 1.8rem;
        font-size: 0.95rem;
      }
      
      .member-experience {
        margin-bottom: 2rem;
        
        h4 {
          color: var(--text-primary);
          font-size: 1.1rem;
          margin-bottom: 1rem;
          position: relative;
          padding-left: 1.5rem;
          
          &::before {
            content: '';
            position: absolute;
            left: 0;
            top: 50%;
            transform: translateY(-50%);
            width: 0.8rem;
            height: 0.8rem;
            background-color: var(--primary-color);
            border-radius: 50%;
          }
        }
        
        ul {
          list-style: none;
          padding-left: 0;
          
          li {
            color: var(--text-secondary);
            line-height: 1.7;
            font-size: 0.9rem;
            position: relative;
            padding-left: 1.8rem;
            margin-bottom: 0.8rem;
            
            &::before {
              content: '•';
              position: absolute;
              left: 0;
              color: var(--primary-color);
              font-weight: bold;
            }
          }
        }
      }
      
      .social-links {
        display: flex;
        justify-content: center;
        gap: 1.2rem;
      }
      
      .social-link {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 3rem;
        height: 3rem;
        border-radius: 50%;
        background-color: white;
        color: var(--primary-color);
        border: 1px solid var(--primary-color);
        transition: var(--transition);
        box-shadow: 0 4px 10px rgba(0,0,0,0.05);
        
        &:hover {
          background-color: var(--primary-color);
          color: white;
          transform: translateY(-5px);
          box-shadow: 0 8px 20px rgba(0,0,0,0.1);
        }
        
        i {
          font-size: 1.3rem;
        }
      }
    }
  }
  
  /* 团队荣誉 */
  .award-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 2.5rem;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }
  
  .award-card {
    background: white;
    border-radius: 1rem;
    padding: 2.5rem;
    text-align: center;
    box-shadow: var(--card-shadow);
    transition: var(--transition);
    opacity: 0;
    transform: translateY(30px);
    
    &.animate-visible {
      opacity: 1;
      transform: translateY(0);
    }
    
    &:hover {
      transform: translateY(-10px);
      box-shadow: var(--card-shadow-hover);
    }
    
    .award-icon {
      width: 5rem;
      height: 5rem;
      background-color: var(--primary-light);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 1.5rem;
      color: var(--primary-color);
      font-size: 2rem;
    }
    
    h3 {
      font-size: 1.3rem;
      margin-bottom: 1rem;
      color: var(--text-primary);
    }
    
    p {
      color: var(--text-secondary);
      line-height: 1.7;
      margin-bottom: 1.5rem;
      font-size: 0.95rem;
    }
    
    .award-year {
      font-size: 1rem;
      color: var(--primary-color);
      font-weight: 600;
    }
  }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .hero {
    height: 450px;
  }
  
  .hero-content h1 {
    font-size: 3.5rem;
  }
  
  .hero-content p {
    font-size: 1.3rem;
  }
  
  .section-title {
    font-size: 2.2rem;
    margin-bottom: 4rem;
  }
}

@media (max-width: 768px) {
  .hero {
    height: 400px;
  }
  
  .hero-content h1 {
    font-size: 2.8rem;
  }
  
  .hero-content p {
    font-size: 1.2rem;
  }
  
  .section-padding {
    padding: 60px 0;
  }
  
  .member-grid, .award-grid {
    gap: 2rem;
  }
  
  .member-card, .award-card {
    padding: 2rem;
  }
}

@media (max-width: 576px) {
  .hero {
    height: 350px;
  }
  
  .hero-content h1 {
    font-size: 2.2rem;
  }
  
  .team-intro .intro-content,
  .member-bio,
  .member-experience li {
    font-size: 1rem;
  }
  
  .section-title {
    font-size: 1.8rem;
    margin-bottom: 3rem;
  }
}

/* 增强动画效果 */
@keyframes fadeInUp {
  from { 
    opacity: 0;
    transform: translateY(40px);
  }
  to { 
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in {
  animation: fadeInUp 1s ease-out forwards;
}

.animate-slide-up {
  animation: fadeInUp 1.2s ease-out 0.2s forwards;
}

.animate-on-scroll {
  opacity: 0;
  transform: translateY(40px);
  transition: opacity 0.8s ease, transform 0.8s ease;
}

.animate-visible {
  opacity: 1;
  transform: translateY(0);
}
</style>