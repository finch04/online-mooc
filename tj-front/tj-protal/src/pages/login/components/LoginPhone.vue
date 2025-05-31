<!-- 登录页面 - 手机号 -->
<template>
  <div class="loginPhone">
    <el-form
      ref="formRef"
      :model="fromData"
      :rules="rules"
      label-width="0px"
      class="demo-dynamic"
    >
      <el-form-item prop="cellPhone" label="">
        <el-input v-model="fromData.cellPhone" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item prop="password" label="">
        <div class="fx-sb">
          <el-input 
            v-model="fromData.password" 
            placeholder="请输入验证码" 
            type="number"
          />
          <span 
            class="bt" 
            :class="isSending ? 'bt-grey' : 'bt-primary'" 
            @click="sendVerificationCode"
            :disabled="isSending"
          >
            {{ isSending ? `${countdown}秒后重发` : '获取验证码' }}
          </span> 
        </div>
      </el-form-item>
      <el-form-item class="marg-b-10">
        <div class="fx-sb">
            <div>
                <el-checkbox v-model="fromData.rememberMe" label="7天免登录" size="large" />
            </div>
            <el-link type="primary" @click="goReset">找回密码</el-link>
        </div>
      </el-form-item>
      <el-form-item class="marg-bt-15">
        <div class="bt" @click="submitForm(formRef)">登 录</div>
      </el-form-item>
    </el-form>
    <div class="font-bt text-center"  @click="goRegister">
        去注册
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onUnmounted } from "vue";
import { useRouter } from 'vue-router';
import { phoneLogins, verifycode ,getUserInfo } from "@/api/user"; // 假设登录接口需要验证码
import { useUserStore } from '@/store';
import { ElMessage } from "element-plus";

const store = useUserStore();
const router = useRouter();
const emit = defineEmits(['goHandle']);

// 登录数据初始化
const formRef = ref();
const fromData = reactive({
  cellPhone: "",
  password: "", // 这里存储验证码
  rememberMe: false,
  type: 2 // 可选：标识登录类型
});

// 新增状态变量
const isSending = ref(false); // 倒计时状态
const countdown = ref(60);    // 剩余秒数
let timer = null;             // 定时器引用

// 手机号效验规则
const verifyPhone = (rule, value, callback) => {
  const reg = /^1[3-9]\d{9}$/; // 简化的手机号正则
  if (!value) {
    callback(new Error('请输入手机号'));
  } else if (!reg.test(value)) {
    callback(new Error('请输入正确的手机号'));
  }
  callback();
};

// 表单验证规则
const rules = reactive({
  cellPhone: [
    { validator: verifyPhone, trigger: "blur" },
  ],
  password: [      
    { required: true, message: "请输入验证码", trigger: "blur" },
    { pattern: /^\d{4}$/, message: "请输入验证码", trigger: "blur" }
  ],
});

// 开始倒计时
const startCountdown = () => {
  isSending.value = true;
  countdown.value = 60;
  timer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(timer);
      isSending.value = false;
    }
  }, 1000);
};

// 组件卸载时清除定时器
onUnmounted(() => {
  if (timer) clearInterval(timer);
});

// 发送验证码
const sendVerificationCode = async () => {
  if (isSending.value) return; // 正在倒计时时禁用点击
  
  const reg = /^1[3-9]\d{9}$/;
  if (!fromData.cellPhone || !reg.test(fromData.cellPhone)) {
    ElMessage.error('请输入正确的手机号');
    return;
  }

  try {
    const res = await verifycode({ cellPhone: fromData.cellPhone });
    if (res.code === 200) {
      ElMessage.success('验证码发送成功');
      startCountdown(); // 开始倒计时
    } else {
      ElMessage.error(res.msg || '验证码发送失败');
    }
  } catch (error) {
    ElMessage.error('网络请求失败，请重试');
  }
};

// 登录提交
const submitForm = (formEl) => {
  console.log(fromData)
  if (!formEl) return;
  formEl.validate(async (valid) => {
    if (valid) {
      await phoneLogins(fromData)
			.then(async res => {
				if (res.code === 200) {
          // 用户token写入 pinia
					store.setToken(res.data);
					// 获取用户信息
          const data = await getUserInfo()
          if (data.code === 200) {
              // 记录到store 并调转到首页
              store.setUserInfo(data.data)
					    // 跳转到首页
              router.push('/main/index')
          }
				} else {
          ElMessage({
              message: res.msg,
              type: 'error'
          });
					console.log('登录失败')
				}
			})
			.catch(err => {
        ElMessage({
          message: err,
          type: 'error'
        });
      });
    } else {
      ElMessage({
          message: '登录出错，请重新尝试',
          type: 'error'
      });
     
    }
  });
};

// 跳转注册页面
const goRegister = () => {
  emit('goHandle', 'register');
};

// 找回密码功能
const goReset = () => {
  emit('goHandle', 'reset');
};
</script>

<style lang="scss" scoped>
.loginPhone {
  margin-top: 40px;
  
  .fx-sb {
    position: relative;
    .bt {
      position: absolute;
      right: 10px;
      top: 6px;
      width: 80px;
      height: 28px;
      line-height: 28px;
      text-align: center;
      font-size: 14px;
      cursor: pointer;
      border-radius: 4px;
    }
    
    .bt-grey {
      background-color: #e4e6eb;
      color: #909399;
      cursor: not-allowed;
    }
    
    .bt-primary {
      background-color: #409eff;
      color: #fff;
    }
  }
}
</style>