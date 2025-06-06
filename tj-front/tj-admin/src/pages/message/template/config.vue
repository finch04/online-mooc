<template>
    <div class="contentBox">
        <!-- 搜索栏 -->
        <div class="bg-wt radius marg-tp-20">
            <div class="pad-30 searchForm">
                <el-form ref="ruleFormRef" :inline="true" :model="searchForm">
                    <el-row :gutter="30">
                        <el-col :span="6">
                            <el-form-item label="名称" prop="keyword">
                                <div class="el-input">
                                    <el-input
                                        placeholder="请输入关键字"
                                        clearable
                                        v-model="searchForm.keyword"
                                    />
                                </div>
                            </el-form-item>
                        </el-col>
                        <el-col :span="6">
                            <el-form-item label="状态" prop="status">
                                <div class="el-input">
                                    <el-select v-model="searchForm.status" placeholder="请选择状态">
                                        <el-option label="禁用" value="0"></el-option>
                                        <el-option label="启用" value="1"></el-option>
                                    </el-select>
                                </div>
                            </el-form-item>
                        </el-col>
                        <el-col :span="6">
                            <div class="btn">
                                <el-button class="button primary" @click="handleSearch">搜索</el-button>
                                <el-button class="button buttonSub" @click="handleReset">重置</el-button>
                            </div>
                        </el-col>
                    </el-row>
                </el-form>
            </div>
        </div>
        <div class="bg-wt radius marg-tp-20">
            <div class="tableBox">
                <div class="subHead pad-30 " >
                    <el-button class="button primary" style="margin-top: 10px;margin-bottom: 10px;" @click="handleAddPlatform">新增平台</el-button>
                    <el-button class="button primary" style="margin-top: 10px;margin-bottom: 10px;float: right;" @click="goBack">返回</el-button>
                </div>
                <el-table :data="smsPlatforms.data" border stripe v-loading="loading" :default-sort="{prop: 'id', order: 'descending'}">
                    <el-table-column type="index" align="center" width="100" label="序号" />
                    <el-table-column label="平台ID" prop="id" min-width="230"></el-table-column>
                    <el-table-column label="平台名称" prop="name" min-width="230"></el-table-column>
                    <el-table-column label="平台代码" prop="code" min-width="200"></el-table-column>
                    <el-table-column label="优先级" prop="priority" min-width="120"></el-table-column>
                    <el-table-column label="平台状态" min-width="120">
                        <template #default="scope">
                            <!-- 根据状态设置不同颜色的标签 -->
                            <el-tag :type="scope.row.status === 0 ? 'danger' : 'success'">{{ getPlatformStatus(scope.row.status) }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column
                        fixed="right"
                        label="操作"
                        align="center"
                        min-width="100"
                        class-name="taskTable"
                    >
                        <template #default="scope">
                            <div class="operate">
                                <span class="textDefault" @click="handleEditPlatform(scope.row.id)">编辑</span>
                            </div>
                        </template>
                    </el-table-column>
                    <template #empty>
                        <EmptyPage :isSearch="isSearch" :baseData="smsPlatforms.data"></EmptyPage>
                    </template>
                </el-table>
                <el-pagination
                    v-if="smsPlatforms.total > 10"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                    :page-sizes="[10, 20, 30, 40]"
                    :page-size="searchForm.pageSize"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="Number(smsPlatforms.total)"
                    class="paginationBox"
                >
                </el-pagination>
            </div>
        </div>
        <el-dialog v-model="dialogVisible" title="平台信息" class="detailBox">
            <el-form :model="platformForm" ref="platformFormRef" :rules="rules" label-width="80px" label-position="right">
                <el-form-item label="平台名称" prop="name">
                    <el-input v-model="platformForm.name"></el-input>
                </el-form-item>
                <el-form-item label="平台代码" prop="code">
                    <el-input v-model="platformForm.code"></el-input>
                </el-form-item>
                <el-form-item label="优先级" prop="priority">
                    <el-input v-model="platformForm.priority" type="number"></el-input>
                </el-form-item>
                <el-form-item label="平台状态">
                    <el-select v-model="platformForm.status" placeholder="请选择平台状态">
                        <el-option label="禁用" value="0"></el-option>
                        <el-option label="启用" value="1"></el-option>
                    </el-select>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSavePlatform">保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import {
    saveSmsThirdPlatform,
    updateSmsThirdPlatform,
    querySmsThirdPlatforms,
    querySmsThirdPlatform
} from '@/api/message.js';
import { ElMessage } from 'element-plus';
import EmptyPage from "@/components/EmptyPage/index.vue";
import { useRouter } from 'vue-router';

