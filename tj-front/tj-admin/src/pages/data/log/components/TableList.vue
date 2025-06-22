<template>
  <div >
    
    <el-table
      :data="logs.data"
      stripe
      border
      :loading="loading"
      @row-click="handleRowClick"
      empty-text="暂无数据"
    >
      <el-table-column prop="requestId" label="请求ID" width="180"></el-table-column>
      <el-table-column prop="time" label="创建时间" width="200"></el-table-column>
      <el-table-column prop="requestUri" label="请求路径" min-width="200"></el-table-column>
      <el-table-column prop="requestMethod" label="请求方式" width="100"></el-table-column>
      <el-table-column prop="host" label="请求主机" width="150"></el-table-column>
      <el-table-column prop="hostAddress" label="主机地址" width="150"></el-table-column>
      <el-table-column prop="responseTime" label="响应时间(ms)" width="120"></el-table-column>
      <el-table-column prop="responseCode" label="响应状态" width="100"></el-table-column>
      <el-table-column prop="requestBody" label="请求body" min-width="200">
        <template #default="scope">
          {{ scope.row.requestBody || '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="responseBody" label="应答body" min-width="200">
        <template #default="scope">
          {{ scope.row.responseBody || '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="responseMsg" label="应答msg" width="150">
        <template #default="scope">
          {{ scope.row.responseMsg || '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="userId" label="用户id" width="110">
        <template #default="scope">
          {{ scope.row.userId || '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="用户名称" width="120">
        <template #default="scope">
          {{ scope.row.userName || '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="businessType" label="业务类型" width="120">
        <template #default="scope">
          {{ scope.row.businessType || '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="dataState" label="数据标志位" width="150">
        <template #default="scope">
          {{ scope.row.dataState || '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="province" label="省" width="100">
        <template #default="scope">
          {{ scope.row.province || '--' }}
        </template>
      </el-table-column>
      <el-table-column prop="city" label="市" width="100">
        <template #default="scope">
          {{ scope.row.city || '--' }}
        </template>
      </el-table-column>
       <!-- 空页面 -->
       <template #empty>
        <!-- <EmptyPage :isSearch="isSearch" :baseData="logs.data"></EmptyPage> -->
      </template>
    </el-table>
    
    <!-- 分页 -->
      <el-pagination
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50, 100]"
        :current-page="searchForm.pageNo"
        :page-size="searchForm.pageSize"
        :total="logs.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      class="paginationBox"
      ></el-pagination>
      </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';
import { ElMessage } from 'element-plus';

const props = defineProps({
  logs: {
    type: Object,
    default: () => ({
      data: [],
      total: 0
    })
  },
  searchForm: {
    type: Object,
    default: () => ({
      pageNo: 1,
      pageSize: 10
    })
  },
  loading: {
    type: Boolean,
    default: false
  },
  isSearch: {
    type: Boolean,
    default: false
  }
});

const emits = defineEmits(['handleSizeChange', 'handleCurrentChange']);

// 处理行点击
const handleRowClick = (row) => {
  console.log('点击行:', row);
};

// 处理分页事件
const handleSizeChange = (newSize) => {
  emits('handleSizeChange', newSize);
};

const handleCurrentChange = (newPage) => {
  emits('handleCurrentChange', newPage);
};
</script>

<style scoped lang="scss">
@import '../../index.scss';
.table-container {
  padding: 20px;
}

</style>