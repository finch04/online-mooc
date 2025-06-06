<template>
    <div>
      <el-table :data="noticeTemplates.data" border stripe v-loading="loading" :default-sort="{prop: 'id', order: 'descending'}">
        <el-table-column type="index" align="center" width="100" label="序号" />
        <el-table-column label="模板ID" prop="id" min-width="230"></el-table-column>
        <el-table-column label="模板名称" prop="name" min-width="200"></el-table-column>
        <el-table-column label="模板代号" prop="code" min-width="200"></el-table-column>
        <el-table-column label="模板标题" prop="title" min-width="200"></el-table-column>
        <el-table-column label="短信模板" prop="isSmsTemplate" min-width="150">
          <template #default="scope">
            <el-tag type="success">{{ scope.row.isSmsTemplate?'是':'否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="通知类型" min-width="150">
          <template #default="scope">
            <el-tag>{{ getNoticeType(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="模板状态" min-width="120">
          <template #default="scope">
            <el-tag>{{ getTemplateStatus(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          label="操作"
          align="center"
          min-width="160"
          class-name="taskTable"
        >
          <template #default="scope">
            <div class="operate">
              <span class="textDefault" @click="handleEditTemplate(scope.row.id)">编辑</span>  <span class="textDefault" @click="handleDetailTemplate(scope.row.id)">查看</span>
              <!-- <span class="textDefault" @click="handleDeleteTemplate(scope.row.id)">删除</span> -->
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <EmptyPage :isSearch="isSearch" :baseData="noticeTemplates.data"></EmptyPage>
        </template>
      </el-table>
      <el-pagination
        v-if="noticeTemplates.total > 10"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :page-sizes="[10, 20, 30, 40]"
        :page-size="searchForm.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="Number(noticeTemplates.total)"
        class="paginationBox"
      >
      </el-pagination>
    </div>
  </template>
  
  <script setup>
  import { ref, defineProps, defineEmits } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import { formatTime } from "@/utils/index";
  import EmptyPage from "@/components/EmptyPage/index.vue";
  import { ElMessage } from 'element-plus';// 假设这里有删除接口
  
  const props = defineProps({
    noticeTemplates: {
      type: Object,
      default: () => ({ data: [], total: 0 }),
    },
    searchForm: {
      type: Object,
      default: () => ({}),
    },
    loading: {
      type: Boolean,
      default: false,
    },
    isSearch: {
      type: Boolean,
      default: false,
    },
  });
  
  const emit = defineEmits(['handleSizeChange', 'handleCurrentChange','handleDetailTemplate', 'handleEditTemplate', ]);
  const router = useRouter();
  const route = useRoute();
  
  const getNoticeType = (type) => {
    switch (type) {
      case 0:
        return '系统通知';
      case 1:
        return '笔记通知';
      case 2:
        return '问答通知';
      case 3:
        return '其它通知';
      default:
        return '未知类型';
    }
  };
  
  const getTemplateStatus = (status) => {
    switch (status) {
      case 0:
        return '草稿';
      case 1:
        return '使用中';
      case 2:
        return '停用';
      default:
        return '未知状态';
    }
  };


  
  const handleEditTemplate = (id) => {
    emit('handleEditTemplate', id);
  };

  const handleDetailTemplate = (id) => {
    emit('handleDetailTemplate', id);
  };
  
  const handleSizeChange = (val) => {
    emit('handleSizeChange', val);
  };
  
  const handleCurrentChange = (val) => {
    emit('handleCurrentChange', val);
  };
  
  // const handleDeleteTemplate = async (id) => {
  //   try {
  //     await deleteNoticeTemplate(id);
  //     ElMessage.success('删除模板成功');
  //     emit('handleDeleteTemplate', id);
  //   } catch (error) {
  //     ElMessage.error('删除模板失败');
  //   }
  // };
  </script>
  
  <style lang="scss" scoped>
  .taskTable {
    .cell {
      padding: 0 !important;
    }
  }
  </style>