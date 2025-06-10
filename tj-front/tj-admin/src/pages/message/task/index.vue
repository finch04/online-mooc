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
          <el-form-item label="通知模板">
            <el-select v-model="taskForm.templateId" placeholder="请选择通知模板">
              <el-option
                v-for="template in noticeTemplates"
                :key="template.id"
                :label="template.name"
                :value="template.id"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="是否通知部分人">
            <el-switch v-model="taskForm.partial" @change="handlePartialChange"></el-switch>
          </el-form-item>
          <!-- 多选下拉查询框 -->
          <el-form-item label="已选用户" v-if="taskForm.partial">
            <el-select
              v-model="taskForm.userIds"
              multiple
              filterable
              collapse-tags
              collapse-tags-tooltip
              remote
              :remote-method="remoteSearchUsers"
              :loading="userLoading"
              placeholder="请选择用户"
              @visible-change="handleSelectVisibleChange"
              class="adaptive-select"
            >
              <el-option
                v-for="user in userList"
                :key="user.id"
                :label="user.name"
                :value="user.id"
              ></el-option>
            </el-select>
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
  queryNoticeTask,
  queryNoticeTemplates
} from '@/api/message.js';
import { queryUsersByPage } from "@/api/user.js";
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
  expireTime: null,
  userIds: [] // 新增字段用于存储选中的用户ID
});
const taskFormRef = ref(null);

const loading = ref(false);
const searchInfo = ref();
const isSearch = ref(false);

// 用户搜索表单
const userSearchForm = reactive({
  name: '',
  gender: null,
  type: null,
  pageNo: 1,
  pageSize: 30 // 一次查询十条数据
});

// 用户列表
const userList = ref([]);
const userTotal = ref(0);
const userLoading = ref(false);

// 通知模板列表
const noticeTemplates = ref([]);

// 搜索任务
const handleSearch = async () => {
  isSearch.value = true;
  loading.value = true;
  try {
    console.log(searchForm)
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
    userIds: [],
    maxTimes: 0,
    interval: null,
    expireTime: null,
    userIds: []
  };
  dialogVisible.value = true;
};

// 编辑任务
const handleEditTask = async (id) => {
  try {
    const res = await queryNoticeTask(id);
    console.log(res)
    if (res && res.data) {
      taskForm.value = res.data;
      dialogVisible.value = true;
      if (taskForm.value.partial) {
        remoteSearchUsers();
      }
    } else {
      ElMessage.error('获取任务信息失败，返回数据为空');
    }
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
  searchForm.minPushTime = val.min;
  searchForm.maxPushTime = val.max;
};

// 当是否通知部分人开关状态改变时
const handlePartialChange = (value) => {
  if (value) {
    userSearchForm.pageNo = 1;
    remoteSearchUsers();
  } else {
    userList.value = [];
    taskForm.value.userIds = [];
  }
};

// 远程搜索用户
const remoteSearchUsers = async (query = '') => {
  userSearchForm.name = query;
  userLoading.value = true;
  try {
    const res = await queryUsersByPage(userSearchForm);
    if (userSearchForm.pageNo === 1) {
      userList.value = res.data.list;
    } else {
      userList.value = [...userList.value, ...res.data.list];
    }
    userTotal.value = res.data.total;
  } catch (error) {
    ElMessage.error('查询用户失败');
  } finally {
    userLoading.value = false;
  }
};

// 处理下拉框显示状态改变
const handleSelectVisibleChange = (visible) => {
  if (visible) {
    userSearchForm.pageNo = 1;
    remoteSearchUsers();
  }
};

// 查询通知模板
const fetchNoticeTemplates = async () => {
  try {
    const res = await queryNoticeTemplates({ pageNo: 1, pageSize: 100 }); // 假设一次查询100条模板数据
    noticeTemplates.value = res.data.list;
  } catch (error) {
    ElMessage.error('查询通知模板失败');
  }
};

onMounted(() => {
  handleSearch();
  fetchNoticeTemplates();
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

.adaptive-select .el-select-dropdown__wrap {
  max-height: none; /* 移除最大高度限制 */
  overflow-y: auto; /* 当内容超出时显示滚动条 */
  width: 100%;
}
</style>