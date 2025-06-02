<template>
    <div class="contentBox">
      <!-- 搜索 -->
      <Search :searchData="searchData" @handleSearch="handleSearch"></Search>
      <!-- end -->
      <div class="bg-wt radius marg-tp-20">
        <div class="tableBox">
          <div class="conHead pad-30">
            <!-- 上传文件 -->
            <div class="addBox">
              <el-button type="primary" @click="handleUploadClick">上传文件</el-button>
            </div>
            <!-- end -->
          </div>
          <!-- 已上传列表 -->
          <TableList
            :baseData="baseData.value"
            :total="total"
            :loading="loading"
            :pageSize="searchData.pageSize"
            :isSearch="isSearch"
            @getList="getList"
            @handleSizeChange="handleSizeChange"
            @handleCurrentChange="handleCurrentChange"
            @deleteFile="deleteFile"
          ></TableList>
          <!-- end -->
        </div>
      </div>
      <!-- 上传中心 -->
      <UploadCenter ref="uploadCenterRef" />
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, onMounted } from "vue";
  import { ElMessage } from "element-plus";
  import { getFiles, deleteFile as deleteFileApi } from "@/api/media";
  import Search from "./components/Search.vue";
  import TableList from "./components/TableList.vue";
  import UploadCenter from "./components/UploadCenter.vue";
  
  const loading = ref(false);
  let total = ref(null);
  let searchData = reactive({
    pageSize: 10,
    pageNo: 1,
    sortBy: "id",
    isAsc: false,
  });
  let baseData = reactive([]);
  let isSearch = ref(false);
  const uploadCenterRef = ref(null);
  
  // 生命周期
  onMounted(() => {
    init();
  });
  
  // 方法
  const init = () => {
    getList();
  };
  
  const getList = async () => {
    loading.value = true;
    await getFiles(searchData)
      .then((res) => {
        if (res.code === 200) {
          loading.value = false;
          baseData.value = res.data.list;
          total.value = res.data.total;
        }
      })
      .catch((err) => {
        loading.value = false;
        ElMessage.error("获取文件列表失败");
      });
  };
  
  const deleteFile = async (id) => {
    await deleteFileApi(id)
      .then((res) => {
        if (res.code === 200) {
          ElMessage({
            message: "文件删除成功",
            type: "success",
          });
          getList();
        } else {
          ElMessage({
            message: res.message || "文件删除失败",
            type: "error",
          });
        }
      })
      .catch((error) => {
        ElMessage({
          message: "文件删除失败",
          type: "error",
        });
      });
  };
  
  // 点击上传按钮
  const handleUploadClick = () => {
    if (uploadCenterRef.value) {
      uploadCenterRef.value.openDialog();
    }
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