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
                                        <el-option label="停用" value="0"></el-option>
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
                <div class="subHead pad-30">
                    <el-button class="button primary" style="margin-bottom: 10px;" @click="handleAddTemplate">新增模板</el-button>
                    <el-button class="button primary" style="margin-bottom: 10px;float: right;" @click="goBack">返回</el-button>
                </div>
                <el-table :data="messageTemplates.data" border stripe v-loading="loading" :default-sort="{prop: 'id', order: 'descending'}">
                    <el-table-column type="index" align="center" width="100" label="序号" />
                    <el-table-column label="模板ID" prop="id" min-width="230"></el-table-column>
                    <el-table-column label="第三方渠道代号" prop="platformCode" min-width="230"></el-table-column>
                    <!-- <el-table-column label="第三方渠道名称" prop="platformName" min-width="230"></el-table-column> -->
                    <el-table-column label="模板名称" prop="name" min-width="230"></el-table-column>
                    <el-table-column label="模板预览内容" prop="content" min-width="230"></el-table-column>
                    <el-table-column label="短信签名" prop="signName" min-width="200"></el-table-column>
                    <el-table-column label="第三方模板code" prop="thirdTemplateCode" min-width="200"></el-table-column>
                    <el-table-column label="模板状态" min-width="120">
                        <template #default="scope">
                            <!-- 根据状态设置不同颜色的标签 -->
                            <el-tag :type="getTagType(scope.row.status)">{{ getTemplateStatus(scope.row.status) }}</el-tag>
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
                                <span class="textDefault" @click="handleEditTemplate(scope.row.id)">编辑</span>
                            </div>
                        </template>
                    </el-table-column>
                    <template #empty>
                        <EmptyPage :isSearch="isSearch" :baseData="messageTemplates.data"></EmptyPage>
                    </template>
                </el-table>
                <el-pagination
                    v-if="messageTemplates.total > 10"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                    :page-sizes="[10, 20, 30, 40]"
                    :page-size="searchForm.pageSize"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="Number(messageTemplates.total)"
                    class="paginationBox"
                >
                </el-pagination>
            </div>
        </div>
        <el-dialog v-model="dialogVisible" title="短信模板信息" class="detailBox">
            <el-form :model="templateForm" ref="templateFormRef" :rules="rules" label-width="160px" label-position="right">
                <el-form-item label="短信平台代号" prop="platformCode">
                    <el-input v-model="templateForm.platformCode"></el-input>
                </el-form-item>
                <el-form-item label="平台短信签名" prop="signName">
                    <el-input v-model="templateForm.signName"></el-input>
                </el-form-item>
                <el-form-item label="平台短信模板code" prop="thirdTemplateCode">
                    <el-input v-model="templateForm.thirdTemplateCode"></el-input>
                </el-form-item>
                <el-form-item label="模板状态" prop="status">
                    <el-select v-model="templateForm.status" placeholder="请选择模板状态">
                        <el-option label="停用" value="0"></el-option>
                        <el-option label="启用" value="1"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="短信模板名称" prop="name">
                    <el-input v-model="templateForm.name"></el-input>
                </el-form-item>
                <el-form-item label="模板内容预览" prop="content">
                    <el-input v-model="templateForm.content" type="textarea"></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSaveTemplate">保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import {
    saveMessageTemplate,
    updateMessageTemplate,
    queryMessageTemplates,
    queryMessageTemplate
} from '@/api/message.js';
import { ElMessage } from 'element-plus';
import EmptyPage from "@/components/EmptyPage/index.vue";
import { useRouter } from 'vue-router';

const router = useRouter();

const searchForm = reactive({
    pageNo: 1,
    pageSize: 10,
    platformCode: null,
    status: null,
    keyword: ''
});

const messageTemplates = ref({
    data: [],
    total: 0
});

const dialogVisible = ref(false);
const templateForm = ref({
    id: null,
    platformCode: '',
    signName: '',
    thirdTemplateCode: '',
    status: null,
    name: '',
    content: ''
});
const templateFormRef = ref(null);

const loading = ref(false);
const isSearch = ref(false);
const ruleFormRef = ref(null);

// 定义表单验证规则
const rules = reactive({
    platformCode: [
        { required: true, message: '第三方短信平台代号不能为空', trigger: 'blur' }
    ],
    signName: [
        { required: true, message: '第三方平台短信签名不能为空', trigger: 'blur' }
    ],
    thirdTemplateCode: [
        { required: true, message: '第三方平台短信模板code不能为空', trigger: 'blur' }
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
        const res = await queryMessageTemplates(searchForm);
        messageTemplates.value.data = res.data.list;
        messageTemplates.value.total = res.data.total;
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

const handleAddTemplate = () => {
    templateForm.value = {
        id: null,
        platformCode: '',
        signName: '',
        thirdTemplateCode: '',
        status: null,
        name: '',
        content: ''
    };
    // 重置表单验证状态
    if (templateFormRef.value) {
        templateFormRef.value.clearValidate();
    }
    dialogVisible.value = true;
};

const handleEditTemplate = async (id) => {
    try {
        const res = await queryMessageTemplate(id);
        if (res && res.data) {
            templateForm.value = res.data;
            templateForm.value.status = templateForm.value.status.toString();
            // 重置表单验证状态
            if (templateFormRef.value) {
                templateFormRef.value.clearValidate();
            }
            dialogVisible.value = true;
        } else {
            ElMessage.error('获取模板信息失败，返回数据为空');
        }
    } catch (error) {
        ElMessage.error('获取模板信息失败');
    }
};

const handleSaveTemplate = async () => {
    templateFormRef.value.validate((valid) => {
        if (valid) {
            (async () => {
                try {
                    if (templateForm.value.id) {
                        await updateMessageTemplate(templateForm.value, templateForm.value.id);
                        ElMessage.success('更新模板成功');
                        handleSearch();
                    } else {
                        await saveMessageTemplate(templateForm.value);
                        ElMessage.success('新增模板成功');
                        handleSearch();
                    }
                    dialogVisible.value = false;
                    handleSearch();
                } catch (error) {
                    ElMessage.error('保存模板失败');
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
    searchForm.platformCode = null;
    handleSearch();
};

onMounted(() => {
    handleSearch();
});

const getTemplateStatus = (status) => {
    switch (status) {
        case 0:
            return '禁用';
        case 1:
            return '启用';
        default:
            return '未知状态';
    }
};

const getTagType = (status) => {
    switch (status) {
        case 0:
            return 'info';
        case 1:
            return 'success';
        case 2:
            return 'danger';
        default:
            return 'info';
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

.detailBox {
    width: 600px; // 调整对话框宽度
    .el-form {
        display: flex;
        flex-wrap: wrap;
        .el-form-item {
            width: 50%; // 每个表单元素占据一半宽度
            box-sizing: border-box;
            padding: 0 10px;
        }
        .el-form-item:last-child {
            width: 100%; // 最后一个元素占据全宽
        }
    }
}

// 确保标签靠右对齐
.detailBox .el-form-item__label {
    text-align: right;
    float: right;
    padding-right: 10px; // 可以根据需要调整右边距
}

.search-box {
    display: flex;
    align-items: center;
}
</style>