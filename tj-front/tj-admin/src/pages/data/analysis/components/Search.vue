<template>
    <div class="bg-wt radius marg-tp-20">
      <div class="pad-30 searchForm">
        <el-form ref="ruleFormRef" :inline="true" :model="props.searchForm">
          <el-row :gutter="30">
            <el-col :span="12">
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
                  @change="handleDate($event)"
                />
              </el-form-item>
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
        beginTime: null,
        endTime: null
      })
    }
  });
  
  const emit = defineEmits(['handleSearch', 'getTime']);
  const ruleFormRef = ref(null);
  // 双向绑定日期范围（数组形式）
  let datePicker = ref([]) //时间，数组形式
  
  // 处理日期变化
  const handleDate = (val) => {
    console.log('日期变化', val);
    emit('getTime', val);
    // 触发搜索事件
    emit('handleSearch');
  };
  
  // 向父组件暴露方法
  defineExpose({});
  </script>
  
  <style scoped lang="scss">
  @import '../../index.scss';
  
  :deep(.el-input__prefix-inner) {
    display: none;
  }
  
  :deep(.is-guttered) {
    min-width: 240px;
  }
  </style>