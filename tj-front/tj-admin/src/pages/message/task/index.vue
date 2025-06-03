<template>
  <div class="contentBox">
    <!-- 搜索 -->
    <Search
      :searchForm="searchForm"
      ref="searchInfo"
      @getTime="getTime"
      @handleSearch="handleSearch"
      @handleReset="handleReset"
    ></Search>
    <!-- end -->
    <div class="bg-wt radius marg-tp-20">
      <div class="tableBox">
        <div class="subHead pad-30">
          <!-- 新增 -->
          <el-button class="button primary" style="margin-bottom: 10px;" @click="handleAddTask" :text="text">新增任务</el-button>
          <!-- end -->
        </div>
        <!-- 表格数据 -->
        <TableList
          :noticeTasks="noticeTasks"
          :searchForm="searchForm"
          :loading="loading"
          :isSearch="isSearch"
          @handleSizeChange="handleSizeChange"
          @handleCurrentChange="handleCurrentChange"
          @handleEditTask="handleEditTask"
        ></TableList>
        <!-- end -->
      </div>
    </div>
    <!-- 新增/编辑任务对话框 -->
    <el-dialog v-model="dialogVisible" title="任务信息" class="detailBox">
        <el-form :model="taskForm" ref="taskFormRef" label-width="180px" label-position="right">
          <el-form-item label="任务名称">
            <el-input v-model="taskForm.name"></el-input>
          </el-form-item>
          <el-form-item label="通知模板ID">
            <el-input v-model="taskForm.templateId"></el-input>
          </el-form-item>
          <el-form-item label="是否通知部分人">
            <el-switch v-model="taskForm.partial"></el-switch>
          </el-form-item>
          <el-form-item label="预期执行时间">
            <el-date-picker
              v-model="taskForm.pushTime"
              type="datetime"
              placeholder="请选择预期执行时间"
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="重复执行次数上限">
            <el-input v-model="taskForm.maxTimes" type="number"></el-input>
          </el-form-item>
          <el-form-item label="重复执行时间间隔（分钟）">
            <el-input v-model="taskForm.interval" type="number"></el-input>
          </el-form-item>
          <el-form-item label="失效时间">
            <el-date-picker
              v-model="taskForm.expireTime"
              type="datetime"
              placeholder="请选择失效时间"
            ></el-date-picker>
          </el-form-item>
        </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTask">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import {
  saveNoticeTask,
  updateNoticeTask,
  queryNoticeTasks,
  queryNoticeTask
} from '@/api/message.js';
import { ElMessage } from 'element-plus';
// 导入组件
import Search from './components/Search.vue';
import TableList from './components/TableList.vue';

// 搜索表单
const searchForm = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: '',
  minPushTime: null,
  maxPushTime: null,
  finished: null
});

// 任务列表数据
const noticeTasks = ref({
  data: [],
  total: 0
});

// 新增/编辑对话框相关
const dialogVisible = ref(false);
const taskForm = ref({
  id: null,
  name: '',
  templateId: null,
  partial: false,
  pushTime: null,
  maxTimes: 0,
  interval: null,
  expireTime: null
});
const taskFormRef = ref(null);

const loading = ref(false);
const searchInfo = ref();
const isSearch = ref(false);

// 搜索任务
const handleSearch = async () => {
  isSearch.value = true;
  loading.value = true;
  try {
    const res = await queryNoticeTasks(searchForm);
    noticeTasks.value.data = res.data.list;
    loading.value = false;
  } catch (error) {
    ElMessage.error('查询失败');
    loading.value = false;
  }
};

// 分页大小改变
const handleSizeChange = (newSize) => {
  searchForm.pageSize = newSize;
  handleSearch();
};

// 当前页码改变
const handleCurrentChange = (newPage) => {
  searchForm.pageNo = newPage;
  handleSearch();
};
// 重置表单
const handleReset = () => {
  searchForm.keyword = null;
  searchForm.finished = null;
  searchForm.minPushTime = null;
  searchForm.maxPushTime = null;
  handleSearch(); // 重置后重新搜索
};
// 新增任务
const handleAddTask = () => {
  taskForm.value = {
    id: null,
    name: '',
    templateId: null,
    partial: false,
    pushTime: null,
    maxTimes: 0,
    interval: null,
    expireTime: null
  };
  dialogVisible.value = true;
};

// 编辑任务
const handleEditTask = async (id) => {
  try {
    const res = await queryNoticeTask(id);
    taskForm.value = res.data;
    dialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取任务信息失败');
  }
};

// 保存任务
const handleSaveTask = async () => {
  try {
    if (taskForm.value.id) {
      // 更新任务
      await updateNoticeTask(taskForm.value, taskForm.value.id);
      ElMessage.success('更新任务成功');
    } else {
      // 新增任务
      await saveNoticeTask(taskForm.value);
      ElMessage.success('新增任务成功');
    }
    dialogVisible.value = false;
    handleSearch();
  } catch (error) {
    ElMessage.error('保存任务失败');
  }
};

// 获取时间
const getTime = (val) => {
  searchForm.minPushTime = val[0];
  searchForm.maxPushTime = val[1];
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
</style>