<template>
    <div class="points-mall-container">
        <BreadCrumb/> 
        <!-- 积分展示和头部 -->
        <div class="points-header">
            <div class="points-info">
                <span class="points-label">当前积分：</span>
                <span class="points-value">{{ currentPoints }}</span>
            </div>
            <div class="btn" style="width: 20%;"><span class="bt bt-round"  @click="$router.push({ path: 'myPointsExchangeRecords' })">我的兑换记录</span></div>
        </div>

        <!-- 商品列表 -->
        <div class="items-grid">
            <PointsMallItem v-for="item in items" :key="item.id" :item="item" :current-points="currentPoints"
                @exchange="handleExchange" />
        </div>

        <!-- 分页 -->
        <el-pagination v-if="total > 0" background layout="total, sizes, prev, pager, next" :total="total"
            :page-size="params.pageSize" :current-page="params.pageNo" @size-change="handleSizeChange"
            @current-change="handlePageChange" />

        <!-- 加载状态 -->
        <el-skeleton v-if="loading" :rows="6" animated />
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMallItems, exchangeItem, getUserCurrentPoints } from '@/api/class.js'
import PointsMallItem from './components/PointsMallItem.vue'
// 组件导入
import BreadCrumb from './components/BreadCrumb.vue'


const router = useRouter()
const loading = ref(false)
const currentPoints = ref(0)
const items = ref([])
const total = ref(0)

const params = ref({
    pageNo: 1,
    pageSize: 12
})


const getUserPoints = async () => {
    try {
        const res = await getUserCurrentPoints()
        if (res.code === 200) {
            currentPoints.value = res.data
        }
    } catch (error) {
        ElMessage.error('获取用户积分失败')
    }
}
// 获取商品列表
const fetchItems = async () => {
    try {
        loading.value = true
        const res = await getMallItems(params.value)
        if (res.code === 200) {
            items.value = res.data.list
            total.value = res.data.total
        }
    } catch (error) {
        ElMessage.error('获取商品列表失败')
    } finally {
        loading.value = false
    }
}

// 兑换商品
const handleExchange = async ({ itemId, address, phone }) => {
    try {
        const res = await exchangeItem({
            itemId,
            address,
            phone
        })
        if (res.code === 200) {
            ElMessage.success('兑换成功')
            fetchItems()
            // 这里应该更新用户积分，假设有相关方法
        }
    } catch (error) {
        ElMessage.error(error.message || '兑换失败')
    }
}

// 分页处理
const handleSizeChange = (size) => {
    params.value.pageSize = size
    fetchItems()
}

const handlePageChange = (page) => {
    params.value.pageNo = page
    fetchItems()
}

onMounted(() => {
    fetchItems()
    // 这里应该获取用户当前积分，假设有相关方法
    getUserPoints()
})
</script>

<style scoped>
.points-mall-container {
    padding: 20px;
}

.points-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.points-info {
    font-size: 16px;
}

.points-label {
    color: var(--color-font3);
}

.points-value {
    font-weight: bold;
    color: var(--color-main);
}

.items-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
    margin-bottom: 20px;
}
</style>