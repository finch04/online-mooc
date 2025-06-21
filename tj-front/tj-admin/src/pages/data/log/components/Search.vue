<template>
  <div class="bg-wt radius marg-tp-20">
    <div class="pad-30 searchForm">
      <el-form ref="ruleFormRef" :inline="true" :model="props.searchForm">
        <el-row :gutter="30">
          <el-col :span="6">
            <el-form-item label="URL" prop="url">
              <div class="el-input">
                <el-input
                  placeholder="请输入"
                  clearable
                  v-model="props.searchForm.url"
                />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <!-- 日期范围选择 -->
            <el-form-item label="时间范围" prop="datePicker">
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
            <el-form-item label="匹配方式" prop="matchType">
              <div class="el-input">
                <el-radio-group v-model="props.searchForm.matchType">
                  <el-radio-button label="exact">精确匹配</el-radio-button>
                  <el-radio-button label="fuzzy">模糊匹配</el-radio-button>
                </el-radio-group>
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
      url: '',
      beginTime: null,
      endTime: null,
      matchType: 'exact',
      pageNo: 1,
      pageSize: 10
    })
  }
});

const emit = defineEmits(['handleSearch', 'handleReset', 'getTime']);
const ruleFormRef = ref(null);
// 双向绑定日期范围（数组形式）
const datePicker = ref(props.searchForm.datePicker ? [props.searchForm.beginTime, props.searchForm.endTime] : []);

const handleSearch = () => {
  emit('handleSearch');
};

// 处理日期变化
const handleDate = (val) => {
  // 拆分开始和结束时间
  if (val && val.length === 2) {
    props.searchForm.beginTime = val[0];
    props.searchForm.endTime = val[1];
  } else {
    props.searchForm.beginTime = null;
    props.searchForm.endTime = null;
  }
  emit('getTime', { min: props.searchForm.beginTime, max: props.searchForm.endTime });
};

// 重置搜索表单
const handleReset = () => {
  if (ruleFormRef.value) {
    ruleFormRef.value.resetFields();
  }
  datePicker.value = []; // 清空时间
  props.searchForm.url = '';
  props.searchForm.beginTime = null;
  props.searchForm.endTime = null;
  props.searchForm.matchType = 'exact';
  props.searchForm.pageNo = 1;
  props.searchForm.pageSize = 10;
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