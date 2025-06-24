<template>
    <div class="contentBox">
        <el-table :data="tableData" stripe>
            <el-table-column prop="courseId" label="课程ID"></el-table-column>
            <el-table-column prop="name" label="课程名称"></el-table-column>
            <el-table-column prop="coverUrl" label="封面">
                <template #default="scope">
                    <img :src="scope.row.coverUrl" alt="课程封面" width="80" height="60">
                </template>
            </el-table-column>
            <el-table-column label="课程价格" prop="price" sortable width="150" :sort-method="(a, b) => {
                    return a.price - b.price;
                }
                ">
                <template #default="scope">
                    <span class="fontTip ft-wt-600">{{
                        scope.row.price === "0" ? "免费" : "￥" + scope.row.price
                        }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="sexLabel" label="访问用户性别标签" width="180"></el-table-column>
            <el-table-column prop="provinceLabels" label="访问用户省份标签"  width="180">
                <template #default="scope">
                    {{ scope.row.provinceLabels?.join(', ') }}
                </template>
            </el-table-column>
            <!-- <el-table-column prop="createTime" label="创建时间"></el-table-column> -->
            <el-table-column prop="updateTime" label="更新时间"  width="150"></el-table-column>
        </el-table>
        <!-- 分页组件 -->
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNo"
            :page-sizes="[10, 20, 30]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="Number(total)"
             class="paginationBox">
        </el-pagination>
    </div>
</template>

<script setup>
import { defineProps, ref, onMounted } from 'vue';
import { getAnalysisResultByCourse } from '@/api/data';
import { ElMessage } from 'element-plus';

const props = defineProps({
    pageNo: {
        type: Number,
        default: 1
    },
    pageSize: {
        type: Number,
        default: 10
    }
});

const tableData = ref([]);
const total = ref(0);
const pageNo = ref(props.pageNo);
const pageSize = ref(props.pageSize);

// 搜索课程画像数据
const handleSearch = async () => {
    try {
        const searchParams = {
            pageNo: pageNo.value,
            pageSize: pageSize.value
        };
        const courseProfileResponse = await getAnalysisResultByCourse(searchParams);
        tableData.value = courseProfileResponse.data.list;
        total.value = courseProfileResponse.data.total;
        console.log('课程画像数据获取完成');
    } catch (error) {
        ElMessage.error('查询失败: ' + error.message);
    }
};

// 分页事件
const handleSizeChange = (newSize) => {
    pageSize.value = newSize;
    handleSearch();
};

const handleCurrentChange = (newPage) => {
    pageNo.value = newPage;
    handleSearch();
};

onMounted(async () => {
    await handleSearch();
});
</script>

<style scoped lang="scss">
@import '../../index.scss';

.el-table__cell img {
    width: 80px;
    height: 60px;
    object-fit: cover;
}

.table-container {
    padding: 20px;
}
</style>