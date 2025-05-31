<template>
  <div class="mySetWrapper content">
    <CardsTitle class="marg-bt-40" title="个人设置" />
    <TableSwitchBar :data="tabData" @changeTable="checkHandle"></TableSwitchBar>  
    
    <!-- 基本信息 -->
    <div v-if="act == 0" class="fx-sb pd-tp-30">
      <div>
        <div class="fx">
          <!-- 一期先不加  放到二期 -->
          <div class="item fx">
            <span class="lab">账号：</span><el-input disabled v-model="user.username" placeholder="请输入内容"></el-input>
          </div> 
          <div class="item fx">
            <span class="lab">昵称：</span> <el-input v-model="user.name" placeholder="请输入内容"></el-input>
          </div>
        </div>
        <div class="item fx">
          <span class="lab">性别：</span>
          <el-radio-group class="radioGroup" v-model="user.gender">
            <el-radio :label="0">男</el-radio>
            <el-radio :label="1">女</el-radio>
          </el-radio-group>
        </div>
        <div class="item fx">
          <span class="lab">QQ：</span>
          <el-input v-model="user.qq" placeholder="请输入QQ号"></el-input>
        </div>
        <div class="item fx">
          <span class="lab">邮箱：</span>
          <el-input v-model="user.email" placeholder="请输入邮箱"></el-input>
        </div>
        <div class="item fx">
          <span class="lab">地区：</span>
          <el-cascader
            v-model="selectedRegion"
            :options="regionOptions"
            placeholder="请选择地区"
          ></el-cascader>
        </div>
        <div class="item fx">
          <span class="lab">简介：</span>
          <el-input 
            v-model="user.intro" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入个人简介"
          ></el-input>
        </div>
        <div class="item fx">
          <div class="bt" @click="updateUserInfoHandle">更新信息</div>
        </div>
      </div>
      
      <div>
        <el-upload
          class="avatar-uploader"
          :action="actions"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :headers="uploadHeaders"
          >
          <img v-if="imageUrl" :src="imageUrl" class="avatar">
          <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          <div class="uploadBut"><span>上传头像</span></div>
        </el-upload>
      </div>
    </div>
    
    <!-- 安全设置 -->
    <div v-else class="security-settings pd-tp-30">
      <div class="security-item">
        <div class="security-content">
          <div class="security-title">登录密码</div>
          <!-- <div class="security-info">当前密码强度：<span :class="passwordStrengthClass">{{passwordStrength}}</span></div> -->
        </div>
        <span class="font-bt" @click="showPasswordDialog">修改</span>
      </div>
      
      <div class="security-item">
        <div class="security-content">
          <div class="security-title">绑定手机</div>
          <div class="security-info">已绑定手机：{{user.cellPhone || '未绑定'}}</div>
        </div>
        <span class="font-bt" @click="showPhoneDialog">{{user.cellPhone ? '修改' : '绑定'}}</span>
      </div>
      
    </div>
    
    <!-- 修改密码弹窗 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="500px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
          <div class="password-strength">
            <span :class="{'active': passwordStrengthLevel >= 1}">弱</span>
            <span :class="{'active': passwordStrengthLevel >= 2}">中</span>
            <span :class="{'active': passwordStrengthLevel >= 3}">强</span>
          </div>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false" style="color: white;">取消</el-button>
        <el-button type="primary" @click="submitPasswordChange">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 修改手机号弹窗 -->
    <el-dialog v-model="phoneDialogVisible" :title="user.cellPhone ? '修改手机号' : '绑定手机号'" width="500px">
      <el-form :model="phoneForm" :rules="phoneRules" ref="phoneFormRef" label-width="100px">
        <el-form-item v-if="user.cellPhone" label="当前手机号">
          <el-input :value="user.cellPhone" disabled></el-input>
        </el-form-item>
        <el-form-item label="新手机号" prop="phone">
          <el-input v-model="phoneForm.phone" placeholder="请输入新手机号"></el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="verify-code-input">
            <el-input v-model="phoneForm.code" placeholder="请输入验证码"></el-input>
            <el-button 
              type="primary" 
              :disabled="codeCountdown > 0"
              @click="sendPhoneCode"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}秒后重试` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="phoneDialogVisible = false" style="color: white;">取消</el-button>
        <el-button type="primary" @click="submitPhoneChange">确认{{user.cellPhone ? '修改' : '绑定'}}</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { updateUserInfo ,getUserInfo,sendSms,bindPhone,updatePassword} from "@/api/user.js";
import { useUserStore } from "@/store"
import proxy from '@/config/proxy';
import { regionData } from 'element-china-area-data';

import CardsTitle from "./components/CardsTitle.vue";
import TableSwitchBar from "@/components/TableSwitchBar.vue";

const store = useUserStore()
const userInfo = ref(store.getUserInfo)

const env = import.meta.env.MODE || "development"
const actions = proxy[env].host+'/ms/files'
const uploadHeaders = {authorization: store.getToken}
const tabData = [
  {id: 0, name: '基本信息'},
  {id: 1, name: '安全设置'}
]

// 切换基本信息和安全设置
const act = ref(0)
const checkHandle = val => {
  act.value = val
}

// 用户信息
const user = reactive({
  id:userInfo.value.id,
  username : userInfo.value.username,
  name: userInfo.value.name,
  icon: userInfo.value.icon,
  gender: userInfo.value.gender || 0,
  cellPhone: userInfo.value.cellPhone,
  email: userInfo.value.email,
  qq: userInfo.value.qq,
  intro: userInfo.value.intro,
  province: userInfo.value.province,
  city: userInfo.value.city,
  district: userInfo.value.district,
})

// 地区选择
const selectedRegion = ref([])
const regionOptions = regionData

// 初始化地区选择
if (user.province && user.city && user.district) {
  selectedRegion.value = [user.province, user.city, user.district]
}

// 监听地区变化
watch(selectedRegion, (newVal) => {
  if (newVal.length === 3) {
    user.province = newVal[0]
    user.city = newVal[1]
    user.district = newVal[2]
  }
})

// 图片上传
const imageUrl = ref(user.icon)
function handleAvatarSuccess(res, file) {
  if (res.code == 200) {
    imageUrl.value = URL.createObjectURL(file.raw);
    user.icon = res.data.path
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error('图片上传出错，请联系管理员')
  }
}

// 密码强度计算
const passwordStrength = computed(() => {
  if (!userInfo.value.passwordStrength) return '未知'
  
  const strength = userInfo.value.passwordStrength
  if (strength < 30) return '弱'
  if (strength < 70) return '中'
  return '强'
})

const passwordStrengthClass = computed(() => {
  if (!userInfo.value.passwordStrength) return ''
  
  const strength = userInfo.value.passwordStrength
  if (strength < 30) return 'weak'
  if (strength < 70) return 'medium'
  return 'strong'
})

// 弹窗相关
const passwordDialogVisible = ref(false)
const phoneDialogVisible = ref(false)

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordFormRef = ref(null)
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    // { min: 8, message: '密码长度至少8位', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value === passwordForm.oldPassword) {
          callback(new Error('新密码不能与旧密码相同'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 密码强度实时计算
const passwordStrengthLevel = computed(() => {
  const password = passwordForm.newPassword
  if (!password) return 0
  
  let strength = 0
  // 长度
  if (password.length >= 8) strength++
  // 包含数字
  if (/\d/.test(password)) strength++
  // 包含字母
  if (/[a-zA-Z]/.test(password)) strength++
  // 包含特殊字符
  if (/[^a-zA-Z0-9]/.test(password)) strength++
  
  return Math.min(strength, 3)
})

// 手机号表单
const phoneForm = reactive({
  phone: '',
  code: ''
})

const phoneFormRef = ref(null)
const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
  ]
}

// 验证码倒计时
const codeCountdown = ref(0)

// 显示弹窗
const showPasswordDialog = () => {
  passwordDialogVisible.value = true
}

const showPhoneDialog = () => {
  phoneForm.phone = ''
  phoneForm.code = ''
  phoneDialogVisible.value = true
}

// 发送手机验证码
const sendPhoneCode = async () => {
  try {
    await phoneFormRef.value.validateField('phone')
    const res = await sendSms({ 
      cellPhone: phoneForm.phone
    })
    if (res.code === 200) {
      ElMessage.success('验证码发送成功')
      codeCountdown.value = 60
      const timer = setInterval(() => {
        codeCountdown.value--
        if (codeCountdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      ElMessage.error(res.msg || '验证码发送失败')
    }
  } catch (e) {
    console.log('验证失败', e)
  }
}
// 提交密码修改
const submitPasswordChange = async () => {
  try {
    await passwordFormRef.value.validate()
    const res = await updatePassword({
      id:user.id,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      passwordDialogVisible.value = false
      // 清空表单
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      ElMessage.error(res.msg || '密码修改失败')
    }
  } catch (e) {
    console.log('验证失败', e)
  }
}

// 提交手机号修改
const submitPhoneChange = async () => {
  try {
    await phoneFormRef.value.validate()
    const res = await bindPhone({
      cellPhone: phoneForm.phone,
      code: phoneForm.code
    })
    
    if (res.code === 200) {
      ElMessage.success(user.cellPhone ? '手机号修改成功' : '手机号绑定成功')
      phoneDialogVisible.value = false
      // 更新用户信息
      const data = await getUserInfo()
      if (data.code == 200) {
        store.setUserInfo(data.data)
        userInfo.value = data.data
        user.cellPhone = data.data.cellPhone
      }
    } else {
      ElMessage.error(res.msg || (user.cellPhone ? '手机号修改失败' : '手机号绑定失败'))
    }
  } catch (e) {
    console.log('验证失败', e)
  }
}


// 提交更新信息
const updateUserInfoHandle = async () => {
  await updateUserInfo(user)
    .then(async (res) => {
      console.log(res)
      if (res.code == 200) {
        // 重新获取当前登录用户的信息
        const data = await getUserInfo()
        if (data.code == 200) {
            // 记录到store
            store.setUserInfo(data.data)
            userInfo.value = data.data
            ElMessage.success('信息更新成功')
        } else {
        ElMessage.error(res.msg || '信息更新失败')
      }
      } else {
        ElMessage.error(res.msg || '信息更新失败')
      }
    })
};
</script>

<style lang="scss" scoped>
.mySetWrapper {
  .info-form {
    width: 70%;
    
    .form-row {
      gap: 20px;
      margin-bottom: 20px;
    }
    
    .form-item {
      align-items: center;
      margin-bottom: 20px;
      
      .label {
        width: 80px;
        text-align: right;
        margin-right: 10px;
        font-size: 14px;
        color: #606266;
      }
    }
  }
  
  .avatar-upload {
    width: 25%;
    text-align: center;
    
    .avatar-uploader {
      margin-bottom: 10px;
      
      :deep(.el-upload) {
        border: 1px dashed #d9d9d9;
        border-radius: 6px;
        cursor: pointer;
        position: relative;
        overflow: hidden;
        width: 150px;
        height: 150px;
        display: flex;
        justify-content: center;
        align-items: center;
        
        &:hover {
          border-color: #409EFF;
        }
      }
      
      .avatar {
        width: 150px;
        height: 150px;
        display: block;
        object-fit: cover;
      }
      
      .avatar-uploader-icon {
        font-size: 28px;
        color: #8c939d;
        width: 150px;
        height: 150px;
        line-height: 150px;
        text-align: center;
      }
    }
    
    .upload-tip {
      margin-top: 10px;
      color: #409EFF;
      font-size: 14px;
    }
    
    .avatar-tip {
      font-size: 12px;
      color: #909399;
    }
  }
  
  .security-settings {
    .security-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px 20px;
      border-bottom: 1px solid #ebeef5;
      
      .security-content {
        .security-title {
          font-weight: 500;
          margin-bottom: 5px;
        }
        
        .security-info {
          font-size: 14px;
          color: #909399;
          
          .weak {
            color: #f56c6c;
          }
          
          .medium {
            color: #e6a23c;
          }
          
          .strong {
            color: #67c23a;
          }
        }
      }
    }
  }
  
  .password-strength {
    display: flex;
    margin-top: 5px;
    
    span {
      flex: 1;
      text-align: center;
      padding: 2px 0;
      color: #c0c4cc;
      background: #f5f7fa;
      margin-right: 5px;
      font-size: 12px;
      width:30px;
      
      &:last-child {
        margin-right: 0;
      }
      
      &.active {
        color: #fff;
        background: #67c23a;
      }
    }
  }
  
  .verify-code-input {
    display: flex;
    
    .el-input {
      flex: 1;
      margin-right: 10px;
    }
  }
}
</style>