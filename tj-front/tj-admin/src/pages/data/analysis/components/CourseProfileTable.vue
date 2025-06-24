<template>
    <div class="contentBox">
        <el-table :data="tableData" stripe>
            <el-table-column prop="courseId" label="课程ID" ></el-table-column>
            <el-table-column prop="name" label="课程名称"></el-table-column>
            <el-table-column prop="coverUrl" label="封面">
                <template #default="scope">
                    <img :src="scope.row.coverUrl" alt="课程封面" width="80" height="60">
                </template>
            </el-table-column>
            <el-table-column prop="price" label="价格">
                <template #default="scope">
                    {{ scope.row.free ? '免费' : `¥${scope.row.price}` }}
                </template>
            </el-table-column>
            <el-table-column prop="sexLabel" label="访问用户性别标签"></el-table-column>
            <el-table-column prop="provinceLabels" label="访问用户省份标签">
                <template #default="scope">
                    {{ scope.row.provinceLabels?.join(', ') }}
                </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间"></el-table-column>
            <el-table-column prop="updateTime" label="更新时间"></el-table-column>
        </el-table>
    </div>
</template>

<script setup>
import { defineProps } from 'vue';

const props = defineProps({
    tableData: {
        type: Array,
        default: () => []
    }
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