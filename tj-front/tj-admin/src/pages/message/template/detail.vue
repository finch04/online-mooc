<!-- 通知模板详情 -->
<template>
    <div class="contentBox">
      <div class="bg-wt radius marg-tp-20">
        <div class="detailBox" :key="keyTime">
          <!-- 标题 -->
          <div class="tit"><span>通知模板基本信息</span></div>
          <div style="border-top: 1px solid #e5e5e5; margin-bottom: 20px"></div>
          <!-- 基本信息 -->
          <div class="information">
            <div class="informations">
              <div class="title">
                <div class="itemtitle">模板名称</div>
                <div class="item">{{ detailData.name }}</div>
              </div>
              <div class="title">
                <div class="itemtitle">模板代号</div>
                <div class="item">{{ detailData.code }}</div>
              </div>
              <div class="title">
                <div class="itemtitle">通知类型</div>
                <span class="item" v-if="detailData.type === '0'">系统通知</span>
                <span class="item" v-if="detailData.type === '1'">笔记通知</span>
                <span class="item" v-if="detailData.type === '2'">问答通知</span>
                <span class="item" v-if="detailData.type === '3'">其它通知</span>
              </div>
              <div class="title">
                <div class="itemtitle">模板状态</div>
                <span class="item" v-if="detailData.status === '0'">草稿</span>
                <span class="item" v-if="detailData.status === '1'">使用中</span>
                <span class="item" v-if="detailData.status === '2'">停用</span>
              </div>
              <div class="title">
                <div class="itemtitle">通知标题</div>
                <div class="item">{{ detailData.title }}</div>
              </div>
              <div class="title">
                <div class="itemtitle">通知内容</div>
                <div class="item">{{ detailData.content }}</div>
              </div>
              <div class="title">
                <div class="itemtitle">短信模板</div>
                <span class="item" v-if="detailData.isSmsTemplate">是</span>
                <span class="item" v-if="!detailData.isSmsTemplate">否</span>
              </div>
              <!-- 当 isSmsTemplate 为 true 时显示短信模板相关字段 -->
              <template v-if="detailData.isSmsTemplate">
                <div v-for="(messageTemplate, index) in detailData.messageTemplates" :key="index">
                  <div class="title">
                    <div class="itemtitle">短信模板信息({{ index+1 }})</div>
                    <div>
                      <div class="item">第三方短信平台代号: {{ messageTemplate.platformCode }}</div>
                      <div class="item">第三方平台短信签名: {{ messageTemplate.signName }}</div>
                      <div class="item">第三方平台短信模板code: {{ messageTemplate.thirdTemplateCode }}</div>
                      <div class="item" v-if="messageTemplate.status === '0'">短信模板状态: 草稿</div>
                      <div class="item" v-if="messageTemplate.status === '1'">短信模板状态: 使用中</div>
                      <div class="item" v-if="messageTemplate.status === '2'">短信模板状态: 停用</div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>
        <div class="detailBox">
          <!-- 按钮 -->
          <div class="btn">
            <el-button
              v-preventReClick
              class="button primary"
              @click="handleGetback"
              >返回</el-button
            >
          </div>
        </div>
      </div>
    </div>
  </template>
  <script setup>
  import { ref, reactive, onMounted, watch } from "vue";
  import { useRouter, useRoute } from "vue-router";
  import { queryNoticeTemplate } from "@/api/message.js";
  import { ElMessage } from "element-plus";
  
  //初始化路由
  const router = useRouter(); //获取全局
  const route = useRoute(); //获取局部
  const detailId = ref(null); //当前通知模板id
  // 存储通知模板详情数据
  const detailData = reactive({
    name: "",
    code: "",
    type: null,
    status: null,
    title: "",
    content: "",
    isSmsTemplate: false,
    messageTemplates: [],
  });
  // ------生命周期------
  onMounted(() => {
    // 获取当前的id
    detailId.value = route.params.id;
  });
  // 监听获取到id触发获取通知模板详情信息
  watch(detailId, () => {
    gettitleDetail();
  });
  // 获取题目详情信息
  let gettitleDetail = async () => {
    try {
      const res = await queryNoticeTemplate(detailId.value);
      if (res && res.data) {
        detailData.name = res.data.name;
        detailData.code = res.data.code;
        detailData.type = String(res.data.type);
        detailData.status = String(res.data.status);
        detailData.title = res.data.title;
        detailData.content = res.data.content;
        detailData.isSmsTemplate = res.data.isSmsTemplate;
        detailData.messageTemplates = res.data.messageTemplates;
      } else {
        ElMessage.error("获取模板信息失败，返回数据为空");
      }
    } catch (error) {
      ElMessage.error("获取模板信息失败");
    }
  };
  
  // 返回键
  const handleGetback = () => {
    router.push({
      path: "/message/template",
    });
  };
  </script>
  <style lang="scss" scoped>
  .information {
    display: flex;
    .informations {
      margin-left: 32px;
      .title {
        display: flex;
        font-weight: 600;
        font-size: 14px;
        color: #332929;
        letter-spacing: 0; //字间距
        display: flex;
        margin-bottom: 30px;
        &:nth-child(2) {
          margin-bottom: 20px;
          .itemtitle {
            align-self: start;
          }
        }
        .itemtitle {
          min-width: 70px;
          justify-content: end;
          align-self: center;
          text-align: right;
          margin-right: 29px;
          line-height: 28px;
        }
        .item {
          font-weight: 400;
          font-size: 14px;
          color: #332929;
          left: 96px;
          letter-spacing: 0;
          text-align: justify;
          line-height: 28px;
        }
      }
    }
  }
  .btn {
    padding-top: 0px;
  }
  :deep(.detailBox .tit) {
    padding-bottom: 10px;
    span {
      font-size: 18px;
    }
  }
  .button {
    width: 130px;
  }
  .detailBox {
    padding-bottom: 30px;
  }
  </style>