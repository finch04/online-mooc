<template>
  <el-card class="item-card" shadow="hover">
    <div class="item-image-container">
      <img :src="item.imageUrl" :alt="item.itemName" class="item-image" />
      <div v-if="item.stock <= 0" class="sold-out">已售罄</div>
    </div>
    
    <div class="item-content">
      <h3 class="item-name">{{ item.itemName }}</h3>
      <p class="item-desc">{{ item.itemDesc }}</p>
      
      <div class="item-footer">
        <div class="points-required">
          <span>所需积分：</span>
          <span class="points">{{ item.pointsRequired }}</span>
        </div>
        
        <el-button
          type="primary"
          :disabled="disabled"
          @click="showExchangeDialog"
        >
          {{ buttonText }}
        </el-button>
      </div>
    </div>

    <!-- 兑换对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="兑换商品"
      width="500px"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="收货地址" required>
          <el-input
            v-model="form.address"
            placeholder="请输入收货地址"
            clearable
          />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input
            v-model="form.phone"
            placeholder="请输入联系电话"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" style="color: white;">取消</el-button>
        <el-button type="primary" @click="handleConfirmExchange">
          确认兑换
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  currentPoints: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['exchange'])

const dialogVisible = ref(false)
const form = ref({
  address: '',
  phone: ''
})

const disabled = computed(() => {
  return props.item.stock <= 0 || props.currentPoints < props.item.pointsRequired
})

const buttonText = computed(() => {
  if (props.item.stock <= 0) return '已售罄'
  if (props.currentPoints < props.item.pointsRequired) return '积分不足'
  return '立即兑换'
})

// 显示兑换对话框
const showExchangeDialog = () => {
  if (disabled.value) return
  form.value = { address: '', phone: '' } // 重置表单
  dialogVisible.value = true
}

// 确认兑换
const handleConfirmExchange = () => {
  if (!form.value.address || !form.value.phone) {
    ElMessage.warning('请填写完整的收货信息')
    return
  }
  
  // 验证电话号码格式
  if (!/^1[3-9]\d{9}$/.test(form.value.phone)) {
    ElMessage.warning('请输入正确的手机号码')
    return
  }
  
  emit('exchange', {
    itemId: props.item.id,
    address: form.value.address,
    phone: form.value.phone
  })
  dialogVisible.value = false
}
</script>

<style scoped>
.item-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.item-image-container {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.item-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.item-card:hover .item-image {
  transform: scale(1.05);
}

.sold-out {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.item-content {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-name {
  margin: 0 0 10px;
  font-size: 16px;
  font-weight: 500;
  color: var(--color-font1);
}

.item-desc {
  margin: 0 0 15px;
  font-size: 14px;
  color: var(--color-font3);
  flex: 1;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.points-required {
  font-size: 14px;
}

.points {
  color: var(--color-main);
  font-weight: bold;
}
</style>