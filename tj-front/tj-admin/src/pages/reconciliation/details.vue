<template>
    <div class="contentBox">
      <div class="bg-wt radius marg-tp-20">
        <div class="detailBox">
          <!-- 基本信息 -->
          <div class="infoItem">
            <ul>
              <li>
                <div class="tit">ID编号</div>
                <p>
                  业务订单号<span>{{ reconciliationInfo.bizOrderNo }}</span>
                </p>
                <p>
                  支付单号<span>{{ reconciliationInfo.payOrderNo }}</span>
                </p>
                <p>
                  退款单号<span>{{ reconciliationInfo.refundOrderNo }}</span>
                </p>
              </li>
              <li>
                <div class="tit">支付信息</div>
                <p>
                  支付渠道代码<span>{{ reconciliationInfo.payChannelCode }}</span>
                </p>
                <p>
                  支付金额（元）<span>{{reconciliationInfo.amount? (reconciliationInfo.amount / 100).toFixed(2):0 }}</span>
                </p>
                <p>
                  退款金额（元）<span>{{reconciliationInfo.refundAmount ? (reconciliationInfo.refundAmount / 100).toFixed(2):0 }}</span>
                </p>
              </li>
              <li>
                <div class="tit">对账信息</div>
                <p>
                  对账状态<span>
                    <span v-if="reconciliationInfo.reconciliationStatus === 0">未对账</span>
                    <span v-if="reconciliationInfo.reconciliationStatus === 1">对账成功</span>
                    <span v-if="reconciliationInfo.reconciliationStatus === 2">对账失败</span>
                  </span>
                </p>
                <p>
                  对账时间<span>{{ reconciliationInfo.reconciliationTime }}</span>
                </p>
                <p>
                  第三方返回业务码<span>{{ reconciliationInfo.resultCode }}</span>
                </p>
                <p>
                  第三方返回提示信息<span>{{ reconciliationInfo.resultMsg }}</span>
                </p>
              </li>
              <li>
                <div class="tit">时间信息</div>
                <p>
                  创建时间<span>{{ reconciliationInfo.createTime }}</span>
                </p>
                <p>
                  更新时间<span>{{ reconciliationInfo.updateTime }}</span>
                </p>
              </li>
            </ul>
          </div>
        </div>
      </div>
      <div class="bg-wt radius marg-tp-20">
        <div class="BottomBox detailBox">
          <!-- 按钮 -->
          <div class="btn">
            <el-button class="button primary" @click="handleGetback" style="width: 130px">
              返回</el-button
            >
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, onMounted } from "vue";
  import { useRouter, useRoute } from "vue-router";
  import { formatTime } from "@/utils/index";
  // 接口api
  import { getReconciliationDetails } from "@/api/reconciliation";
  
  // ------定义变量------
  const router = useRouter(); //获取全局
  const route = useRoute(); //获取局部
  let reconciliationId = route.params.id ? route.params.id : null; //保存基础信息时后端返回的课程目录
  let reconciliationInfo = reactive({}); //详情数据
  
  // ------生命周期------
  onMounted(() => {
    getDetailData(reconciliationId);
  });
  
  // ------定义方法------
  // 获取详情
  const getDetailData = async (id) => {
    await getReconciliationDetails(id)
      .then((res) => {
        if (res.code === 200) {
          // 使用 Object.assign 保持响应式更新
          Object.assign(reconciliationInfo, res.data);
          console.log("加载完成", reconciliationInfo);
        }
      })
      .catch((err) => { console.log(err) });
  };
  
  // 返回
  const handleGetback = () => {
    router.push({
      path: "/order/reconciliation",
    });
  };
  </script>
  
  <style lang="scss" scoped>
  .bg-wt {
    margin-bottom: 20px;
    .BottomBox {
      padding: 20px 0;
      border-top: 1px solid #ebeef5;
      text-align: center;
      .btn {
        padding-top: 0;
        .button {
          width: 114px;
        }
      }
    }
  }
  
  .detailBox {
    padding-top: 20px;
    padding-right: 30px;
  }
  
  .infoItem {
    margin-top: 30px;
    padding-bottom: 5px;
    ul {
      &:first-child {
        border-bottom: 0;
      }
    }
  }
  </style>