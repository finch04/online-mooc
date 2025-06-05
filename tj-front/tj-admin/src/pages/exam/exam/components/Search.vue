<template>
    <div class="bg-wt radius marg-tp-20">
      <div class="pad-30 searchForm">
        <el-form ref="ruleForm" :inline="true" :model="searchData">
          <el-row :gutter="30">
            <el-col :span="6">
              <el-form-item label="考试类型" prop="type">
                <div class="el-input">
                  <el-select
                    v-model="searchData.type"
                    clearable
                    placeholder="请选择"
                    style="width: 100%"
                  >
                    <el-option label="练习" value="1"></el-option>
                    <el-option label="考试" value="2"></el-option>
                  </el-select>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <div class="btn">
                <el-button class="button primary" @click="handleSearch">搜索</el-button>
                <el-button class="button buttonSub" @click="handleReset(ruleForm)">重置</el-button>
              </div>
            </el-col>
          </el-row>
        </el-form>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from "vue";
  
  const props = defineProps({
    searchData: {
      type: Object,
      default: () => ({})
    }
  });
  
  const emit = defineEmits();
  const ruleForm = ref();
  
  const handleSearch = () => {
    emit("handleSearch");
  };
  
  const handleReset = (ruleForm) => {
    ruleForm.resetFields();
    emit("getList");
  };
  </script>