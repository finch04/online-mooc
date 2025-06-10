<template>
  <div class="bg-wt radius marg-tp-20">
    <div class="pad-30 searchForm">
      <el-form ref="ruleFormRef" :inline="true" :model="props.searchForm">
        <el-row :gutter="30">
          <el-col :span="6">
            <el-form-item label="任务名称" prop="keyword">
              <div class="el-input">
                <el-input
                  placeholder="请输入"
                  clearable
                  v-model="props.searchForm.keyword"
                />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <!-- 日期范围选择 -->
            <el-form-item label="预期执行时间" prop="datePicker">
              <el-date-picker
                v-model="datePicker"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                clearable
                @change="handleDate" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="是否完成" prop="finished">
              <div class="el-input">
                <el-checkbox v-model="props.searchForm.finished">已完成</el-checkbox>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <div class="btn">
              <el-button class="button primary" @click="handleSearch">搜索</el-button>
              <el-button class="button buttonSub" @click="handleReset"
                >重置</el-button
              >
            </div>
          </el-col>
        </el-row>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue';

// 定义 props 和 emits
const props = defineProps({
  searchForm: {
    type: Object,
    required: true,
    default: () => ({
      keyword: '',
      finished: false, // 布尔值更合理
      minPushTime: null, // 开始时间
      maxPushTime: null // 结束时间
    })
  }
});

const emit = defineEmits(['handleSearch', 'handleReset', 'getTime']);
const ruleFormRef = ref(null);
// 双向绑定日期范围（数组形式）
const datePicker = ref(props.searchForm.datePicker ? [props.searchForm.minPushTime, props.searchForm.maxPushTime] : []);

const handleSearch = () => {
  emit('handleSearch');
};

// 处理日期变化
const handleDate = (val) => {
  // 拆分开始和结束时间
  if (val && val.length === 2) {
    props.searchForm.minPushTime = val[0];
    props.searchForm.maxPushTime = val[1];
  } else {
    props.searchForm.minPushTime = null;
    props.searchForm.maxPushTime = null;
  }
  emit('getTime', { min: props.searchForm.minPushTime, max: props.searchForm.maxPushTime });
};

// 重置搜索表单
const handleReset = () => {
  if (ruleFormRef.value) {
    ruleFormRef.value.resetFields();
  }
  datePicker.value = []; // 清空时间
  props.searchForm.keyword = '';
  props.searchForm.finished = false;
  props.searchForm.minPushTime = null;
  props.searchForm.maxPushTime = null;
  console.log(props.searchForm);
  emit("handleReset"); // 重置表单
  emit("handleSearch"); // 刷新列表
};
</script>

<style scoped lang="scss">
@import '../../index.scss';

.btn {
  .button {
    display: flex;
    float:left
  }
}

:deep(.el-input__prefix-inner) {
  display: none;
}

:deep(.is-guttered) {
  min-width: 240px;
}
</style>