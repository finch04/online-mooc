<template>
    <div class="contentBox">
      <Search
        :searchForm="searchForm"
        ref="searchInfo"
        @handleSearch="handleSearch"
        @handleReset="handleReset"
      ></Search>
      <div class="bg-wt radius marg-tp-20">
        <div class="tableBox">
          <div class="subHead pad-30">
            <el-button class="button primary" style="margin-bottom: 10px;" @click="handleAddTemplate">新增模板</el-button>
            <el-button class="button primary" style="margin-bottom: 10px;float: right;" @click="handleConfig">
              第三方短信平台管理</el-button>
              <el-button class="button primary" style="margin-bottom: 10px;float: right;" @click="handleMessageTemplate">
                短信模板管理</el-button>
          </div>
          <TableList
            :noticeTemplates="noticeTemplates"
            :searchForm="searchForm"
            :loading="loading"
            :isSearch="isSearch"
            @handleSizeChange="handleSizeChange"
            @handleCurrentChange="handleCurrentChange"
            @handleEditTemplate="handleEditTemplate"
            @handleDetailTemplate="handleDetailTemplate"
          ></TableList>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, onMounted } from 'vue';
  import {
    saveNoticeTemplate,
    updateNoticeTemplate,
    queryNoticeTemplates,
    queryNoticeTemplate
  } from '@/api/message.js';
  import { ElMessage } from 'element-plus';
  import { useRouter } from 'vue-router';
  import Search from './components/Search.vue';
  import TableList from './components/TableList.vue';
  
  const router = useRouter();
  
  const searchForm = reactive({
    pageNo: 1,
    pageSize: 10,
    keyword: '',
    status: null
  });
  
  const noticeTemplates = ref({
    data: [],
    total: 0
  });
  
  const loading = ref(false);
  const searchInfo = ref();
  const isSearch = ref(false);
  
  const handleSearch = async () => {
    isSearch.value = true;
    loading.value = true;
    try {
      const res = await queryNoticeTemplates(searchForm);
      noticeTemplates.value.data = res.data.list;
      noticeTemplates.value.total = res.data.total;
      loading.value = false;
    } catch (error) {
      ElMessage.error('查询失败');
      loading.value = false;
    }
  };
  
  const handleSizeChange = (newSize) => {
    searchForm.pageSize = newSize;
    handleSearch();
  };
  
  const handleCurrentChange = (newPage) => {
    searchForm.pageNo = newPage;
    handleSearch();
  };
  
  const handleReset = () => {
    searchForm.keyword = '';
    searchForm.status = null;
    handleSearch();
  };
  
  const handleConfig = () => {
    router.push({
      path: `/message/template/config`
    });
  };
  const handleMessageTemplate = () => {
    router.push({
      path: `/message/template/message`
    });
  };
  

  const handleAddTemplate = () => {
    router.push({
      path: `/message/template/add/null`
    });
  };
  
  const handleEditTemplate = (id) => {
    router.push({
      path: `/message/template/add/${id}`
    });
  };
  const handleDetailTemplate = (id) => {
    router.push({
      path: `/message/template/detail/${id}`
    });
  };

  onMounted(() => {
    handleSearch();
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