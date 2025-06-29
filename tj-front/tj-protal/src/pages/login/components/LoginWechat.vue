<template>
    <div class="loginWechat">
        <!-- <div class="title">微信扫码登录</div> -->
        <div id="wxLogin" ref="wxLoginContainer" class="wx-login-container"></div>
        <div v-if="loginStatus === 'pending'" class="status-message">
            等待扫码...
        </div>
        <div v-if="loginStatus === 'scanned'" class="status-message">
            已扫码，请在手机上确认登录
        </div>
        <div v-if="loginStatus === 'success'" class="status-message success">
            登录成功，正在跳转...
        </div>
        <div v-if="loginStatus === 'error'" class="status-message error">
            登录失败，请重试
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { saveWxUuid, checkWxLoginStatus } from '@/api/user'; // 引入新API

const router = useRouter();
const wxLoginContainer = ref(null);
const loginStatus = ref('pending');
let loginTimer = null;

// 微信登录配置（需手动填写）
const WX_CONFIG = {
    appid: 'wx17655f8047b85150', // TODO: 填入你的微信开放平台APPID
    redirectUri: 'http://tjxt-user-t.itheima.net/xuecheng/auth/wxLogin', // TODO: 填入你的回调URL
};

// 生成唯一标识
const generateState = () => {
    return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
};

// 加载微信JS SDK
const loadWxLoginScript = () => {
    return new Promise((resolve, reject) => {
        if (window.WxLogin) {
            resolve();
            return;
        }

        const script = document.createElement('script');
        script.src = 'https://res.wx.qq.com/connect/zh_CN/htmledition/js/wxLogin.js';
        script.onload = resolve;
        script.onerror = reject;
        document.body.appendChild(script);
    });
};

// 初始化微信登录组件
const initWxLogin = async () => {
    try {
        await loadWxLoginScript();
        await nextTick(); // 等待DOM渲染完成

        // 生成唯一标识
        const state = generateState();

        // 将UUID存入后端Redis（使用新API）
        await saveWxUuid(state);

        // 创建微信登录对象
        new window.WxLogin({
            id: 'wxLogin',
            appid: WX_CONFIG.appid,
            scope: 'snsapi_login',
            redirect_uri: encodeURIComponent(WX_CONFIG.redirectUri),
            state,
            style: 'black', // 黑色风格
            href: '' // 自定义样式链接
        });

        // 开始轮询检查登录状态（使用新API）
        startLoginCheck(state);
    } catch (error) {
        console.error('初始化微信登录失败', error);
        ElMessage.error('初始化微信登录失败，请刷新页面');
        loginStatus.value = 'error';
    }
};

// 开始轮询检查登录状态
const startLoginCheck = (state) => {
    loginTimer = setInterval(async () => {
        try {
            // 调用新API检查登录状态
            const res = await checkWxLoginStatus(state);
            if (res.code === 200) {
                const status = res.data.status;
                if (status === 'pending') {
                    loginStatus.value = 'pending';
                } else if (status === 'scanned') {
                    loginStatus.value = 'scanned';
                } else if (status === 'success') {
                    clearInterval(loginTimer);
                    loginStatus.value = 'success';

                    // 登录成功，获取token
                    const token = res.data.token;
                    // 存储token并跳转到首页
                    localStorage.setItem('token', token);

                    setTimeout(() => {
                        router.push('/');
                    }, 1000);
                }
            }
        } catch (error) {
            console.error('检查登录状态失败', error);
        }
    }, 3000); // 每3秒检查一次
};

onMounted(() => {
    initWxLogin();
});

onUnmounted(() => {
    if (loginTimer) {
        clearInterval(loginTimer);
    }
});
</script>

<style scoped>
.loginWechat {
    margin-top: 40px;
    text-align: center;
}

.wx-login-container {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
}

.status-message {
    margin-top: 10px;
    font-size: 14px;
}

.success {
    color: #67c23a;
}

.error {
    color: #f56c6c;
}
</style>