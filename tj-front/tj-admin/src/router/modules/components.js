import Layout from "@/pages/layouts/index.vue"
import ClassIcon from "@/assets/icon_class.svg"
import HDIcon from "@/assets/icon_hd.svg"
import orderIcon from "@/assets/icon_order.svg"
import UserIcon from "@/assets/icon_user.svg"
import YXIcon from "@/assets/icon_yx.svg"
import OurseIcon from "@/assets/icon_course.svg"
export default [

  {
    path: "/curriculum",
    component: Layout,
    redirect: "/curriculum/index",
    name: "curriculum",
    meta: { title: "课程管理", icon: '&#xe60f;' },
    children: [
      {
        path: "index",
        name: "curriculum",
        component: () => import("@/pages/curriculum/course/index.vue"),
        meta: { title: "课程管理" },
      },
      {
        path: "add/:id",
        name: "curriculumAdd",
        component: () => import("@/pages/curriculum/course/add.vue"),
        meta: { title: "添加编辑", hidden: true, fmeta: {path: '/curriculum/index', title: '课程管理'} },
      },
      {
        path: "details/:id",
        name: "curriculumDetail",
        component: () => import("@/pages/curriculum/course/details.vue"),
        meta: { title: "课程详情", hidden: true, fmeta: {path: '/curriculum/index', title: '课程管理'} },
      },
      {
        path: "live",
        name: "live",
        component: () => import("@/pages/curriculum/live/index.vue"),
        meta: { title: "直播管理" },
      },
      {
        path: "type",
        name: "type",
        component: () => import("@/pages/curriculum/type/index.vue"),
        meta: { title: "课程分类" },
      },
      {
        path: "media",
        name: "media",
        component: () => import("@/pages/curriculum/media/index.vue"),
        meta: { title: "媒资管理" },
      },
      {
        path: "file",
        name: "file",
        component: () => import("@/pages/curriculum/file/index.vue"),
        meta: { title: "文件管理" },
      }
    ],
  },
  {
    path: "/exam",
    component: Layout,
    redirect: "/exam/index",
    name: "exam",
    meta: { title: "考试中心", icon: '&#xe60b;' },
    children: [
      {
        path: "index",
        name: "exam",
        component: () => import("@/pages/exam/exam/index.vue"),
        meta: { title: "考试管理" },
      },
      {
        path: "title",
        name: "title",
        component: () => import("@/pages/exam/title/index.vue"),
        meta: { title: "题目管理" },
      },
      {
        path: "mark",
        name: "mark",
        component: () => import("@/pages/exam/mark/index.vue"),
        meta: { title: "教师阅卷" },
      },
      {
        path: "exam/detail/:id",
        name: "examDetail",
        component: () => import("@/pages/exam/exam/detail.vue"),
        meta: { title: "考试详情", hidden: true ,fmeta: {path: '/exam/index', title: '考试管理'}},
      },
      {
        path: "title/add/:id",
        name: "titleAdd",
        component: () => import("@/pages/exam/title/add.vue"),
        meta: { title: "添加编辑", hidden: true ,fmeta: {path: '/exam/title/', title: '题目管理'}},
      },
      {
        path: "title/details/:id",
        name: "titleDetails",
        component: () => import("@/pages/exam/title/details.vue"),
        meta: { title: "题目编辑", hidden: true ,fmeta: {path: '/exam/title/', title: '题目管理'} },
      },
      {
        path: "title/detail/:id",
        name: "titleDetail",
        component: () => import("@/pages/exam/title/detail.vue"),
        meta: { title: "题目详情", hidden: true ,fmeta: {path: '/exam/title/', title: '题目管理'} },
      }
    ],
  },
  {
    path: "/marketing",
    component: Layout,
    redirect: "/marketing/index",
    name: "marketing",
    meta: { title: "营销中心", icon: '&#xe60d;' },
    children: [
      {
        path: "index",
        name: "marketing",
        component: () => import("@/pages/marketing/index.vue"),
        meta: { title: "优惠券管理" },
      },
      {
        path: "add/:id",
        name: "add",
        component: () => import("@/pages/marketing/add.vue"),
        meta: { title: "添加编辑", hidden: true ,fmeta: {path: '/marketing/index', title: '优惠券管理'} },
      },
      {
        path: "details/:id",
        name: "details",
        component: () => import("@/pages/marketing/details.vue"),
        meta: { title: "详情", hidden: true ,fmeta: {path: '/marketing/index', title: '优惠券管理'} },
      },
    ],
  },
  {
    path: "/message",
    component: Layout,
    redirect: "/message/index",
    name: "message",
    meta: { title: "消息中心", icon: '<svg t="1748945364119" class="icon" style="margin-bottom:17px" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4377" width="20" height="20"><path d="M297.432 333.888c-13.32-8.88-31.27-5.29-40.15 8.03-8.88 13.318-5.288 31.268 8.03 40.148l32.12-48.18z m129.904 121.412l16.06-24.09-16.06 24.09z m171.328 0l-16.06-24.09 16.06 24.09z m162.024-73.234c13.318-8.88 16.908-26.83 8.03-40.148-8.88-13.32-26.832-16.91-40.15-8.03l32.12 48.178z m-479.316-188.16h463.256V136H281.372v57.906z m588.72 125.466v386.046H928V319.372h-57.906z m-125.464 511.512H281.372v57.906h463.256v-57.906z m-588.72-125.466V319.372H98v386.046h57.906z m125.464 125.466c-69.296 0-125.466-56.17-125.466-125.466H98c0 101.26 82.112 183.372 183.372 183.372v-57.906z m588.72-125.466c0 69.296-56.168 125.466-125.464 125.466v57.906c101.26 0 183.372-82.112 183.372-183.372h-57.906zM744.63 193.906c69.296 0 125.466 56.17 125.466 125.466H928C928 218.112 845.888 136 744.628 136v57.906zM281.372 136C180.112 136 98 218.112 98 319.372h57.906c0-69.296 56.17-125.466 125.466-125.466V136z m-16.06 246.066l145.964 97.322 32.12-48.18-145.964-97.32-32.12 48.178z m349.412 97.322l145.964-97.322-32.12-48.18-145.964 97.324 32.12 48.18z m-203.448 0c61.614 41.076 141.834 41.076 203.448 0l-32.12-48.18a125.462 125.462 0 0 1-139.208 0l-32.12 48.18z" fill="#1E2124" p-id="4378"></path></svg>' },
    children: [
      {
        path: "index",
        name: "message",
        component: () => import("@/pages/message/index.vue"),
        meta: { title: "消息管理" },
      },
      {
        path: "notice",
        name: "notice",
        component: () => import("@/pages/message/notice/index.vue"),
        meta: { title: "公告管理" },
      },
      {
        path: "task",
        name: "task",
        component: () => import("@/pages/message/task/index.vue"),
        meta: { title: "公告任务管理" },
      },
      {
        path: "template",
        name: "template",
        component: () => import("@/pages/message/template/index.vue"),
        meta: { title: "模板管理" },
      },
      {
        path: "template/add/:id",
        name: "templateAdd",
        component: () => import("@/pages/message/template/add.vue"),
        meta: { title: "添加编辑", hidden: true ,fmeta: {path: '/message/template', title: '模板管理'}},
      },
      {
        path: "template/detail/:id",
        name: "templateDetail",
        component: () => import("@/pages/message/template/detail.vue"),
        meta: { title: "模板详情", hidden: true ,fmeta: {path: '/message/template', title: '模板管理'}},
      },
      {
        path: "template/config",
        name: "config",
        component: () => import("@/pages/message/template/config.vue"),
        meta: { title: "第三方短信平台管理",hidden:true ,fmeta: {path: '/message/template', title: '模板管理'} },
      },
      {
        path: "template/message",
        name: "SmsTemplate",
        component: () => import("@/pages/message/template/message.vue"),
        meta: { title: "短信模板管理",hidden:true ,fmeta: {path: '/message/template', title: '模板管理'} },
      },
    ],
  },
  {
    path: "/interactive",
    component: Layout,
    redirect: "/interactive/index",
    name: "interactive",
    meta: { title: "互动问答", icon: '&#xe60e;' },
    children: [
      {
        path: "index",
        name: "interactive",
        component: () => import("@/pages/interactive/index.vue"),
        meta: { title: "问答管理" },
      },
      {
        path: "details/:id",
        name: "answersDetails",
        component: () => import("@/pages/interactive/details.vue"),
        meta: { title: "问题详情", hidden: true ,fmeta: {path: '/interactive/index', title: '问答管理'} },
      },
      {
        path: "replies",
        name: "repliesDetails",
        component: () => import("@/pages/interactive/commentDetails.vue"),
        meta: { title: "回答详情", hidden: true ,fmeta: {path: '/interactive/index', title: '问答管理'} },
      },
      {
        path: "note",
        name: "note",
        component: () => import("@/pages/note/index.vue"),
        meta: { title: "笔记管理" },
      },
      {
        path: "noteDetails/:id",
        name: "noteDetails",
        component: () => import("@/pages/note/details.vue"),
        meta: { title: "笔记详情", hidden: true ,fmeta: {path: '/note/index', title: '笔记管理'} },
      },
    ],
  },
  {
    path: "/user",
    component: Layout,
    redirect: "/user/index",
    name: "user",
    meta: { title: "用户管理", icon: '&#xe610;' },
    children: [
      {
        path: "index",
        name: "student",
        component: () => import("@/pages/userlist/student/index.vue"),
        meta: { title: "学员管理" },
      },
      {
        path: "teacher",
        name: "teacher",
        component: () => import("@/pages/userlist/teacher/index.vue"),
        meta: { title: "教师管理" },
      },
      {
        path: "users",
        name: "users",
        component: () => import("@/pages/userlist/user/index.vue"),
        meta: { title: "后台用户管理" },
      },
    ],
  },
  {
    path: "/order",
    component: Layout,
    redirect: "/order/index",
    name: "order",
    meta: { title: "订单管理", icon: '&#xe611;' },
    children: [
      {
        path: "index",
        name: "order",
        component: () => import("@/pages/order/index.vue"),
        meta: { title: "订单管理" },
      },
      {
        path: "refund",
        name: "refund",
        component: () => import("@/pages/refund/index.vue"),
        meta: { title: "退款管理" },
      },
      {
        path: "reconciliation",
        name: "reconciliation",
        component: () => import("@/pages/reconciliation/index.vue"),
        meta: { title: "对账管理" },
      },
      {
        path: "channel",
        name: "channel",
        component: () => import("@/pages/reconciliation/channel.vue"),
        meta: { title: "支付渠道管理" ,hidden: true  },
      },
      {
        path: "details/:id",
        name: "orderDetails",
        component: () => import("@/pages/order/details.vue"),
        meta: { title: "订单详情", hidden: true ,fmeta: {path: '/order/index', title: '订单管理'} },
      },
      {
        path: "refundDetails/:id",
        name: "refundDetails",
        component: () => import("@/pages/refund/details.vue"),
        meta: { title: "退款详情", hidden: true ,fmeta: {path: '/order/refund', title: '退款管理'} },
      },
      {
        path: "reconciliationDetails/:id",
        name: "reconciliationDetails",
        component: () => import("@/pages/reconciliation/details.vue"),
        meta: { title: "对账详情", hidden: true ,fmeta: {path: '/order/reconciliation', title: '订单管理'} },
      },
    ],
  },
  {
    path: "/my",
    component: Layout,
    redirect: "/my/index",
    name: "my",
    meta: { title: "个人中心", icon: '&#xe611;', hidden: true  },
    children: [
      {
        path: "index",
        name: "my",
        component: () => import("@/pages/my/index.vue"),
        meta: { title: "个人中心", hidden: true  },
      }
    ],
  },
  {
    path: "/result",
    name: "result",
    component: Layout,
    redirect: "/result/success",
    meta: { title: "结果页", icon: orderIcon, hidden: true },
    children: [
      // {
      //   path: 'success',
      //   name: 'ResultSuccess',
      //   component: () => import('@/pages/result/success/index.vue'),
      //   meta: { title: '成功页' },
      // },
      // {
      //   path: 'fail',
      //   name: 'ResultFail',
      //   component: () => import('@/pages/result/fail/index.vue'),
      //   meta: { title: '失败页' },
      // },
      // {
      //   path: 'network-error',
      //   name: 'ResultNetworkError',
      //   component: () => import('@/pages/result/network-error/index.vue'),
      //   meta: { title: '网络异常' },
      // },
      // {
      //   path: '403',
      //   name: 'Result403',
      //   component: () => import('@/pages/result/403/index.vue'),
      //   meta: { title: '无权限' },
      // },
      {
        path: "404",
        name: "Result404",
        component: () => import("@/pages/result/404/index.vue"),
        meta: { title: "访问页面不存在页" },
      },
      // {
      //   path: '500',
      //   name: 'Result500',
      //   component: () => import('@/pages/result/500/index.vue'),
      //   meta: { title: '服务器出错页' },
      // },
      // {
      //   path: 'browser-incompatible',
      //   name: 'ResultBrowserIncompatible',
      //   component: () => import('@/pages/result/browser-incompatible/index.vue'),
      //   meta: { title: '浏览器不兼容页' },
      // },
      // {
      //   path: 'maintenance',
      //   name: 'ResultMaintenance',
      //   component: () => import('@/pages/result/maintenance/index.vue'),
      //   meta: { title: '系统维护页' },
      // },
    ],
  },
]
