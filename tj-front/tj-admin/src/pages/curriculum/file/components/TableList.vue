<!--媒资列表-->
<template>
  <div>
    <!-- 表格 -->
    <el-table :data="baseData" border stripe v-loading="loading" :sort-change="handleSortChange">
      <el-table-column type="index" align="center" width="100" label="序号" />
      <!-- <el-table-column prop="" label="文件预览" min-width="250" class-name="textLeft">
        <template #default="scope">
          <div>
            <el-image
              style="width: 80px; height: 80px"
              :src="scope.row.path"
              :zoom-rate="1.2"
              :max-scale="7"
              :min-scale="0.2"
              show-progress
              :initial-index="0"
              fit="cover"
            />
          </div>
        </template>
      </el-table-column> -->
      <el-table-column prop="" label="文件名称" min-width="250" class-name="textLeft">
        <template #default="scope">
          <div>
            {{ ellipsis(scope.row.filename,10) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="fileSize" label="大小（KB）" min-width="160">
        <template #default="scope">
          {{ (scope.row.fileSize / 1024 ).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="文件状态" min-width="160">
        <template #default="scope">
        {{ getStatusText(scope.row.status) }}
        </template>
      </el-table-column>
      <el-table-column prop="creater" label="上传人" min-width="150">
      </el-table-column>
      <el-table-column
        prop="useTimes"
        sortable="custom"
        label="当前引用次数"
        min-width="170"
      >
      </el-table-column>
      <el-table-column
        prop="createTime"
        sortable="custom"
        min-width="220"
        label="上传时间"
        :formatter="formatTime"
      >
      </el-table-column>
      <el-table-column
        fixed="right"
        label="操作"
        align="center"
        min-width="110"
        class-name="popperClass"
      >
        <template #default="scope">
          <div class="operate">
            <span class="textDefault" @click="handlePreview(scope.row.id)"
              >查看</span
            >
            <span
              @click="handleOpenDelete(scope.row)"  class="textDefault"
              :class="scope.row.useTimes > 0 ? 'textForbidden' : 'textWarning'"
              >删除</span
            >
          </div>
        </template>
      </el-table-column>
      <!-- 空页面 -->
      <template #empty>
        <EmptyPage :isSearch="isSearch" :baseData="baseData"></EmptyPage>
      </template>
      <!-- end -->
    </el-table>
    <!-- end -->
    <!-- 分页 -->
    <el-pagination
      v-if="total > 10"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :page-sizes="[10, 20, 30, 40]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="Number(total)"
      class="paginationBox"
    >
    </el-pagination>
    <!-- end -->
    <!-- 删除 -->
    <Delete
      :dialogDeleteVisible="dialogDeleteVisible"
      :deleteText="deleteText"
      @handleDelete="handleDelete"
      @handleClose="handleClose"
    ></Delete>
    <!-- end -->
    <!-- 预览弹层 -->
    <el-dialog v-model="dialogFormVisible" title="文件详情" custom-class="file-detail-dialog" >
        <div v-if="fileInfo" class="file-detail-content">
          <!-- <div class="image-preview">
           
          </div> -->
          <div class="info-details">
            <table>
              <tr>
                <td class="label">文件ID:</td>
                <td>{{ fileInfo.id }}</td>
              </tr>
              <tr>
                <td class="label">文件预览:</td>
                <td> <el-image
              style="width: 80px; height: 80px"
              :src="fileInfo.path"
              :zoom-rate="1.2"
              :max-scale="7"
              :min-scale="0.2"
              show-progress
              :initial-index="0"
              fit="cover"
            /></td>
              </tr>
              <tr>
                <td class="label">文件名称:</td>
                <td>{{ fileInfo.filename }}</td>
              </tr>
              <tr>
                <td class="label">文件访问路径:</td>
                <td>{{ fileInfo.path }}</td>
              </tr>
              <tr>
                <td class="label">文件hash值:</td>
                <td>{{ fileInfo.fileHash }}</td>
              </tr>
              <tr>
                <td class="label">文件类型:</td>
                <td>{{ fileInfo.fileType }}</td>
              </tr>
              <tr>
                <td class="label">文件key:</td>
                <td>{{ fileInfo.key }}</td>
              </tr>
              <tr>
                <td class="label">文件存储平台:</td>
                <td>{{ fileInfo.platform }}</td>
              </tr>
              <tr>
                <td class="label">大小（KB）:</td>
                <td>{{ (fileInfo.fileSize / 1024 ).toFixed(2) }}</td>
              </tr>
              <tr>
                <td class="label">上传人:</td>
                <td>{{ fileInfo.creater }}</td>
              </tr>
              <tr>
                <td class="label">当前引用次数:</td>
                <td>{{ fileInfo.useTimes }}</td>
              </tr>
              <tr>
                <td class="label">文件状态:</td>
                <td>{{ getStatusText(fileInfo.status) }}</td>
              </tr>
              <tr>
                <td class="label">上传时间:</td>
                <td>{{ fileInfo.createTime }}</td>
              </tr>
            </table>
          </div>
        </div>
      <template #footer>
        <el-button @click="dialogFormVisible = false">关闭</el-button>
      </template>
    </el-dialog>
    <!-- end -->
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from "vue"
import { ElMessage } from "element-plus"
import { formatSeconds } from '@/utils/index'
import { formatTime, ellipsis } from "@/utils/index"
// 接口api
import { deleteFile, getFileInfo } from "@/api/media"
// 导入组件
// 删除弹出层
import Delete from "@/components/Delete/index.vue"
// 空页面
import EmptyPage from "@/components/EmptyPage/index.vue"
// 获取父组件值、方法
const props = defineProps({
  // 数据
  baseData: {
    type: Array,
    default: () => [],
  },
  // 总条数
  total: {
    type: Number,
    default: 0,
  },
  // 每页的数量
  pageSize: {
    type: Number,
    default: 0,
  },
  // loading
  loading: {
    type: Boolean,
    default: false,
  },
  // 是否触发了搜索按钮
  isSearch: {
    type: Boolean,
    default: false,
  },
})
// ------定义变量------
const emit = defineEmits() //子组件获取父组件事件传值
const deleteText = ref("此操作将删除该文件，是否继续？") //需要删除的提示内容
let dialogDeleteVisible = ref(false) //控制删除弹层
let dialogFormVisible = ref(false) //弹层隐藏显示
let fileId = ref("")//文件id
let fileInfo = ref(null)

// ------定义方法------
// 确定删除
const handleDelete = async () => {
  await deleteFile(fileId.value)
    .then((res) => {
      if (res.code === 200) {
        ElMessage({
          message: "删除成功!",
          type: "success",
          showClose: false,
        })
        emit("getList")
        handleClose()
      }
    })
    .catch((err) => { })
}
// 设置每页条数
const handleSizeChange = (val) => {
  emit("handleSizeChange", val)
}
// 当前页
const handleCurrentChange = (val) => {
  emit("handleCurrentChange", val)
}
// 排序方式
const handleSortChange = (column, prop, order) => {
  console.log(column, prop, order);
}
// 打开删除弹层
const handleOpenDelete = (row) => {
  dialogDeleteVisible.value = true
  fileId.value = row.id
}
// 关闭删除弹层
const handleClose = () => {
  dialogDeleteVisible.value = false
}
// 打开预览弹层
const handlePreview = async (id) => {
  fileId.value = id
  try {
    const res = await getFileInfo(id)
    if (res.code === 200) {
      fileInfo.value = res.data
      dialogFormVisible.value = true
    } else {
      ElMessage({
        message: "获取文件详情失败",
        type: "error",
      })
    }
  } catch (error) {
    ElMessage({
      message: "获取文件详情失败",
      type: "error",
    })
  }
}
// 关闭弹层
const handlePreviewClose = () => {
  dialogFormVisible.value = false
};

// 根据状态码返回状态文本
const getStatusText = (status) => {
  switch (status) {
    case 1:
      return '上传中';
    case 2:
      return '已上传';
    case 3:
      return '已处理';
  }
}
</script>
<style lang="scss" scoped>
:deep(.el-table th.el-table__cell>.cell) {
  width: 106%;
}
.popperClass {
  .cell {
    padding: 0 !important;
  }
}
.file-detail-dialog {
  .el-dialog__content {
    padding: 20px;
  }
}
.file-detail-content {
  display: flex;
  gap: 20px;
}
.image-preview {
  flex-shrink: 0;
}
.info-details {
  flex-grow: 1;
}
.label {
  font-weight: bold;
  margin-right: 10px;
}
.info-details table {
  width: 100%;
  border-collapse: collapse;
}
.info-details table td {
  padding: 8px;
  border-bottom: 1px solid #ddd;
}
.info-details table td.label {
  width: 30%;
}
</style>