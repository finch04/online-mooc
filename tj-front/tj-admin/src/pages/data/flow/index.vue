<template>
    <div class="contentBox">
        <!-- 搜索 -->
        <Search :searchForm="searchForm" @getTime="getTime" @handleSearch="handleSearch"></Search>
        <!-- end -->
        <div class="bg-wt radius marg-tp-20">
            <!-- 基础流量数据图表 -->
            <BaseMetric :metrics="baseMetrics" />
        </div>
        <div class="bg-wt radius marg-tp-20">
            <!-- URL访问量前10名图表 -->
            <UrlVisitsMetric :metrics="visitsMetrics" />
        </div>
        <div class="bg-wt radius marg-tp-20">
            <!-- URL报错量前10名图表 -->
            <UrlErrorsMetric :metrics="errorsMetrics" />
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue';
import { ElMessage } from 'element-plus';

// 导入组件
import Search from './components/Search.vue';
import BaseMetric from './components/BaseMetric.vue';
import UrlVisitsMetric from './components/UrlVisitsMetric.vue';
import UrlErrorsMetric from './components/UrlErrorsMetric.vue';

// 导入 API
import {
    getBaseFlow,
    getVisitsUrlFlow,
    getErrorsUrlFlow
} from '@/api/data';

// 搜索表单
const searchForm = reactive({
    beginTime: null,
    endTime: null
});

// 基础流量数据
const baseMetrics = ref(null);
// URL访问量前10名数据
const visitsMetrics = ref(null);
// URL报错量前10名数据
const errorsMetrics = ref(null);

// 搜索流量数据
const handleSearch = async () => {
    try {
        // 获取基础流量数据
        const baseResponse = await getBaseFlow(searchForm);
        baseMetrics.value = baseResponse.data;
        console.log('基础流量数据', baseResponse);

        // 获取URL访问量前10名数据
        const visitsResponse = await getVisitsUrlFlow(searchForm);
        visitsMetrics.value = visitsResponse.data;

        // 获取URL报错量前10名数据
        const errorsResponse = await getErrorsUrlFlow(searchForm);
        errorsMetrics.value = errorsResponse.data;

        // 等待 DOM 更新后再触发图表更新
        await nextTick();

    } catch (error) {
        ElMessage.error('查询失败: ' + error.message);
    }
};

// 获取时间
const getTime = (val) => {
    console.log('时间选择',val)
    if(val){
        searchForm.beginTime = val[0];
        searchForm.endTime = val[1];
    }
};

onMounted(async () => {
    await handleSearch();
});
</script>

<style scoped lang="scss">
@import '../index.scss';

.search-box {
    margin-bottom: 20px;
}

.el-table {
    margin-bottom: 10px;
}
</style>