# 智慧MOOC 

## 项目简介
基于SpringCloud Alibaba的智慧教育平台，整合课程交易、社交化学习、AI辅助及数据分析。支持高并发抢券、动态优惠、数据分析、异步公告推送、实时聊天、AI 问答及个人知识库图谱。

## 技术栈应用

- Vue3 + vite + Tdesign + pinia + vue-router
 
## 安装运行

``` bash
## 安装依赖 
npm install || yarn 

## 启动项目 

# 启动链接mock
npm run dev
# 启动链接测试环境
npm run start

## 构建正式环境 - 打包 
npm run build 

```

## 运行环境 - 初始开发环境及工具

- 项目开发环境: Mac + node: v17.8.0 + npm: 8.5.5 || pnpm: 6.32.8

## 项目结构
```html
java-eaglemap  
│
└───build - 打包目录
│   
└───public - 公共资源目录
│     
└───src - 源代码
│   ├── api 请求相关
│   ├── assets 公共资源
│   ├── conponents                          公用组件
│   │   ├── Result                          - 请求结果404、500 等
│   │   ├── AskChapterItems.vue             - 章节栏-问答模块、笔记
│   │   ├── Breadcrumb.vue                  - 公用面包屑-所有详情页面使用
│   │   ├── classCards.vue                  - 课程展示卡片 - 首页、搜索页
│   │   ├── ClassCatalogue.vue              - 课程目录模块
│   │   ├── MainTitle.vue                   - 标题组件
│   │   ├── TableSwitchBar.vue              - table切换头部
│   │   ├── Header.vue                      - 公用头部组件
│   │   └── Footer.vue                      - 公用底部组件
│   ├── pages                               页面展示目录
│   │   ├── adk 
│   │   │   ├── index.tsx                   - 电子围栏挂你首页 列表展示
│   │   │   └── details.tsx                 - 电子围栏挂详情  展示围栏地图及
│   │   ├── classDetails                           
│   │   ├── classList
│   │   │   ├── index.tsx                   - 服务端管理首页
│   │   │   └── details.tsx                 - 服务端管理详情页面 - 展示该服务下的终端列表
│   │   ├── classSearch                     - 系统配置页面
│   │   ├── layouts                         - 系统配置页面
│   │   ├── learning                        - 系统配置页面
│   │   ├── login                           - 系统配置页面
│   │   ├── main                            - 系统配置页面
│   │   ├── pay                             - 系统配置页面
│   │   ├── rsult                           - 系统配置页面
│   │   └── personal                        - 个人中心
│   │       ├── components                  - 组件
│   │       ├── style                       - 样式包 包含index.scss中的所引用的样式
│   │       ├── index.scss                  - 主样式文件
│   │       ├── myClass                     - 我的课程
│   │       ├── myCollect                   - 我的收藏
│   │       ├── myCcoupon                   - 我的优惠券
│   │       ├── myCcouponExplain            - 我的优惠券使用说明
│   │       ├── myExam                      - 我的考试
│   │       ├── myExamDetails               - 我的考试详情
│   │       ├── myIntegral                  - 我的积分
│   │       ├── myIntegralRanking           - 我的积分 - 天梯榜
│   │       ├── myMessage                   - 我的消息
│   │       ├── myOrder                     - 个人中心-我的订单
│   │       ├── myOrder Details             - 个人中心-我的订单详情
│   │       └── mySet.tsx                   - 我的设置
│   ├── router     路由页面
│   │   ├── modules                     - 路由配置
│   │   └── index.js                    - 路由配置
│   ├── store      pinia（类似vuex）状态管理数据处理
│   │   ├── modules                     - pinia配置
│   │   └── index.js                    - pinia配置
│   ├── style       公用样式
│   │   ├── element                     - element样式处理 
│   │   ├── font_style                  - icon字体
│   │   ├── theme                       - 主体样式定义
│   │   └── index.scss                  - 公用样式 
│   ├── utils       工具函数
│   │   ├── request.js                  - 请求封装
│   │   └── tool.js                     - 工具函数封装 
│   ├── .gitignore
│   ├── package.json
│   └── README.md

```

## 相关资料
Vue3: https://cn.vuejs.org/guide/introduction.html
## 参考文档（项目开发过程中用到的所有技术难点 涉及到的相关参考）
- vite配置: https://vitejs.dev/config/
## 包含的三方技术 简介（如果有的话需要写明）
- 腾讯视频播放器：https://cloud.tencent.com/document/product/266/58773
- https://tcplayer.vcube.tencent.com/
