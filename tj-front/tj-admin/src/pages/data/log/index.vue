<template>
    <div class="contentBox">
        <!-- 搜索 -->
        <Search :searchForm="searchForm" ref="searchInfo" @getTime="getTime" @handleSearch="handleSearch" @handleReset="handleReset"></Search>
        <!-- end -->
        <div class="bg-wt radius marg-tp-20">
            <div class="tableBox">
                <div class="subHead pad-30">
                    <h3 class="font-bold text-lg mb-4">URL日志</h3>
                    <!-- 导出日志 -->
                    <el-button class="button primary" style="margin-bottom: 10px;float: right;" @click="exportLogs"
                        :text="text">导出7天内全部日志</el-button>
                    <!-- end -->
                </div>
                <!-- 表格数据 -->
                <TableList :logs="logs" :searchForm="searchForm" :loading="loading" :isSearch="isSearch"
                    @handleSizeChange="handleSizeChange" @handleCurrentChange="handleCurrentChange"></TableList>
                <!-- end -->

            </div>
        </div>
        <div class="bg-wt radius marg-tp-20">
            <!-- Echarts 图表 -->
            <UrlMetric :metrics="metrics" />
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch,nextTick } from 'vue';
import { ElMessage } from 'element-plus';

// 导入组件
import Search from './components/Search.vue';
import TableList from './components/TableList.vue';
import UrlMetric from './components/UrlMetric.vue';

// 导入 API
import {
    getLogsPageByUrl,
    getLogsPageByUrlByLike,
    getMetricByUrl,
    getMetricByUrlByLike,
    exportLog 
} from '@/api/data';

// 搜索表单
const searchForm = reactive({
    pageNo: 1,
    pageSize: 10,
    url: '',
    beginTime: null,
    endTime: null,
    matchType: 'exact'
});

// 日志数据
const logs = ref({
    data: [],
    total: 0
});

const loading = ref(false);
const searchInfo = ref();
const isSearch = ref(false);

// 指标数据
const metrics = ref(null);

// 搜索日志
const handleSearch = async () => {
    isSearch.value = true;
    loading.value = true;
    try {
        let response;
        // 根据匹配类型选择不同的 API
        if (searchForm.matchType === 'exact') {
            response = await getLogsPageByUrl(searchForm);
        } else {
            response = await getLogsPageByUrlByLike(searchForm);
        }
        if(response.code !== 200){
            ElMessage.error(response.msg);
            return
        }
        logs.value.data = response.data.list;
        logs.value.total = parseInt(response.data.total) || 0;

        console.log('获取指标数据',searchForm)
        let metricResponse;
        if (searchForm.matchType === 'exact') {
            metricResponse = await getMetricByUrl(searchForm);
        } else {
            metricResponse = await getMetricByUrlByLike(searchForm);
        }
        metrics.value = metricResponse.data;

        loading.value = false;
         // 等待 DOM 更新后再触发图表更新
         await nextTick();
    } catch (error) {
        ElMessage.error('查询失败: ' + error.message);
        loading.value = false;
    }
};

// 分页大小改变
const handleSizeChange = (newSize) => {
    searchForm.pageSize = newSize;
    handleSearch();
};

// 当前页码改变
const handleCurrentChange = (newPage) => {
    searchForm.pageNo = newPage;
    handleSearch();
};

// 重置表单
const handleReset = () => {
    searchForm.url = '';
    searchForm.beginTime = null;
    searchForm.endTime = null;
    searchForm.matchType = 'exact';
    searchForm.pageNo = 1;
    searchForm.pageSize = 10;
    handleSearch(); // 重置后重新搜索
};

// 获取时间
const getTime = (val) => {
    console.log('时间选择',val)
  searchForm.beginTime = val[0];
  searchForm.endTime = val[1];
};
const exportLogs = async () => {
    try {
        const response = await exportLog();
        
        // 调试用
        console.log("Received blob data:", response.data);
        
        const blob = new Blob([response.data], {
            type: response.headers['content-type']
        });
        
        const downloadUrl = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = response.headers['content-disposition'] 
            ? response.headers['content-disposition'].split('filename=')[1] 
            : 'logs_export.xlsx';
        document.body.appendChild(a);
        a.click();
        
        // 清理
        setTimeout(() => {
            document.body.removeChild(a);
            URL.revokeObjectURL(downloadUrl);
        }, 100);
        
    } catch (error) {
        console.error('Export failed:', error);
        ElMessage.error(`导出失败: ${error.response?.data?.message || error.message}`);
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