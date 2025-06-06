<!-- 添加、编辑通知模板 -->
<template>
  <div class="contentBox">
      <div class="bg-wt radius marg-tp-20">
          <div class="detailBox" :key="keyTime">
              <!-- 标题 -->
              <div class="tit"><span>通知模板基本信息</span></div>
              <div class="divider" style="margin-bottom: 20px"></div>
              <!-- 基本信息 -->
              <el-form :model="templateForm" ref="templateFormRef" :rules="rules" label-width="150px" label-position="left" class="form-container">
                  <el-form-item label="模板名称" prop="name">
                      <el-input v-model="templateForm.name" placeholder="请输入模板名称"></el-input>
                  </el-form-item>
                  <el-form-item label="模板代号" prop="code">
                      <el-input v-model="templateForm.code" placeholder="请输入模板代号"></el-input>
                  </el-form-item>
                  <el-form-item label="通知类型" prop="type">
                      <el-select v-model="templateForm.type" placeholder="请选择通知类型">
                          <el-option label="系统通知" value="0"></el-option>
                          <el-option label="笔记通知" value="1"></el-option>
                          <el-option label="问答通知" value="2"></el-option>
                          <el-option label="其它通知" value="3"></el-option>
                      </el-select>
                  </el-form-item>
                  <el-form-item label="模板状态" prop="status">
                      <el-select v-model="templateForm.status" placeholder="请选择模板状态">
                          <el-option label="草稿" value="0"></el-option>
                          <el-option label="使用中" value="1"></el-option>
                          <el-option label="停用" value="2"></el-option>
                      </el-select>
                  </el-form-item>
                  <el-form-item label="通知标题" prop="title">
                      <el-input v-model="templateForm.title" placeholder="请输入通知标题"></el-input>
                  </el-form-item>
                  <el-form-item label="通知内容" prop="content">
                      <el-input v-model="templateForm.content" type="textarea" placeholder="请输入通知内容"></el-input>
                  </el-form-item>
                  <el-form-item label="是否是短信模板">
                      <el-switch v-model="templateForm.isSmsTemplate"></el-switch>
                  </el-form-item>
                  <!-- 当 isSmsTemplate 为 true 时显示短信模板相关字段 -->
                  <template v-if="templateForm.isSmsTemplate">
                      <div v-for="(messageTemplate, index) in templateForm.messageTemplates" :key="index" class="sms-template-item">
                          <div class="sms-template-header">
                              <span>短信模板信息 - 第{{ index + 1 }}个</span>
                              <el-button type="text" size="small" @click="removeMessageTemplate(index)" v-if="templateForm.messageTemplates.length > 1">删除</el-button>
                          </div>
                          <el-row>
                              <el-col :span="12">
                                  <el-form-item label="第三方短信平台代号" :prop="`messageTemplates.${index}.platformCode`" :rules="smsTemplateRules.platformCode">
                                      <el-input v-model="messageTemplate.platformCode" placeholder="请输入第三方短信平台代号"></el-input>
                                  </el-form-item>
                              </el-col>
                              <el-col :span="12">
                                  <el-form-item label="第三方平台短信签名" :prop="`messageTemplates.${index}.signName`" :rules="smsTemplateRules.signName">
                                      <el-input v-model="messageTemplate.signName" placeholder="请输入第三方平台短信签名"></el-input>
                                  </el-form-item>
                              </el-col>
                              <el-col :span="12">
                                  <el-form-item label="平台短信模板code" :prop="`messageTemplates.${index}.thirdTemplateCode`" :rules="smsTemplateRules.thirdTemplateCode">
                                      <el-input v-model="messageTemplate.thirdTemplateCode" placeholder="请输入第三方平台短信模板code"></el-input>
                                  </el-form-item>
                              </el-col>
                              <el-col :span="12">
                                  <el-form-item label="短信模板状态" :prop="`messageTemplates.${index}.status`" :rules="smsTemplateRules.status">
                                      <el-select v-model="messageTemplate.status" placeholder="请选择短信模板状态">
                                          <el-option label="停用" value="0"></el-option>
                                          <el-option label="启用" value="1"></el-option>
                                      </el-select>
                                  </el-form-item>
                              </el-col>
                          </el-row>
                      </div>
                      <el-button @click="addMessageTemplate" class="add-sms-template-btn">添加短信模板</el-button>
                  </template>
              </el-form>
          </div>
          <div class="detailBox" :key="keyTime" style="border-top: 1px solid #e8e8e8; padding: 0;">
              <!-- 按钮 -->
              <div class="btn">
                  <el-button class="button buttonSub" @click="handleCancel">取消</el-button>
                  <el-button
                          class="button buttonSub"
                          v-preventReClick
                          @click="handleSubmit('getback')"
                  >保存并返回</el-button>
                  <el-button
                          v-preventReClick
                          class="button primary"
                          @click="handleNext"
                  >保存并继续</el-button>
              </div>
          </div>
      </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watchEffect } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { saveNoticeTemplate, updateNoticeTemplate, queryNoticeTemplate } from "@/api/message.js";

// 初始化路由
const router = useRouter();
const route = useRoute();

const templateForm = ref({
  id: null,
  name: '',
  code: '',
  type: null,
  status: null,
  title: '',
  content: '',
  isSmsTemplate: false,
  messageTemplates: [
      {
          platformCode: '',
          signName: '',
          thirdTemplateCode: '',
          status: null
      }
  ],
  deleteMessageTemplates: []
});
const templateFormRef = ref(null);

