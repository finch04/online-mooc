<template>
    <div class="contentBox">
      <ExamSearch :searchData="searchData" @handleSearch="handleSearch" @getList="getList"></ExamSearch>
      <div class="bg-wt radius marg-tp-20">
        <div class="tableBox">
          <ExamTableList
            :examData="examData.value"
            :total="total"
            :loading="loading"
            :isSearch="isSearch"
            :pageSize="searchData.pageSize"
            @getList="getList"
            @handleSizeChange="handleSizeChange"
            @handleCurrentChange="handleCurrentChange"
          ></ExamTableList>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, onMounted } from "vue";
  import { getExamPage } from "@/api/exam";
  
  import ExamSearch from "./components/Search.vue";
  import ExamTableList from "./components/TableList.vue";
  
  const loading = ref(false);
  let total = ref(null);
  let isSearch = ref(false);
  let searchData = reactive({
    pageSize: 10,
    pageNo: 1,
    examType: null
  });
  let examData = reactive([]);
  
  onMounted(() => {
    init();
  });
  
  const init = () => {
    getList();
  };
  
  const getList = async () => {
    loading.value = true;
    await getExamPage(searchData)
      .then((res) => {
        if (res.code === 200) {
          loading.value = false;
          examData.value = res.data.list;
          total.value = res.data.total;
        }
      })
      .catch((err) => {});
  };
  
  const handleSearch = () => {
    isSearch.value = true;
    getList();
  };
  
  const handleSizeChange = (val) => {
    searchData.pageSize = val;
    getList();
  };
  
  const handleCurrentChange = (val) => {
    searchData.pageNo = val;
    getList();
  };
  </script>
  
  <style src="./../index.scss" lang="scss" scoped></style>
  <style lang="scss" scoped>
  .contentBox {
    margin-bottom: 20px;
  }
  </style>