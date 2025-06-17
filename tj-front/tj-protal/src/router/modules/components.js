import Layout from '@/pages/layouts/index.vue';

export default [
  {
    path: '/ask',
    name: 'ask',
    component: Layout,
    redirect: '/ask/index',
    meta: { title: '发布、编辑问答'},
    children: [
      {
        path: 'index',
        name: 'ask',
        component: () => import('@/pages/ask/index.vue'),
        meta: { title: '发布问题' },
      }
    ],
  },
  {
    path: '/about-us',
    name: 'about',
    component: Layout,
    redirect: '/about-us/index',
    meta: { title: '关于我们'},
    children: [
      {
        path: 'index',
        name: 'AboutUs',
        component: () => import('@/pages/footer/about-us.vue'),
        meta: { title: '关于我们' },
      }
    ],
  },
  {
    path: '/management-team',
    name: 'ManagementTeam',
    component: Layout,
    redirect: '/management-team/index',
    meta: { title: '管理团队' },
    children: [
      {
        path: 'index',
        name: 'ManagementTeam',
        component: () => import('@/pages/footer/management-team.vue'),
        meta: { title: '管理团队' },
      }
    ]
  },
  {
    path: '/job-opportunities',
    name: 'JobOpportunities',
    component: Layout,
    redirect: '/job-opportunities/index',
    meta: { title: '工作机会' },
    children: [
      {
        path: 'index',
        name: 'JobOpportunities',
        component: () => import('@/pages/footer/job-opportunities.vue'),
        meta: { title: '工作机会' },
      }
    ]
  },
  {
    path: '/customer-service',
    name: 'CustomerService',
    component: Layout,
    redirect: '/customer-service/index',
    meta: { title: '客户服务' },
    children: [
      {
        path: 'index',
        name: 'CustomerService',
        component: () => import('@/pages/footer/customer-service.vue'),
        meta: { title: '客户服务' },
      }
    ]
  },
  {
    path: '/help-document',
    name: 'HelpDocument',
    component: Layout,
    redirect: '/help-document/index',
    meta: { title: '帮助文档' },
    children: [
      {
        path: 'index',
        name: 'HelpDocument',
        component: () => import('@/pages/footer/help-document.vue'),
        meta: { title: '帮助文档' },
      }
    ]
  },
  {
    path: '/how-to-register',
    name: 'HowToRegister',
    component: Layout,
    redirect: '/how-to-register/index',
    meta: { title: '如何注册' },
    children: [
      {
        path: 'index',
        name: 'HowToRegister',
        component: () => import('@/pages/footer/how-to-register.vue'),
        meta: { title: '如何注册' },
      }
    ]
  },
  {
    path: '/how-to-select-courses',
    name: 'HowToSelectCourses',
    component: Layout,
    redirect: '/how-to-select-courses/index',
    meta: { title: '如何选课' },
    children: [
      {
        path: 'index',
        name: 'HowToSelectCourses',
        component: () => import('@/pages/footer/how-to-select-courses.vue'),
        meta: { title: '如何选课' },
      }
    ]
  },
  {
    path: '/cooperative-institutions',
    name: 'CooperativeInstitutions',
    component: Layout,
    redirect: '/cooperative-institutions/index',
    meta: { title: '合作机构' },
    children: [
      {
        path: 'index',
        name: 'CooperativeInstitutions',
        component: () => import('@/pages/footer/cooperative-institutions.vue'),
        meta: { title: '合作机构' },
      }
    ]
  },
  {
    path: '/cooperative-tutors',
    name: 'CooperativeTutors',
    component: Layout,
    redirect: '/cooperative-tutors/index',
    meta: { title: '合作导师' },
    children: [
      {
        path: 'index',
        name: 'CooperativeTutors',
        component: () => import('@/pages/footer/cooperative-tutors.vue'),
        meta: { title: '合作导师' },
      }
    ]
  },
  {
    path: '/result',
    name: 'result',
    component: Layout,
    redirect: '/result/success',
    meta: { title: '结果页', icon: 'check-circle' },
    children: [
      {
        path: 'success',
        name: 'ResultSuccess',
        component: () => import('@/pages/result/success/index.vue'),
        meta: { title: '成功页' },
      },
      {
        path: '404',
        name: 'Result404',
        component: () => import('@/pages/result/404/index.vue'),
        meta: { title: '访问页面不存在页' },
      },
    ],
  },
];