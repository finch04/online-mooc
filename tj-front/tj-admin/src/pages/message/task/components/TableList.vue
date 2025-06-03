<template>
  <div>
    <!-- 表格 -->
    <el-table :data="noticeTasks.data" border stripe v-loading="loading" :default-sort="{prop:'pushTime',order:'descending'}">
      <el-table-column type="index" align="center" width="100" label="序号" />
      <el-table-column label="任务ID" prop="id" min-width="230">
      </el-table-column>
      <el-table-column label="任务名称" prop="name" min-width="230">
      </el-table-column>
      <el-table-column label="预期执行时间" prop="pushTime" min-width="200">
      </el-table-column>
      <el-table-column label="失效时间" prop="expireTime" min-width="200">
      </el-table-column>
      <el-table-column label="是否完成" min-width="120">
        <template #default="scope">
          <el-tag>{{ scope.row.finished?"已完成":"未完成" }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        fixed="right"
        label="操作"
        align="center"
        min-width="100"
        class-name="taskTable"
      >
        <template #default="scope">
          <div class="operate">
            <span class="textDefault" @click="handleEditTask(scope.row.id)">编辑</span>
          </div>
        </template>
      </el-table-column>
      <!-- 空页面 -->
      <template #empty>
        <EmptyPage :isSearch="isSearch" :baseData="noticeTasks.data"></EmptyPage>
      </template>
      <!-- end -->
    </el-table>
    <!-- end -->
    <!-- 分页 -->
    <el-pagination
      v-if="noticeTasks.total > 10"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :page-sizes="[10, 20, 30, 40]"
      :page-size="searchForm.pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="Number(noticeTasks.total)"
      class="paginationBox"
    >
    </el-pagination>
    <!-- end -->
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { formatTime } from "@/utils/index";
// 空页面
import EmptyPage from "@/components/EmptyPage/index.vue";

const props = defineProps({
  // 搜索对象
  noticeTasks: {
    type: Object,
    default: () => ({ data: [], total: 0 }),
  },
  // 搜索表单
  searchForm: {
    type: Object,
    default: () => ({}),
  },
  // loading
  loading: {
    type: Boolean,
    default: false,
  },
  // 是否触发了搜索按钮
  isSearch: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['handleSizeChange', 'handleCurrentChange', 'handleEditTask']);
const router = useRouter();
const route = useRoute();

// 编辑任务
const handleEditTask = (id) => {
  emit('handleEditTask', id);
};

// 设置每页条数
const handleSizeChange = (val) => {
  emit('handleSizeChange', val);
};

// 当前页
const handleCurrentChange = (val) => {
  emit('handleCurrentChange', val);
};
</script>

<style lang="scss" scoped>
.taskTable {
  .cell {
    padding: 0 !important;
  }
}
</style>