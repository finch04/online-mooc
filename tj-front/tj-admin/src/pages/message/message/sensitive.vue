<template>
    <div>
        <div class="bg-wt radius marg-tp-20">
            <!-- 操作按钮区域 -->
            <div class="pad-30 searchForm">
                <el-form ref="ruleFormRef" :inline="true" :model="searchForm">
                    <el-row :gutter="30">
                        <el-col :span="6">
                            <el-form-item label="敏感词关键字" prop="keyword">
                                <div class="el-input">
                                    <el-input
                                        placeholder="请输入"
                                        clearable
                                        v-model="searchForm.keyword"
                                        @clear="handleSearch"
                                        @keyup.enter="handleSearch"
                                    />
                                </div>
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                            <div class="btn">
                                <el-button class="button primary" @click="handleSearch">搜索</el-button>
                                <el-button class="button buttonSub" @click="handleReset">重置</el-button>
                            </div>
                        </el-col>
                    </el-row>
                </el-form>
            </div>
        </div>
        <!-- 表格 -->
        <div class="bg-wt radius marg-tp-20">
            <div class="tableBox">
                <div class="subHead pad-30">
                    <el-button class=" button primary" @click="handleAdd" style="margin-bottom: 10px;">
                        新增
                    </el-button>
                </div>
                <el-table :data="baseData" border stripe v-loading="loading"
                    :default-sort="{ prop: 'createTime', order: 'descending' }">
                    <el-table-column type="index" align="center" width="100" label="序号" />
                    <el-table-column label="敏感词" prop="sensitives" min-width="200" />
                    <el-table-column label="创建时间" prop="createTime" min-width="200">
                        <template #default="scope">
                            {{ scope.row.createTime }}
                        </template>
                    </el-table-column>
                    <el-table-column fixed="right" label="操作" align="center" min-width="100" class-name="orderTable">
                        <template #default="scope">
                            <div class="operate">
                                <span class="textDefault" @click="handleEdit(scope.row)">
                                    编辑
                                </span>
                                <span class="textDefault" @click="confirmDelete(scope.row.id)">
                                    删除
                                </span>
                            </div>
                        </template>
                    </el-table-column>
                    <template #empty>
                        <EmptyPage :isSearch="isSearch" :baseData="baseData"></EmptyPage>
                    </template>
                </el-table>
                <div v-if="total > 10">
                    <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                        :current-page="currentPage" :page-sizes="[10, 20, 30]" :page-size="pageSize"
                        layout="total, sizes, prev, pager, next, jumper" :total="Number(total)" 
                        class="paginationBox">
                    </el-pagination>
                </div>
            </div>
        </div>
        <!-- 新增/编辑弹窗 -->
        <el-dialog v-model="dialogVisible" title="敏感词信息">
            <el-form ref="sensitiveForm" :model="sensitiveData" label-width="120px">
                <el-form-item label="敏感词">
                    <el-input v-model="sensitiveData.sensitives"></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="closeDialog">取消</el-button>
                    <el-button type="primary" @click="saveSensitive">保存</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage ,ElMessageBox} from "element-plus";
import EmptyPage from "@/components/EmptyPage/index.vue";
import { listSensitiveWords, saveSensitiveWord, updateSensitiveWord, deleteSensitiveWord } from "@/api/message.js";

const ruleFormRef = ref(null);
const loading = ref(false);
const sensitiveForm = ref();
const baseData = ref([]);
const isSearch = ref(false);
const dialogVisible = ref(false);
const sensitiveData = reactive({
    id: null,
    sensitives: null,
    createTime: null
});
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const searchForm = reactive({
    keyword: ''
});

onMounted(() => init());

const init = () => getList();

const getList = async () => {
    loading.value = true;
    try {
        const query = {
            pageNo: currentPage.value,
            pageSize: pageSize.value,
            name: searchForm.keyword // 传递搜索关键字
        };
        const res = await listSensitiveWords(query);
        if (res.code === 200) {
            baseData.value = res.data.list || []; // 确保data存在
            total.value = res.data.total || 0;
            isSearch.value = !!searchForm.keyword; // 判断是否为搜索状态
        } else {
            ElMessage.error(res.msg || '获取数据失败');
        }
    } catch (err) {
        console.error('获取列表数据失败:', err);
        ElMessage.error('系统异常，请重试');
    } finally {
        loading.value = false;
    }
};

// 搜索功能
const handleSearch = () => {
    currentPage.value = 1; // 重置为第一页
    getList();
};

// 重置表单
const handleReset = () => {
    if (ruleFormRef.value) {
        ruleFormRef.value.resetFields();
    }
    searchForm.keyword = '';
    handleSearch(); // 重置后重新搜索
};

const handleAdd = () => {
    resetSensitiveData();
    dialogVisible.value = true;
};

const handleEdit = async (row) => {
    resetSensitiveData();
    sensitiveData.id = row.id;
    sensitiveData.sensitives = row.sensitives;
    sensitiveData.createTime = row.createTime;
    dialogVisible.value = true;
};

// 删除确认对话框
const confirmDelete = (id) => {
    ElMessageBox.confirm(
        '确定要删除该敏感词吗？',
        '提示',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(() => {
        handleDelete(id);
    }).catch(() => {
        // 用户取消操作，不执行任何操作
    });
};
const handleDelete = async (id) => {
    try {
        const res =  await deleteSensitiveWord(id);
        if (res.code === 200) {
            ElMessage.success('敏感词删除成功');
            getList();
        } else {
            ElMessage.error(res.msg || '删除失败');
        }
    } catch (error) {
        ElMessage.error(error.message || '系统异常，请重试');
        console.error('删除失败:', error);
    }
};

// 自定义校验函数
const validateForm = () => {
    if (!sensitiveData.sensitives) {
        ElMessage.error('敏感词不能为空');
        return false;
    }
    return true;
};

// 保存敏感词信息
const saveSensitive = async () => {
    if (validateForm()) {
        try {
            let res;
            if (sensitiveData.id) {
                // 编辑操作
                res = await updateSensitiveWord(sensitiveData);
                if (res.code !== 200) {
                    ElMessage.error(res.msg || '操作失败');
                }else{
                    ElMessage.success('敏感词信息更新成功');
                }
            } else {
                // 新增操作
                res = await saveSensitiveWord(sensitiveData);
                if (res.code !== 200) {
                    ElMessage.error(res.msg || '操作失败');
                }else{
                    ElMessage.success('敏感词信息新增成功');
                }
            }
            dialogVisible.value = false;
            getList();
        } catch (error) {
            ElMessage.error(error.message || '系统异常，请重试');
            console.error('操作失败:', error);
        }
    }
};

const closeDialog = () => {
    resetSensitiveData();
    dialogVisible.value = false;
};

const resetSensitiveData = () => {
    sensitiveData.id = null;
    sensitiveData.sensitives = null;
    sensitiveData.createTime = null;
};

const handleSizeChange = (newSize) => {
    pageSize.value = newSize;
    getList();
};

const handleCurrentChange = (newPage) => {
    currentPage.value = newPage;
    getList();
};
</script>

<style lang="scss" scoped>
@import '../index.scss';

.btn {
    .button {
        display: flex;
        float: left
    }
}

:deep(.el-input__prefix-inner) {
    display: none;
}

:deep(.is-guttered) {
    min-width: 240px;
}

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

.flex {
    display: flex;
}

.items-center {
    align-items: center;
}

.ml-10 {
    margin-left: 10px;
}
</style>