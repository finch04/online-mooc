<template>
  <div class="bg-wt radius marg-tp-20">
    <div class="pad-30 searchForm">
      <el-form ref="ruleFormRef" :inline="true" :model="props.searchForm">
        <el-row :gutter="30">
          <el-col :span="6">
            <el-form-item label="名称" prop="keyword">
              <div class="el-input">
                <el-input
                  placeholder="请输入关键字"
                  clearable
                  v-model="props.searchForm.keyword"
                />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态" prop="status">
              <div class="el-input">
                <el-select v-model="props.searchForm.status" placeholder="请选择状态">
                  <el-option label="草稿" value="0"></el-option>
                  <el-option label="使用中" value="1"></el-option>
                  <el-option label="停用" value="2"></el-option>
                </el-select>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <div class="btn">
              <el-button class="button primary" @click="handleSearch">搜索</el-button>
              <el-button class="button buttonSub" @click="handleReset">重置</el-button>
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

const emit = defineEmits(['handleSearch', 'handleReset']);
const ruleFormRef = ref(null);

const handleSearch = () => {
  emit('handleSearch');
};

const handleReset = () => {
  if (ruleFormRef.value) {
    ruleFormRef.value.resetFields();
  }
  props.searchForm.keyword = '';
  props.searchForm.status = null;
  emit("handleReset");
  emit("handleSearch");
};
</script>

<style scoped lang="scss">
@import '../../index.scss';

.btn {
  .button {
    display: flex;
    float: left;
  }
}

:deep(.el-input__prefix-inner) {
  display: none;
}

:deep(.is-guttered) {
  min-width: 240px;
}
</style>