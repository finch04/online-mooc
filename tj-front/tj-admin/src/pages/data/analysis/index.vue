<template>
    <div class="contentBox">
        <!-- 搜索 -->
        <Search :searchForm="searchForm" @getTime="getTime" @handleSearch="handleSearch"></Search>
        <!-- end -->

        <!-- 数据看板 - 原三个图表 -->
        <div class="boarddata bg-wt">
            <div class="boarddatahead">
                <div class="tab">
                    <el-tabs v-model="activeTab" @tab-change="onTabChange">
                        <el-tab-pane name="courseConversion" label="课程转换漏斗" />
                        <el-tab-pane name="courseDetailGender" label="课程访问量性别分布" />
                        <el-tab-pane name="courseDetailProvince" label="课程访问量省排名" />
                    </el-tabs>
                </div>
            </div>

            <!-- 根据当前标签显示对应图表或表格 -->
            <EchartsFunnel v-if="activeTab === 'courseConversion'" :metrics="courseConversionMetrics" />
            <BaseMetric v-if="activeTab === 'courseDetailGender'" :metrics="courseDetailGenderMetrics"
                title="课程访问量性别分布" />
            <BaseMetric v-if="activeTab === 'courseDetailProvince'" :metrics="courseDetailProvinceMetrics"
                title="课程访问量省排名" />
        </div>

        <!-- 数据看板 - 用户画像和课程画像 -->
        <div class="bg-wt radius marg-tp-20">
            <el-tabs v-model="activeUserTab" @tab-change="onUserTabChange" style="float: right;margin-right: 20px;">
                <el-tab-pane name="userProfile" label="用户画像" />
                <el-tab-pane name="courseProfile" label="课程画像" />
            </el-tabs>
            <div class="tableBox">
                <UserProfileTable v-if="activeUserTab === 'userProfile'" :pageNo="userPageNo" :pageSize="userPageSize" />
                <CourseProfileTable v-if="activeUserTab === 'courseProfile'" :pageNo="coursePageNo" :pageSize="coursePageSize" />
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

// 导入组件
import Search from './components/Search.vue';
import BaseMetric from './components/BaseMetric.vue';
import EchartsFunnel from './components/EchartsFunnel.vue';
import UserProfileTable from './components/UserProfileTable.vue';
import CourseProfileTable from './components/CourseProfileTable.vue';

// 导入 API
import {
    getCourseConversionDpv,
    getCourseDetailGenderDuv,
    getCourseDetailProvinceDuv
} from '@/api/data';

// 搜索表单
const searchForm = reactive({
    beginTime: null,
    endTime: null,
    pageNo: 1,
    pageSize: 10
});

// 活跃标签
const activeTab = ref('courseConversion');
const activeUserTab = ref('userProfile');

// 展示数据
const courseConversionMetrics = ref(null);
const courseDetailGenderMetrics = ref(null);
const courseDetailProvinceMetrics = ref(null);

// 用户画像和课程画像分页参数
const userPageNo = ref(1);
const userPageSize = ref(10);
const coursePageNo = ref(1);
const coursePageSize = ref(10);

// 搜索流量数据
const handleSearch = async () => {
    try {
        // 获取课程转换漏斗数据
        const courseConversionResponse = await getCourseConversionDpv(searchForm);
        courseConversionMetrics.value = courseConversionResponse.data;

        // 获取课程详情性别分布数据
        const courseDetailGenderResponse = await getCourseDetailGenderDuv(searchForm);
        courseDetailGenderMetrics.value = courseDetailGenderResponse.data;

        // 获取课程详情省排名数据
        const courseDetailProvinceResponse = await getCourseDetailProvinceDuv(searchForm);
        courseDetailProvinceMetrics.value = courseDetailProvinceResponse.data;

        console.log('所有数据获取完成');
    } catch (error) {
        ElMessage.error('查询失败: ' + error.message);
    }
};

// 标签切换事件
const onTabChange = (tab) => {
    console.log('数据标签切换', tab.name);
};

const onUserTabChange = (tab) => {
    console.log('用户数据标签切换', tab.name);
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