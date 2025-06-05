<template>
  <div @keydown="handleEsc">
    <el-table :data="examData" border stripe v-loading="loading" class="examTableList">
      <el-table-column type="index" align="center" width="100" label="序号" />
      <el-table-column label="考试用户" min-width="140" class-name="textLeft">
        <template #default="scope">
          <div class="head">
            <span @click="handleMagnify(scope.row.icon)">
              <img :src="scope.row.icon" />
              <span class="shade"><i></i></span>
            </span>
            {{ scope.row.name }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="课程名称" min-width="240" prop="courseName" />
      <el-table-column label="节名称" min-width="240" prop="sectionName" />
      <el-table-column label="用户头像" min-width="240" prop="courseName" />
      <el-table-column label="考试类型" min-width="120">
        <template #default="scope">
          {{ scope.row.type === 1 ? '练习' : '考试' }}
        </template>
      </el-table-column>
      <el-table-column label="分数" min-width="120" prop="score" />
      <el-table-column label="用时(秒)" min-width="120" prop="duration" />
      <el-table-column label="提交时间" min-width="220" :formatter="formatTime" prop="finishTime" />
      <el-table-column fixed="right" label="操作" align="center" min-width="100" class-name="examTable">
        <template #default="scope">
          <div class="operate">
            <span class="textDefault" @click="handleDetail(scope.row)">详情</span>
          </div>
        </template>
      </el-table-column>
      <template #empty>
        <div class="emptyPage">
          <EmptyPage :isSearch="isSearch" :baseData="baseData"></EmptyPage>
        </div>
      </template>
    </el-table>
    <el-pagination v-if="total >= 10" @size-change="handleSizeChange" @current-change="handleCurrentChange"
      :page-sizes="[10, 20, 30, 40]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper"
      :total="Number(total)" class="paginationBox">
    </el-pagination>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { formatTime } from "@/utils/index";
import { getExamById } from "@/api/exam";

// 导入组件
import EmptyPage from "@/components/EmptyPage/index.vue";

const props = defineProps({
  examData: {
    type: Array,
    default: () => []
  },
  total: {
    type: Number,
    default: 0
  },
  pageSize: {
    type: Number,
    default: 0
  },
  loading: {
    type: Boolean,
    default: false
  },
  isSearch: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits();
const router = useRouter();

const handleEsc = (e) => {
  if (e.keyCode === 27) {
    // 这里可以根据实际情况处理，比如返回上一页
    router.back();
  }
};

const handleDetail = async (row) => {
  // 跳转到考试详情页面，并传递必要的参数
  router.push({
    path: "/exam/exam/detail/" + row.id,
    query: {
      id: row.id,
      // 可以根据需要传递更多参数
      title: row.title,
      courseName:row.courseName,
      sectionName:row.sectionName,
      name: row.name,
      duration:  row.duration,
      type: row.type,
      time: row.time,
      finishTime: row.finishTime,
      score:row.score
      // 其他参数
    }
  });
};

const handleSizeChange = (val) => {
  emit("handleSizeChange", val);
};

const handleCurrentChange = (val) => {
  emit("handleCurrentChange", val);
};
</script>

<style lang="scss" scoped>
.paginationBox {
  border-top: 1px solid #ebeef5;
}

:deep(.el-table__body-wrapper tr td.el-table-fixed-column--right) {
  .cell {
    padding: 0 20px;
  }
}
</style>

<style lang="scss">
.examTable {
  .cell {
    padding: 0 !important;
  }
}

.examTableList {
  .el-table__inner-wrapper {
    .el-table__body-wrapper {
      .el-scrollbar {
        .el-scrollbar__wrap {
          .el-scrollbar__view {
            .el-table__body {
              tbody {
                .el-table__row {
                  .el-table__cell {
                    padding: 0 !important;
                    height: 60px;
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>