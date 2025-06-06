<template>
    <div>
        <div class="bg-wt radius marg-tp-20">
            <!-- 操作按钮区域 -->
            <div class="pad-30  searchForm">
                    <el-button class="button primary" @click="handleAdd">
                        新增
                    </el-button>
                    <el-button class="button primary" @click="handleBack" style="float: right;">
                        返回
                    </el-button>
            </div>
        </div>
        <!-- 表格 -->
        <div class="bg-wt radius marg-tp-20">
            <div class="tableBox">
                <el-table :data="baseData" border stripe v-loading="loading"
                    :default-sort="{ prop: 'createTime', order: 'descending' }">
                    <el-table-column type="index" align="center" width="100" label="序号" />
                    <el-table-column label="渠道名称" prop="name" min-width="200" />
                    <el-table-column label="渠道编码" prop="channelCode" min-width="150" />
                    <el-table-column label="渠道优先级" prop="channelPriority" min-width="150" />
                    <el-table-column label="渠道图标" min-width="150">
                        <template #default="scope">
                            <el-image :src="scope.row.channelIcon" alt=""></el-image>
                        </template>
                    </el-table-column>
                    <el-table-column label="状态" prop="status" min-width="150">
                        <template #default="scope">
                            <el-tag v-if="scope.row.status === 1" type="success">使用中</el-tag>
                            <el-tag v-if="scope.row.status === 2" type="danger">停用</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column fixed="right" label="操作" align="center" min-width="100" class-name="orderTable">
                        <template #default="scope">
                            <div class="operate">
                                <span class="textDefault" @click="handleEdit(scope.row)">
                                    编辑
                                </span>
                            </div>
                        </template>
                    </el-table-column>
                    <template #empty>
                        <EmptyPage :isSearch="isSearch" :baseData="baseData"></EmptyPage>
                    </template>
                </el-table>
            </div>
        </div>
        <!-- 新增/编辑弹窗 -->
        <el-dialog v-model="dialogVisible" title="支付渠道信息">
            <el-form ref="channelForm" :model="channelData" label-width="120px">
                <el-form-item label="支付渠道名称">
                    <el-input v-model="channelData.name"></el-input>
                </el-form-item>
                <el-form-item label="支付渠道编码">
                    <el-input v-model="channelData.channelCode"></el-input>
                </el-form-item>
                <el-form-item label="渠道优先级">
                    <el-input v-model.number="channelData.channelPriority" type="number"></el-input>
                </el-form-item>
                <el-form-item label="渠道图标">
                    <UploadImage @getCoverUrl="(url) => channelData.channelIcon = url"
                        @setUplad="(val) => channelData.channelIcon = ''" :upladImg="channelData.channelIcon"
                        :isCourse="false">
                    </UploadImage>
                </el-form-item>
                <el-form-item label="支付渠道状态">
                    <el-select v-model="channelData.status" placeholder="请选择">
                        <el-option label="使用中" value="1"></el-option>
                        <el-option label="停用" value="2"></el-option>
                    </el-select>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="closeDialog">取消</el-button>
                    <el-button type="primary" @click="saveChannel">保存</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { formatTime } from "@/utils/index";
import EmptyPage from "@/components/EmptyPage/index.vue";
import { listAllPayChannels, addPayChannel, updatePayChannel, getPayChannelById } from "@/api/reconciliation";
import UploadImage from "@/components/UploadImage/index.vue";

const props = defineProps({
    searchData: { type: Object, default: () => ({}) }
});

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const channelForm = ref();
const baseData = ref([]);
const isSearch = ref(false);
const dialogVisible = ref(false);
const channelData = reactive({
    id: null,
    name: null,
    channelCode: null,
    channelPriority: null,
    channelIcon: null,
    status: "1"
});

onMounted(() => init());

const init = () => getList();

const getList = async () => {
    loading.value = true;
    try {
        const res = await listAllPayChannels();
        if (res.code === 200) baseData.value = res.data;
    } catch (err) {
        console.error('获取列表数据失败:', err);
    } finally {
        loading.value = false;
    }
};

const handleAdd = () => {
    resetChannelData();
    dialogVisible.value = true;
};

const handleEdit = async (row) => {
    resetChannelData();
    channelData.id = row.id;
    try {
        const res = await getPayChannelById(row.id);
        if (res.code === 200) {
            const data = res.data;
            channelData.name = data.name;
            channelData.channelCode = data.channelCode;
            channelData.channelPriority = data.channelPriority;
            channelData.channelIcon = data.channelIcon;
            channelData.status = data.status.toString();
        }
    } catch (error) {
        ElMessage.error('获取渠道信息失败');
        console.error('获取渠道信息失败:', error);
    }
    dialogVisible.value = true;
};

// 自定义校验函数
const validateForm = () => {
    if (!channelData.name) {
        ElMessage.error('渠道名称不能为空');
        return false;
    }
    if (channelData.name.length < 2 || channelData.name.length > 50) {
        ElMessage.error('渠道名称长度在 2 到 50 个字符之间');
        return false;
    }
    if (!channelData.channelCode) {
        ElMessage.error('渠道编码不能为空');
        return false;
    }
    if (!/^\w{1,30}$/.test(channelData.channelCode)) {
        ElMessage.error('渠道编码仅允许字母、数字、下划线，长度不超过30位');
        return false;
    }
    if (channelData.channelPriority === null || isNaN(channelData.channelPriority)) {
        ElMessage.error('渠道优先级不能为空且必须为有效数字');
        return false;
    }
    if (!channelData.channelIcon) {
        ElMessage.error('请上传渠道图标');
        return false;
    }
    if (!channelData.status) {
        ElMessage.error('请选择渠道状态');
        return false;
    }
    return true;
};

// 保存渠道信息
const saveChannel = async () => {
    if (validateForm()) {
        try {
            let res;
            if (channelData.id) {
                // 编辑操作
                res = await updatePayChannel(channelData.id, channelData);
                ElMessage.success('渠道信息更新成功');
            } else {
                // 新增操作
                res = await addPayChannel(channelData);
                ElMessage.success('渠道信息新增成功');
            }

            if (res.code !== 200) {
                throw new Error(res.msg || '操作失败');
            }

            dialogVisible.value = false;
            getList();
        } catch (error) {
            ElMessage.error(error.message || '系统异常，请重试');
            console.error('操作失败:', error);
        }
    }
};

const handleBack = () => router.push('/order/reconciliation');

const closeDialog = () => {
    resetChannelData();
    dialogVisible.value = false;
};

const resetChannelData = () => {
    channelData.id = null;
    channelData.name = null;
    channelData.channelCode = null;
    channelData.channelPriority = null;
    channelData.channelIcon = null;
    channelData.status = "1";
};
</script>

<style lang="scss">
.searchForm {
    display: flex;
    justify-content: space-between;
    padding: 20px;
}

.orderTable {
    .cell {
        padding: 0 !important;
    }
}
</style>