const router = useRouter();

const searchForm = reactive({
    pageNo: 1,
    pageSize: 10,
    keyword: '', // 用于搜索平台名称
    status: null // 用于搜索平台状态
});

const smsPlatforms = ref({
    data: [],
    total: 0
});

const dialogVisible = ref(false);
const platformForm = ref({
    id: null,
    name: '',
    code: '',
    priority: null,
    status: null
});
const platformFormRef = ref(null);

const loading = ref(false);
const isSearch = ref(false);
const ruleFormRef = ref(null);

// 定义表单验证规则
const rules = reactive({
    name: [
        { required: true, message: '平台名称不能为空', trigger: 'blur' }
    ],
    code: [
        { required: true, message: '平台代码不能为空', trigger: 'blur' }
    ],
    priority: [
        { required: true, message: '优先级不能为空', trigger: 'blur' }
    ]
});

const goBack = () => {
    router.push({
        path: `/message/template`
    });
};

const handleSearch = async () => {
    isSearch.value = true;
    loading.value = true;
    try {
        const res = await querySmsThirdPlatforms(searchForm);
        smsPlatforms.value.data = res.data.list;
        smsPlatforms.value.total = res.data.total;
        loading.value = false;
    } catch (error) {
        ElMessage.error('查询失败');
        loading.value = false;
    }
};

const handleSizeChange = (newSize) => {
    searchForm.pageSize = newSize;
    handleSearch();
};

const handleCurrentChange = (newPage) => {
    searchForm.pageNo = newPage;
    handleSearch();
};

const handleAddPlatform = () => {
    platformForm.value = {
        id: null,
        name: '',
        code: '',
        priority: null,
        status: null
    };
    // 重置表单验证状态
    if (platformFormRef.value) {
        platformFormRef.value.clearValidate();
    }
    dialogVisible.value = true;
};

const handleEditPlatform = async (id) => {
    try {
        const res = await querySmsThirdPlatform(id);
        if (res && res.data) {
            platformForm.value = res.data;
            platformForm.value.status=  res.data.status.toString();
            // 重置表单验证状态
            if (platformFormRef.value) {
                platformFormRef.value.clearValidate();
            }
            dialogVisible.value = true;
        } else {
            ElMessage.error('获取平台信息失败，返回数据为空');
        }
    } catch (error) {
        ElMessage.error('获取平台信息失败');
    }
};

const handleSavePlatform = async () => {
    platformFormRef.value.validate((valid) => {
        if (valid) {
            (async () => {
                try {
                    if (platformForm.value.id) {
                        await updateSmsThirdPlatform(platformForm.value, platformForm.value.id);
                        ElMessage.success('更新平台成功');
                    } else {
                        await saveSmsThirdPlatform(platformForm.value);
                        ElMessage.success('新增平台成功');
                    }
                    dialogVisible.value = false;
                    handleSearch();
                } catch (error) {
                    ElMessage.error('保存平台失败');
                }
            })();
        } else {
            ElMessage.error('请填写完整信息');
        }
    });
};

const handleReset = () => {
    if (ruleFormRef.value) {
        ruleFormRef.value.resetFields();
    }
    searchForm.keyword = '';
    searchForm.status = null;
    handleSearch();
};

onMounted(() => {
    handleSearch();
});

const getPlatformStatus = (status) => {
    switch (status) {
        case 0:
            return '禁用';
        case 1:
            return '启用';
        default:
            return '未知状态';
    }
};
</script>

<style scoped lang="scss">
@import '../index.scss';

.btn {
    .button {
        display: flex;
        float: left;
    }
}

:deep(.el-input__prefix-inner) {
    display: none;
}

:deep(.is-guttered) {
    min-width: 240px;
}

.taskTable {
    .cell {
        padding: 0 !important;
    }
}

.detailBox .el-form-item__label {
    text-align: right;
}

.search-box {
    display: flex;
    align-items: center;
}
</style>