<template>
  <div>
    <!-- 搜索 -->
    <div class="bg-wt radius marg-tp-20">
      <div class="pad-30 searchForm">
        <el-form ref="ruleForm" :inline="true" :model="searchData">
          <el-row :gutter="30">
            <el-col :span="6">
              <el-form-item label="对账状态" prop="reconciliationStatus">
                <div class="el-input">
                  <el-select
                    v-model="searchData.status"
                    clearable
                    placeholder="请选择"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="item in reconciliationStatusData"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="18">
              <div class="btn" >
                <el-button class="button primary" @click="handleSearch">
                  搜索
                </el-button>
                <el-button class="button buttonSub" @click="handleReset(ruleForm)">
                  重置
                </el-button>
                <el-button class="button primary" @click="openChannelManagement" style="float: right;">
                  支付渠道管理
                </el-button>
              </div>
            </el-col>
          </el-row>
        </el-form>
      </div>
    </div>
    <!-- 表格 -->
    <div class="bg-wt radius marg-tp-20">
      <div class="tableBox">
        <el-table :data="baseData.value" border stripe v-loading="loading" :default-sort="{prop:'createTime',order:'descending'}">
          <el-table-column type="index" align="center" width="100" label="序号" />
          <el-table-column label="业务订单号" prop="bizOrderNo" min-width="230" />
          <el-table-column label="支付单号" prop="payOrderNo" min-width="230" />
          <el-table-column label="退款单号" prop="refundOrderNo" min-width="230" />
          <el-table-column label="支付渠道代码" prop="payChannelCode" min-width="120" />
          <el-table-column label="支付金额（元）" min-width="140">
            <template #default="scope" >
              {{scope.row.amount? (scope.row.amount / 100).toFixed(2):  0 }}
            </template>
          </el-table-column>
          <el-table-column label="退款金额（元）" min-width="140">
            <template #default="scope">
              {{scope.row.refundAmount? (scope.row.refundAmount / 100).toFixed(2):0}}
            </template>
          </el-table-column>
          <el-table-column label="对账状态" min-width="120">
            <template #default="scope">
              <span v-if="scope.row.reconciliationStatus === 0">未对账</span>
              <span v-if="scope.row.reconciliationStatus === 1">对账成功</span>
              <span v-if="scope.row.reconciliationStatus === 2">对账失败</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="reconciliationTime"
            label="对账时间"
            min-width="200"
            :formatter="formatTime"
          />
          <el-table-column label="第三方返回业务码" prop="resultCode" min-width="120" />
          <el-table-column label="第三方返回提示信息" prop="resultMsg" min-width="200" />
          <el-table-column
            prop="createTime"
            label="创建时间"
            min-width="200"
            :formatter="formatTime"
          />
          <el-table-column
            prop="updateTime"
            label="更新时间"
            min-width="200"
            :formatter="formatTime"
          />
          <el-table-column
            fixed="right"
            label="操作"
            align="center"
            min-width="70"
            class-name="orderTable"
          >
            <template #default="scope">
              <div class="operate">
                <span class="textDefault" @click="handleCheck(scope.row)">
                  查看
                </span>
              </div>
            </template>
          </el-table-column>
          <!-- 空页面 -->
          <template #empty>
            <EmptyPage :isSearch="isSearch" :baseData="baseData"></EmptyPage>
          </template>
        </el-table>
        <!-- 分页 -->
        <el-pagination
          v-if="total > 10"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :page-sizes="[10, 20, 30, 40]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="Number(total)"
          class="paginationBox"
        ></el-pagination>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { formatTime } from "@/utils/index";
// 空页面
import EmptyPage from "@/components/EmptyPage/index.vue";
// 接口api
import { getReconciliationList } from "@/api/reconciliation";

// 获取父组件值、方法
const props = defineProps({
  // 搜索对象
  searchData: {
    type: Object,
    default: () => ({}),
  },
});

// ------定义变量------
const emit = defineEmits(); //子组件获取父组件事件传值
const router = useRouter(); //获取全局
const route = useRoute(); //获取局部
const loading = ref(false); //加载数据
const ruleForm = ref(); //定义搜索表单的ref
let total = ref(null); //数据总条数
let searchData = reactive({
  pageSize: 10,
  pageNo: 1,
  status: null,
}); //搜索对象
let baseData = reactive([]); //表格数据
let isSearch = ref(false); //是否触发了搜索按钮
const pageSize = ref(10); //每页数量

const reconciliationStatusData = [
  { value: 0, label: "未对账" },
  { value: 1, label: "对账成功" },
  { value: 2, label: "对账失败" },
];

// ------生命周期------
onMounted(() => {
  init();
});

// ------定义方法------
// 获取初始值
const init = () => {
  getList(); //获取对账列表数据
};

// 获取列表值
const getList = async () => {
  loading.value = true;
  await getReconciliationList(searchData)
    .then((res) => {
      if (res.code === 200) {
        loading.value = false;
        baseData.value = res.data.list;
        total.value = res.data.total;
      }
    })
    .catch((err) => {});
};

// 搜索
const handleSearch = () => {
  isSearch.value = true; //是否触发了搜索按钮
  getList(); //刷新列表
};

// 重置搜索表单
const handleReset = (ruleForm) => {
  ruleForm.resetFields();
  searchData.status = null;
  isSearch.value = false;
  getList(); //刷新列表
};

// 设置每页条数
const handleSizeChange = (val) => {
  searchData.pageSize = val;
  // 刷新列表
  getList();
};

// 当前页
const handleCurrentChange = (val) => {
  searchData.pageNo = val;
  // 刷新列表
  getList();
};

// 查看
const handleCheck = (row) => {
  router.push({
    path: "/order/reconciliationDetails/" + row.id,
  });
};

// 打开支付渠道管理页面
const openChannelManagement = () => {
  router.push({
    path: "/order/channel"
  });
};
</script>

<style lang="scss">
.orderTable {
  .cell {
    padding: 0 !important;
  }
}
</style>