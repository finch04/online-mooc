<template>
    <div class="kibana-dashboard-container">
        <!-- Kibana 嵌入容器 -->
        <div class="iframe-wrapper">
            <iframe
                ref="kibanaIframe"
                :src="kibanaUrl"
                frameborder="0"
                width="100%"
                :height="iframeHeight"
                allowfullscreen
            ></iframe>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

// 配置项（可根据实际情况调整）
const props = defineProps({
    kibanaUrl: {
        type: String,
        default: 'http://192.168.150.101:5601/app/dashboards#/view/611bfc10-3892-11f0-8d04-3d422be6eefb?embed=true&_g=(filters%3A!()%2CrefreshInterval%3A(pause%3A!t%2Cvalue%3A0)%2Ctime%3A(from%3Anow-15m%2Cto%3Anow))', // Kibana 仪表盘 URL
    },
    pageTitle: {
        type: String,
        default: '数据大屏',
    }
});

const iframeHeight = ref('80vh'); // iframe 高度，视窗高度的 80%
const iframeRef = ref(null);
const isFullScreen = ref(false);

// 刷新 iframe 方法
const refreshIframe = () => {
    if (iframeRef.value) {
        iframeRef.value.contentWindow?.location.reload();
    }
};

// 窗口Resize时自动调整iframe高度
window.addEventListener('resize', () => {
    iframeHeight.value = window.innerHeight * 0.8 + 'px';
});


</script>

<style scoped lang="scss">
.kibana-dashboard-container {
    padding: 20px;
    height: 100vh;
    box-sizing: border-box;

    .page-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;

        h1 {
            margin: 0;
            font-size: 24px;
            color: #333;
        }
    }

    .iframe-toolbar {
        display: flex;
        gap: 10px;
    }

    .iframe-wrapper {
        border: 1px solid #e5e7eb;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

        iframe {
            min-height: 700px; /* 最小高度，避免内容过少时显示异常 */
            transition: height 0.3s ease;
        }
    }

    .header,
    .sidebar {
        transition: all 0.3s ease;
    }

    .hidden {
        display: none;
    }
}
</style>