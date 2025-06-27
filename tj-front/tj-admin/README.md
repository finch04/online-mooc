# 智慧MOOC

## 项目简介

基于SpringCloud Alibaba的智慧教育平台，整合课程交易、社交化学习、AI辅助及数据分析。支持高并发抢券、动态优惠、数据分析、异步公告推送、实时聊天、AI 问答及个人知识库图谱。

## 运行环境 - 初始开发环境及工具

- 项目开发环境: Mac + node: v17.8.0 + npm: 8.5.5 || pnpm: 6.32.8 

## 技术栈应用

- Vue3 + vite + Tdesign + pinia + vue-router

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
│   │   ├── images 图片资源
│   ├── api                                 - 接口
│   │   ├── user.js                         - 用户
│   │   ├── teacher.js                      - 教师管理
│   │   ├── curriculum.js                   - 课程管理
│   │   ├── title.js                        - 题目管理
│   │   ├── marketing.js                    - 优惠券
│   │   ├── media.js                        - 媒资
│   │   ├── order.js                        - 订单
│   │   ├── question.js                     - 互动问答
│   │   ├── refund.js                       - 退款审批
│   │   ├── question.js                     - 互动问答
│   ├── conponents                          - 公用组件
│   │   ├── AddButton                       - 添加按钮组件
│   │   ├── Delete                          - 删除组件
│   │   ├── Header                          - 头部组件
│   │   ├── ImageMagnify                    - 图片放大组件
│   │   └── ResetPwd                        - 重置密码组件
│   │   └── UploadImage                     - 上传图片组件
│   ├── pages                               页面展示目录
│   │   ├── main                            首页 
│   │   │   ├── index.vue                   - 工作台
│   │   │   └── index.scss                  - 工作台样式
│   │   │   └── components                  - 组件
│   │   ├── login                           登录页面
│   │   ├── userlist                        用户管理
│   │   │   ├── teacher                     - 教师管理
│   │   │   │   ├── index.vue               - 列表
│   │   │   │   └── index.scss              - 样式
│   │   │   │   └── components              - 组件
│   │   │   ├── student                     - 学员管理---小山
│   │   │   │   ├── index.vue               - 列表
│   │   │   │   └── index.scss              - 样式
│   │   │   │   └── components              - 组件
│   │   │   ├── user                        - 后台用户---小山
│   │   │   │   ├── index.vue               - 列表
│   │   │   │   └── index.scss              - 样式
│   │   │   │   └── components              - 组件
│   │   ├── curriculum                      课程管理
│   │   │   ├── course                      - 课程管理
│   │   │   │   ├── index.vue               - 列表
│   │   │   │   ├── add.vue                 - 添加、编辑
│   │   │   │   ├── details.vue             - 详情---小山
│   │   │   │   └── index.scss              - 样式
│   │   │   │   └── components              - 组件
│   │   │   │   │   └── Search              - 列表搜索
│   │   │   │   │   └── Steps               - 步骤条
│   │   │   │   │   └── TableList           - 目录列表
│   │   │   │   │   └── Title               - 公用标题
│   │   │   │   └── base                    - 基本信息
│   │   │   │   │   └── index               - 基本信息
│   │   │   │   └── catalogue               - 课程目录
│   │   │   │   │   └── index               - 课程目录
│   │   │   │   │   └── sort                - 章排序弹层
│   │   │   │   └── video                   - 课程视频
│   │   │   │   │   └── index               - 视频列表
│   │   │   │   │   └── addVideo            - 添加视频
│   │   │   │   └── topic                   - 课程题目
│   │   │   │   │   └── index               - 题目列表
│   │   │   │   │   └── SetTopic            - 添加题目
│   │   │   │   └── tearch                  - 课程老师
│   │   │   │   │   └── index               - 老师列表
│   │   │   │   │   └── SetTopic            - 添加老师
│   │   │   ├── type                        - 课程分类
│   │   │   │   ├── index.vue               - 列表
│   │   │   │   └── index.scss              - 样式
│   │   │   │   └── components              - 组件
│   │   │   ├── media                       - 媒资管理
│   │   │   │   ├── index.vue               - 列表
│   │   │   │   └── index.scss              - 样式
│   │   │   │   └── components              - 组件
│   │   ├── title                           题目管理
│   │   │   ├── index.vue                   - 列表
│   │   │   ├── add.vue                     - 添加、编辑
│   │   │   ├── details.vue                 - 详情---小山
│   │   │   └── index.scss                  - 样式
│   │   │   └── components                  - 组件
│   │   ├── marketing                       营销中心
│   │   │   ├── index.vue                   - 优惠券列表
│   │   │   ├── add.vue                     - 添加、编辑
│   │   │   ├── details.vue                 - 详情---小山
│   │   │   └── index.scss                  - 样式
│   │   │   └── components                  - 组件
│   │   ├── interactive                     互动问答
│   │   │   ├── index.vue                   - 问答管理列表
│   │   │   └── index.scss                  - 样式
│   │   │   └── components                  - 组件
│   │   ├── order                           订单管理
│   │   │   ├── index.vue                   - 管理列表
│   │   │   ├── details.vue                 - 详情---小山
│   │   │   └── index.scss                  - 样式
│   │   │   └── components                  - 组件
│   │   ├── note                            笔记
│   │   │   ├── index.vue                   - 管理列表
│   │   │   └── index.scss                  - 样式
│   │   │   └── components                  - 组件
│   │   ├── my                              个人中心---小山
│   │   │   ├── index.vue                   - 主页
│   │   │   └── index.scss                  - 样式
│   │   │   └── components                  - 组件
│   ├── router                              路由页面
│   │   ├── index.tsx                       - 轨迹管理首页
│   │   └── PrivateRoute.tsx 
│   ├── utils       封装工具目录
│   │   ├── commonData.js                   公用数据       
│   │   ├── request.js                      封装请求模块
│   │   └── validate.js                     表单校验  
│   ├── images                              README.md的图片资源
│   ├── .gitignore
│   ├── package.json
│   ├── tsconfig.json
│    └── README.md

```
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
## 参考文档（项目开发过程中用到的所有技术难点 涉及到的相关参考）
- vite配置: https://vitejs.dev/config/
## 包含的三方技术 简介（如果有的话需要写明）
