<template>
  <div class="loginPass">
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
        <el-input v-model="fromData.password" placeholder="请输入密码" />
      </el-form-item>
      <el-form-item prop="code" label="">
        <div class="fx-sb">
          <el-input v-model="fromData.code" placeholder="请确认短信验证码" />
          <span 
            class="bt" 
            :class="isSending ? 'bt-grey' : 'bt-primary'" 
            @click="verifycodeHandle"
            :disabled="isSending"
          >
            {{ isSending ? `${countdown}秒后重发` : '发送验证码' }}
          </span> 
        </div>
      </el-form-item>
      <el-form-item class="marg-bt-15">
        <div class="bt" @click="submitForm(formRef)">注册</div>
      </el-form-item>
    </el-form>
    <div class="font-bt text-center" @click="goLogin()">
        去登录
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onUnmounted } from "vue";
import { useRouter } from 'vue-router'
import { userRegist, verifycode } from "@/api/user";
import { useUserStore } from '@/store'
import { ElMessage } from "element-plus";

const store = useUserStore();
const router = useRouter()

const emit = defineEmits(['goHandle'])
// 登录数据初始化
const formRef = ref();
const fromData = reactive({
  cellPhone: "",
  password: "",
  code: ""
});

// 新增状态变量
const isSending = ref(false); // 是否正在倒计时
const countdown = ref(60);    // 倒计时秒数
let timer = null;             // 定时器引用

// 手机号效验
const verifyPone = (rule, value, callback) => {
  const reg = /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/
  if (value == '') {
    callback(new Error('请输入手机号'));
  } else if(!reg.test(value)){
    callback(new Error('请输入正确的手机号'));
  }
  callback()
}

// 效验
const rules = reactive({
  cellPhone: [
    { validator: verifyPone, trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur"},
  ],
  code: [
    { required: true, message: "请输入短信验证码", trigger: "blur"},
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
const verifycodeHandle = async() => {
  // 如果正在倒计时，直接返回
  if (isSending.value) return;
  
  const reg = /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/
  if (fromData.cellPhone == "" || !reg.test(fromData.cellPhone)){
    ElMessage({
              message: '请输入正确的手机号',
              type: 'error'
          });
    return 
  }
  
  // 发送验证码
  await verifycode({cellPhone:fromData.cellPhone})
    .then(async res => {
      if (res.code == 200) {
        ElMessage({
          message: '验证码发送成功',
          type: 'success'
        });
        
        // 开始倒计时
        startCountdown();
      } else {
        ElMessage({
          message: res.msg,
          type: 'error'
        });
        console.log('验证码发送失败')
      }
    })
    .catch(err => {
      ElMessage({
        message: '网络错误，请稍后重试',
        type: 'error'
      });
    });
}

// 注册
const submitForm = (formEl) => {
  if (!formEl) return;
  formEl.validate( async (valid) => {
    if (valid) {
      // 提交注册 
      await userRegist(fromData)
        .then(async res => {
          if (res.code == 200) {
            ElMessage({
              message: '注册成功！请登录',
              type: 'success'
            });
            setTimeout(() => {
              emit('goHandle', 'pass')
            }, 500)
          } else {
            ElMessage({
              message: res.msg,
              type: 'error'
            });
          }
        })
        .catch(err => {});
    }
  });
};

// 去登陆
const goLogin = () => {
  emit('goHandle', 'pass')
}
</script>

<style lang="scss" scoped>
.loginPass {
    margin-top: 40px;
    .fx-sb {
      position: relative;
      .bt {
        position: absolute;
        width: 80px;
        height: 28px;
        line-height: 28px;
        font-size: 14px;
        right: 10px;
        top: 6px;
        text-align: center;
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