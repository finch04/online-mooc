<template>
    <div class="resetPassword">
      <el-form autocomplete="off"
        ref="formRef"
        :model="fromData"
        :rules="rules"
        label-width="0px"
        class="demo-dynamic"
      >
        <el-form-item prop="cellPhone" label="">
          <el-input 
            v-model="fromData.cellPhone" 
            placeholder="请输入注册手机号" 
            @blur="validatePhone"
          />
        </el-form-item>
        
        <el-form-item prop="code" label="" >
          <div class="fx-sb">
            <el-input 
              v-model="fromData.code" 
              placeholder="请输入短信验证码" 
              type="number"
            />
            <span 
              class="bt" 
              :class="isSending ? 'bt-grey' : 'bt-primary'" 
              @click="sendVerificationCode"
              :disabled="isSending"
            >
              {{ isSending ? `${countdown}秒后重发` : '发送验证码' }}
            </span> 
          </div>
        </el-form-item>
        
        <el-form-item prop="newPassword" label="" readonly>
          <el-input 
            v-model="fromData.newPassword" 
            placeholder="请设置新密码" 
            show-password
            type="password"
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword" label="">
          <el-input 
            v-model="fromData.confirmPassword" 
            placeholder="请确认新密码" 
            show-password
            type="password"
          />
        </el-form-item>
        
        <el-form-item class="marg-bt-15">
          <div class="bt" @click="submitForm(formRef)">重置密码</div>
        </el-form-item>
      </el-form>
      
      <div class="font-bt text-center" @click="goLogin">
        返回到登录
      </div>
    </div>
  </template>
  
  <script setup>
  import { reactive, ref, onUnmounted } from "vue";
  import { useRouter } from "vue-router";
  import { verifycode,resetPassword } from "@/api/user"; // 假设的接口
  import { ElMessage } from "element-plus";
  
  const router = useRouter();
  const emit = defineEmits(["goHandle"]);
  
  // 表单数据
  const formRef = ref();
  const fromData = reactive({
    cellPhone: "",
    code: "", // 验证码
    newPassword: "", // 新密码
    confirmPassword: "" // 确认密码
  });
  
  // 倒计时状态
  const isSending = ref(false);
  const countdown = ref(60);
  let timer = null;
  
  // 手机号验证规则
  const validatePhone = (rule, value, callback) => {
    const reg = /^1[3-9]\d{9}$/;
    if (!value) {
      callback(new Error("请输入手机号"));
    } else if (!reg.test(value)) {
      callback(new Error("请输入正确的手机号"));
    }
    callback();
  };
  
  // 表单验证规则
  const rules = reactive({
    cellPhone: [
      { validator: validatePhone, trigger: "blur" }
    ],
    code: [
      { required: true, message: "请输入验证码", trigger: "blur" },
      { pattern: /^\d{4}$/, message: "请输入验证码", trigger: "blur" }
    ],
    newPassword: [
      { required: true, message: "请设置新密码", trigger: "blur" },
    ],
    confirmPassword: [
      { required: true, message: "请确认新密码", trigger: "blur" },
      { validator: (rule, value) => value === fromData.newPassword || "两次密码不一致", trigger: "blur" }
    ]
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
    if (isSending.value) return;
    
    // 验证手机号
    const reg = /^1[3-9]\d{9}$/;
    if (!fromData.cellPhone || !reg.test(fromData.cellPhone)) {
      ElMessage.error("请输入正确的手机号");
      return;
    }
  
    try {
      const res = await verifycode({ cellPhone: fromData.cellPhone }); // 调用发送验证码接口
      if (res.code === 200) {
        ElMessage.success("验证码发送成功");
        startCountdown();
      } else {
        ElMessage.error(res.msg || "验证码发送失败");
      }
    } catch (error) {
      ElMessage.error("网络请求失败，请重试");
    }
  };
  
  // 提交重置密码
  const submitForm = (formEl) => {
    if (!formEl) return;
    formEl.validate(async (valid) => {
      if (valid) {
        try {
          // 调用重置密码接口
          const res = await resetPassword({
            cellPhone: fromData.cellPhone,
            code: fromData.code,
            password: fromData.newPassword
          });
          
          if (res.code === 200) {
            ElMessage.success("密码重置成功，请重新登录");
            setTimeout(() => {
              goLogin() // 跳转登录页
            }, 1000);
          } else {
            ElMessage.error(res.msg);
          }
        } catch (error) {
          ElMessage.error("密码重置失败，请检查信息");
        }
      }
    });
  };
  
  // 返回登录页
  const goLogin = () => {
  emit('goHandle', 'pass');
};
  </script>
  
  <style lang="scss" scoped>
  .resetPassword {
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
    
    .el-input__inner {
      height: 40px;
      font-size: 14px;
    }
}
  </style>