let keyTime = ref(null);  // 解决组件强制重新加载的问题

// 表单校验规则
const rules = reactive({
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入模板代号', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择通知类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择模板状态', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入通知标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入通知内容', trigger: 'blur' }
  ]
});

// 短信模板校验规则
const smsTemplateRules = reactive({
  platformCode: [
    { required: true, message: '请输入第三方短信平台代号', trigger: 'blur' }
  ],
  signName: [
    { required: true, message: '请输入第三方平台短信签名', trigger: 'blur' }
  ],
  thirdTemplateCode: [
    { required: true, message: '请输入第三方平台短信模板code', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择短信模板状态', trigger: 'change' }
  ]
});

// 自定义校验函数：当 isSmsTemplate 为 true 时，至少存在一个短信模板
const validateSmsTemplates = (rule, value, callback) => {
  if (templateForm.value.isSmsTemplate && templateForm.value.messageTemplates.length === 0) {
    callback(new Error('选择是短信模板时，至少需要存在一个短信模板'));
  } else {
    callback();
  }
};

// 添加自定义校验规则
rules.messageTemplates = [
  { validator: validateSmsTemplates, trigger: 'change' }
];

// ------生命周期------
onMounted(async () => {
  if (route.params.id === 'null') {
      // 新增直接不管
      return;
  }
  // 如果是编辑页面，可根据路由参数获取详情数据
  if (route.params.id) {
      templateForm.value.id = route.params.id;
      try {
          const res = await queryNoticeTemplate(route.params.id);
          if (res && res.data) {
              templateForm.value.name = res.data.name;
              templateForm.value.code = res.data.code;
              templateForm.value.type = String(res.data.type);
              templateForm.value.status = String(res.data.status);
              templateForm.value.title = res.data.title;
              templateForm.value.content = res.data.content;
              templateForm.value.isSmsTemplate = res.data.isSmsTemplate;
              templateForm.value.messageTemplates = res.data.messageTemplates || [];
              if(templateForm.value.messageTemplates.length > 0){
                templateForm.value.messageTemplates.forEach((item) => {
                  item.status = String(item.status);
                });
              }
          } else {
              ElMessage.error("获取模板信息失败，返回数据为空");
          }
      } catch (error) {
          ElMessage.error("获取模板信息失败");
      }
  }
});

// 保存并继续时返回到重新添加页面，这时候会有残余的数据，所以要强制刷新加载页面
// 值发生改变时说明不是同一个组件，将会进行重新加载和渲染
watchEffect(() => {
  if (route.path === "/message/add/null") {
      keyTime.value = new Date().getTime();
  }
});

// ------定义方法------
// 保存
const handleSubmit = async (str) => {
  templateFormRef.value.validate((valid) => {
    if (valid) {
      try {
        if (templateForm.value.id) {
             updateNoticeTemplate(templateForm.value, templateForm.value.id);
            ElMessage.success('更新模板成功');
        } else {
             saveNoticeTemplate(templateForm.value);
            ElMessage.success('新增模板成功');
        }
        if (str === 'getback') {
            router.push({
                path: "/message/template",
            });
        } else {
            // 清空表单数据
            templateForm.value = {
                id: null,
                name: '',
                code: '',
                type: null,
                status: null,
                title: '',
                content: '',
                isSmsTemplate: false,
                messageTemplates: [
                    {
                        platformCode: '',
                        signName: '',
                        thirdTemplateCode: '',
                        status: null
                    }
                ],
                deleteMessageTemplates: []
            };
        }
      } catch (error) {
          ElMessage.error('保存模板失败');
      }
    } else {
      ElMessage.error('表单校验不通过，请检查输入内容');
    }
  });
};

// 下一步
const handleNext = () => {
  handleSubmit();
};

// 取消
const handleCancel = () => {
  router.push({
      path: "/message/template",
  });
};

const addMessageTemplate = () => {
  templateForm.value.messageTemplates.push({
      platformCode: '',
      signName: '',
      thirdTemplateCode: '',
      status: null
  });
};

const removeMessageTemplate = (index) => {
  if (templateForm.value.messageTemplates[index].id) {
      templateForm.value.deleteMessageTemplates.push(templateForm.value.messageTemplates[index].id);
  }
  templateForm.value.messageTemplates.splice(index, 1);
};
</script>

<style lang="scss" scoped>
.contentBox {
  padding: 20px;
}

.bg-wt {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.tit {
  font-size: 18px;
  font-weight: bold;
  padding: 20px;
}

.divider {
  border-top: 1px solid #e5e5e5;
}

.form-container {
  padding: 0 20px 20px;
}

.sms-template-item {
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 10px;
}

.sms-template-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.add-sms-template-btn {
  margin-top: 10px;
}

.btn {
  padding: 30px 0;
  text-align: center;
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  color: #332929;
}

:deep(.numText) {
  color: #B5ABAB;
}

.el-input__wrapper {
  :deep(.el-input__inner) {
      color: #332929;

      &::placeholder {
          color: #999;
      }
  }
}

:deep(.el-input .el-input__wrapper .el-input__inner) {
  color: #332929;
}

.btn .button {
  width: 130px;
  margin: 0 10px;
}
</style>