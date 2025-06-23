<template>
    <div class="contentBox">
        <!-- 搜索 -->
        <Search :searchForm="searchForm" @getTime="getTime" @handleSearch="handleSearch"></Search>
        <!-- end -->
        
        <!-- 基础数据看板 -->
        <div class="boarddata bg-wt">
            <div class="boarddatahead">
                <div class="tab">
                    <el-tabs v-model="baseActiveTab" @tab-change="onBaseTabChange">
                        <el-tab-pane v-for="(item, index) in baseChartTabs" :key="item.name" :label="item.label" :name="item.name">
                        </el-tab-pane>
                    </el-tabs>
                </div>
            </div>
            
            <!-- 根据当前标签显示对应图表 -->
            <BaseMetric v-if="baseActiveTab === 'dpv'" :metrics="dpvMetrics" title="DPV 页面浏览量趋势" />
            <BaseMetric v-if="baseActiveTab === 'dau'" :metrics="dauMetrics" title="DAU 活跃用户数趋势" />
            <BaseMetric v-if="baseActiveTab === 'duv'" :metrics="duvMetrics" title="DUV 独立访客数趋势" />
            <BaseMetric v-if="baseActiveTab === 'dnu'" :metrics="dnuMetrics" title="DNU 新用户趋势" />
        </div>

        <!-- 数据看板 -->
        <div class="boarddata bg-wt">
            <div class="boarddatahead" >
                <div class="tab">
                    <el-tabs v-model="urlActiveTab" @tab-change="onUrlTabChange" >
                        <el-tab-pane name="dpvTime" label="活跃访问数时段" />
                        <el-tab-pane name="dauProvince" label="活跃用户省分布排名" />
                    </el-tabs>
                </div>
            </div>
            
            <!-- 根据当前标签显示对应图表，使用BaseMetric组件 -->
            <BaseMetric v-if="urlActiveTab === 'dpvTime'" :metrics="dpvTimeMetrics" title="活跃访问数时段" />
            <BaseMetric v-if="urlActiveTab === 'dauProvince'" :metrics="dauProvinceMetrics" title="活跃用户省分布排名" />
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue';
import { ElMessage } from 'element-plus';

// 导入组件
import Search from './components/Search.vue';
import BaseMetric from './components/BaseMetric.vue';

// 导入 API
import {
    getDnu,
    getDuv,
    getDpv,
    getDau,
    getDpvTime,
    getDauProvince
} from '@/api/data';

// 搜索表单
const searchForm = reactive({
    beginTime: null,
    endTime: null
});

// 基础数据看板标签配置
const baseChartTabs = ref([
    { name: 'dpv', label: '页面浏览量' },
    { name: 'dau', label: '活跃用户数' },
    { name: 'duv', label: '独立访客数' },
    { name: 'dnu', label: '新用户趋势' }
]);

// URL数据看板活跃标签
const urlActiveTab = ref('dpvTime');
// 基础数据看板活跃标签
const baseActiveTab = ref('dpv');

// 不同类型的基础流量数据
const dnuMetrics = ref(null);
const duvMetrics = ref(null);
const dpvMetrics = ref(null);
const dauMetrics = ref(null);

// URL访问量前10数据
const dpvTimeMetrics = ref(null);
// URL报错量前10数据
const dauProvinceMetrics = ref(null);

// 搜索流量数据
const handleSearch = async () => {
    try {
        // 获取基础流量数据
        const dnuResponse = await getDnu(searchForm);
        dnuMetrics.value = dnuResponse.data;
        
        const duvResponse = await getDuv(searchForm);
        duvMetrics.value = duvResponse.data;
        
        const dpvResponse = await getDpv(searchForm);
        dpvMetrics.value = dpvResponse.data;
        
        const dauResponse = await getDau(searchForm);
        dauMetrics.value = dauResponse.data;
        
        // 获取URL相关数据
        const dpvTimeResponse = await getDpvTime(searchForm);
        dpvTimeMetrics.value = dpvTimeResponse.data;
        
        const dauProvinceResponse = await getDauProvince(searchForm);
        dauProvinceMetrics.value = dauProvinceResponse.data;
        
        console.log('所有数据获取完成');
        // 等待 DOM 更新后再触发图表更新
        await nextTick();

    } catch (error) {
        ElMessage.error('查询失败: ' + error.message);
    }
};

// 基础数据看板标签切换事件
const onBaseTabChange = (tab) => {
    console.log('基础数据标签切换', tab.name);
};

// URL数据看板标签切换事件
const onUrlTabChange = (tab) => {
    console.log('URL数据标签切换', tab.name);
};

// 获取时间
const getTime = (val) => {
    console.log('时间选择', val);
    if (val) {
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

.boarddata {
  margin-top: 10px;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  
  .boarddatahead {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
    font-size: 16px;
    color: #333;
    padding: 14px 20px;
    border-bottom: 1px solid #ebeef5;
  }
  
  .tab {
    width: 100%;
  }
}
</style>