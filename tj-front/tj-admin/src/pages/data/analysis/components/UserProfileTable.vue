<template>
    <div class="contentBox">
        <el-table :data="tableData" stripe>
            <el-table-column prop="userId" label="用户ID"></el-table-column>
            <el-table-column prop="userName" label="用户名"  >
                <template #default="scope">
                    <div class="head" style="justify-content: left ;">
                        <span @click="handleMagnify(scope.row.icon)">
                            <img :src="scope.row.icon" />
                            {{ scope.row.icon }}
                            <span class="shade"><i></i></span>
                        </span>
                        {{ scope.row.userName }}
                    </div>
                </template>
            </el-table-column>
            <el-table-column prop="sex" label="性别">
                <template #default="scope">
                    {{ scope.row.sex === 0 ? '男性' : '女性' }}
                </template>
            </el-table-column>
            <el-table-column prop="province" label="省份"></el-table-column>
            <!-- <el-table-column prop="icon" label="头像">
                <template #default="scope">
                    <img :src="scope.row.icon" alt="头像" width="50" height="50">
                </template>
            </el-table-column> -->
            <el-table-column prop="courseLabels" label="常访问课程ID">
                <template #default="scope">
                    {{ scope.row.courseLabels?.join(', ') }}
                </template>
            </el-table-column>
            <el-table-column prop="freeLabel" label="课程偏好">
                <template #default="scope">
                    {{ scope.row.freeLabel === 0 ? '免费课程' : '付费课程' }}
                </template>
            </el-table-column>
        </el-table>
          <!-- 放大图片弹层 -->
    <ImageMagnify
      :dialogPicVisible="dialogPicVisible"
      :pic="pic"
      @handleMagnifyClose="handleMagnifyClose"
    ></ImageMagnify>
    <!-- end -->
    </div>
</template>

<script setup>
import { defineProps,ref } from 'vue';
// 图片放大弹层
import ImageMagnify from "@/components/ImageMagnify/index.vue";
let dialogPicVisible = ref(false); //控制放大图片弹层显示隐藏
let pic = ref(""); //要放大的图片
const props = defineProps({
    tableData: {
        type: Array,
        default: () => []
    }
});
//打开放大图弹层
const handleMagnify = (val) => {
  dialogPicVisible.value = true;
  pic.value = val;
};
// 关闭放大图弹层
const handleMagnifyClose = () => {
  dialogPicVisible.value = false;
  pic.value = "";
};
// 按esc关闭弹层
const handleEsc = (e) => {
  if (e.keyCode === 27) {
    dialogPicVisible.value = false;
    dialogResetVisible.value = false;
    dialogStatusVisible.value = false;
    pic.value = ""
  }
};
</script>
<style scoped lang="scss">
@import '../../index.scss';

.course-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.course-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    width: 100%;
    max-width: 300px;
}

.course-cover {
    width: 80px;
    height: 60px;
    overflow: hidden;
    border-radius: 4px;
}

.course-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.course-info {
    flex: 1;
    min-width: 0;
}

.course-name {
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.course-stats {
    display: flex;
    gap: 10px;
    font-size: 12px;
    color: #606266;
    margin-top: 4px;
}

.price.free {
    color: #67c23a;
}
.table-container {
  padding: 20px;
}

</style>