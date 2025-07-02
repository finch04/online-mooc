import { createApp } from 'vue'
// 组件库
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'swiper/scss';
import 'swiper/scss/pagination';
import App from './App.vue'
import '@/style/font_style/iconfont.css';
import '@/style/font_icon/iconfont.css';
import { VueClipboard } from '@soerenmartius/vue3-clipboard';
import hljs from 'highlight.js';
import 'highlight.js/styles/atom-one-dark.css' //样式
// 全局样式
import '@/style/index.scss';
// 权限-路由拦截
import './permission';
// 数据存储、路由
import { store } from './store';
import router from './router';

const app = createApp(App);
//创建v-highlight全局指令
app.directive('highlight',function (el) {
    let blocks = el.querySelectorAll('pre code');
    blocks.forEach((block)=>{
      hljs.highlightBlock(block)
    })
  })
app.use(ElementPlus);
app.use(store);
app.use(router);
app.use(VueClipboard);
app.mount('#app')
