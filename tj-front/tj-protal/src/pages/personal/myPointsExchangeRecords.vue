<template>
  <div class="records-container">
    <BreadCrumb />
    <div class="header">
      <h2>我的兑换记录</h2>
      <div class="btn" style="width: 20%;">
        <span class="bt bt-round" @click="() => $router.push({path: 'myIntegralShop'})">
          返回积分商城
        </span>
      </div>
    </div>

    <el-table :data="records" stripe v-loading="loading" empty-text="暂无兑换记录">
      <el-table-column prop="itemUrl" label="" width="100">
        <template #default="scope">
          <div style="display: flex; align-items: center">
            <el-avatar :src="scope.row.itemUrl" />
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="itemName" label="商品名称" width="200" />
      <el-table-column prop="pointsUsed" label="消耗积分" width="120" />
      <el-table-column prop="createTime" label="兑换时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ formatStatus(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="expressNumber" label="快递单号" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <span 
            class="bt bt-round" 
            style="width: 70%;" 
            @click="showDetail(row.id)"
          >
            详情
          </span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination 
      v-if="total > 0" 
      background 
      layout="total, sizes, prev, pager, next" 
      :total="total"
      :page-size="params.pageSize" 
      :current-page="params.pageNo" 
      @size-change="handleSizeChange"
      @current-change="handlePageChange" 
    />

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="兑换记录详情"
      width="600px"
    >
      <el-descriptions 
        :column="1" 
        border
        v-loading="detailLoading"
      >
        <el-descriptions-item label="商品名称">
          <div class="detail-item">
            <el-avatar :src="currentRecord.itemUrl" size="small" />
            <span style="margin-left: 10px;">{{ currentRecord.itemName }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="消耗积分">
          {{ currentRecord.pointsUsed }}
        </el-descriptions-item>
        <el-descriptions-item label="兑换状态">
          <el-tag :type="getStatusTagType(currentRecord.status)">
            {{ formatStatus(currentRecord.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收货地址">
          {{ currentRecord.address || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          {{ currentRecord.phone || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="快递单号">
          {{ currentRecord.expressNumber || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="兑换时间">
          {{ currentRecord.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ currentRecord.updateTime ? currentRecord.updateTime: '无' }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer style="display: flex;">
       <div style="display: flex;">
          <span class="bt bt-round" style="margin-right: 50px;"
            v-if="currentRecord.status === 0"
            type="danger"
            @click="handleCancel(currentRecord.id)"
          >
            取消兑换
          </span>
          <span class="bt bt-round" @click="detailDialogVisible = false">
            关闭
          </span>
       </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { queryExchangeRecordsByUser, queryExchangeRecordById,cancelExchangeRecordById } from '@/api/class.js'
// 组件导入
import BreadCrumb from './components/BreadCrumb.vue'

const router = useRouter()
const loading = ref(false)
const detailLoading = ref(false)
const records = ref([])
const total = ref(0)
const detailDialogVisible = ref(false)
const currentRecord = ref({
  id: null,
  userId: null,
  itemId: null,
  itemName: '',
  itemUrl: '',
  pointsUsed: 0,
  status: 0,
  expressNumber: '',
  address: '',
  phone: '',
  createTime: '',
  updateTime: ''
})

const params = ref({
  pageNo: 1,
  pageSize: 10
})

// 获取兑换记录
const fetchRecords = async () => {
  try {
    loading.value = true
    const res = await queryExchangeRecordsByUser(params.value)
    if (res.code === 200) {
      records.value = res.data.list
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取兑换记录失败')
  } finally {
    loading.value = false
  }
}

// 显示详情
const showDetail = async (recordId) => {
  try {
    detailLoading.value = true
    detailDialogVisible.value = true
    const res = await queryExchangeRecordById(recordId)
    if (res.code === 200) {
      currentRecord.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 取消兑换
const handleCancel = (recordId) => {
  ElMessageBox.confirm('确定要取消该兑换记录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 这里应该调用取消兑换的API
      await cancelExchangeRecordById(recordId)
      ElMessage.success('取消成功')
      detailDialogVisible.value = false
      fetchRecords()
    } catch (error) {
      ElMessage.error(error.message || '取消失败')
    }
  })
}

// 状态格式化
const formatStatus = (status) => {
  const statusMap = {
    0: '待发货',
    1: '已发货',
    2: '已完成',
    3: '已取消'
  }
  return statusMap[status] || '未知状态'
}

// 状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    0: 'warning',
    1: '',
    2: 'success',
    3: 'info'
  }
  return typeMap[status] || ''
}

// 时间格式化
const formatTime = (time) => {
  return new Date(time).toLocaleString()
}

// 分页处理
const handleSizeChange = (size) => {
  params.value.pageSize = size
  fetchRecords()
}

const handlePageChange = (page) => {
  params.value.pageNo = page
  fetchRecords()
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.btn {
  width: 250px;
  height: 40px;
  display: flex;
  align-items: center;
  margin: 10px 0;
}
.records-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.el-table {
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
}
</style>