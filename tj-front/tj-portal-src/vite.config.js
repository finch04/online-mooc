// 项目配置页面
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import svgLoader from 'vite-svg-loader';
import vueJsx from '@vitejs/plugin-vue-jsx';
import path from 'path';

const CWD = process.cwd();

//配置参考 https://vitejs.dev/config/
export default defineConfig((mode) => {
  const define = {
		"process.env": process.env,
        global: {}
    };
  // const { VITE_BASE_URL } = loadEnv(mode, CWD);
  return {
    define,
    base: './',
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    css: {
      preprocessorOptions: {
        scss: {
          api: 'modern-compiler'
        }
      }},
    plugins: [
      vue(),
      vueJsx(),
      svgLoader()
    ],
    server: {
      port: 18082,
      host: '0.0.0.0',
      proxy: {
        '/api': {
          // target: 'https://apifoxmock.com/m1/5754259-5437482-default', // pip正式环境
          target:'http://api.tianji.com',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '')
        },
        '/img-tx': {
          // target:  'https://tjxt-dev.itheima.net/', // 'http://172.17.2.134',
          target:  'http://www.tianji.com/',
          changeOrigin: true,
          // rewrite: (path) => {
          //   return path.replace(/^\/img-tx/, '')
          // }
        },
        '/mock/3359':{
          target: 'http://172.17.0.137:8321/mock/3359',
          changeOrigin: true,
        }
      }
    },
  }
})
