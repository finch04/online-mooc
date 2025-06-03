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
            <el-form-item label="预期执行时间" prop="datePicker">
              <div class="el-input">
                <el-date-picker
                  v-model="datePicker"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  clearable
                  @change="handleDate($event)"
                >
                </el-date-picker>
              </div>
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

const props = defineProps({
  searchForm: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['handleSearch', 'handleReset', 'getTime']);
const ruleFormRef = ref(null);
let datePicker = ref([]);

const handleSearch = () => {
  emit('handleSearch');
};

const handleDate = (val) => {
  emit('getTime', val);
};

// 重置搜索表单
const handleReset = () => {
  if (ruleFormRef.value) {
    ruleFormRef.value.resetFields();
  }
  datePicker.value = []; // 清空时间
  props.searchForm.keyword = '';
  props.searchForm.finished = '';
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