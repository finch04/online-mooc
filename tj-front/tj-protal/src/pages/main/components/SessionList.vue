<template>
    <div class="sessionList">
        <!-- 新增会话按钮 -->
        <el-button style="margin-bottom: 20px;text-align: center;color: white;" @click="openModal(null)">新增会话</el-button>
        <ul class="session-ul">
            <li v-for="session in userSessionList" :key="session.id"
                :class="{ 'active-session': selectedSessionId === session.sessionId }"
                @click="selectSession(session.sessionId)" 
                class="session-item">
                <!-- 会话内容区域 -->
                <div class="session-content">
                    <!-- 会话名称 -->
                    <div class="session-name">
                        {{ session.name }}
                        <!-- 会话标签 -->
                        <div class="session-tag" v-if="session.tag">#{{ session.tag }}</div>
                    </div>
                </div>

                <!-- 操作按钮组 -->
                <div class="button-group">
                    <el-button 
                        type="primary" 
                        class="edit-button" 
                        :icon="Edit" 
                        circle 
                        @click="openModal(session.id)"
                    ></el-button>
                    <el-button 
                        type="warning"
                        class="delete-button" 
                        :icon="Delete" 
                        circle 
                        @click="confirmDelete(session.id)"
                    ></el-button>
                </div>
            </li>
        </ul>

        <!-- 模态框 -->
        <el-dialog v-model="isModalVisible" :title="dialogTitle" @close="handleModalClose">
            <el-form :model="formData" :rules="formRules" ref="formRef" label-width="80px">
                <el-form-item label="会话名称" prop="name">
                    <el-input 
                        v-model="formData.name" 
                        :maxlength="8" 
                        @input="handleInputLimit('name')"
                    ></el-input>
                </el-form-item>
                <el-form-item label="会话标签">
                    <el-input 
                        v-model="formData.tag" 
                        :maxlength="8" 
                        @input="handleInputLimit('tag')"
                    ></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer btn">
                    <el-button class="bt-round bt-red" @click="isModalVisible = false" style="color:white">取消</el-button>
                    <el-button class="bt-round" type="primary" @click="submitForm">确定</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 删除确认对话框 -->
        <el-dialog 
            v-model="deleteConfirmVisible" 
            title="删除确认" 
            @close="handleDeleteCancel"
        >
            <p>确定要删除该会话吗？</p>
            <template #footer>
                <el-button @click="deleteConfirmVisible = false" style="color:white">取消</el-button>
                <el-button type="danger" @click="handleDeleteConfirm">删除</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, reactive } from 'vue';
import { ElMessage, ElDialog } from 'element-plus';
import { createUserSession, deleteUserSession, updateUserSession } from '@/api/ai.js';
import { Edit, Delete } from '@element-plus/icons-vue'; 

const props = defineProps({
    userSessionList: { type: Array, default: () => [] },
    selectedSessionId: { type: String, default: null }
});

const emits = defineEmits(['selectSession', 'createSession', 'deleteSession', 'updateSession']);

// 模态框相关变量
const isModalVisible = ref(false);
const dialogTitle = ref('');
const formData = ref({ name: '', tag: '' });
const formRef = ref(null);
const formRules = reactive({ 
    name: [
        { required: true, message: '会话名称为必填项', trigger: 'blur' }
    ]
});
const currentSessionId = ref(null);

// 删除确认对话框
const deleteConfirmVisible = ref(false);
const deletingSessionId = ref(null);

// 关闭模态框时清除校验状态
const handleModalClose = () => {
    formRef.value?.resetFields(); // 清除表单校验状态
    formData.value = { name: '', tag: '' }; // 清空输入内容
};

// 输入长度限制处理
const handleInputLimit = (field) => {
    if (field === 'name') {
        if (formData.value.name.length > 8) {
            formData.value.name = formData.value.name.slice(0, 8);
        }
    } else if (field === 'tag') {
        if (formData.value.tag.length > 5) {
            formData.value.tag = formData.value.tag.slice(0, 5);
        }
    }
};

// 打开模态框
const openModal = (id) => {
    currentSessionId.value = id;
    dialogTitle.value = id ? '修改会话' : '新增会话';
    if (id) {
        const session = props.userSessionList.find(s => s.id === id);
        session && (formData.value = { ...session });
    } else {
        formData.value = { name: '', tag: '' };
    }
    isModalVisible.value = true;
};

// 提交表单
const submitForm = () => {
    formRef.value.validate(valid => {
        if (valid) {
            currentSessionId.value ? updateSession(currentSessionId.value) : createSession();
        }
    });
};

// 新增会话
const createSession = async () => {
    try {
        const res = await createUserSession(formData.value);
        if (res.code === 200) {
            emits('createSession', res.data.sessionId);
            ElMessage.success('创建会话成功');
            isModalVisible.value = false;
        } else {
            ElMessage.error(`创建失败: ${res.msg || '未知错误'}`);
        }
    } catch (error) {
        ElMessage.error(`创建失败: ${error.message || '网络错误'}`);
    }
};

// 修改会话
const updateSession = async (id) => {
    try {
        const res = await updateUserSession(id, formData.value);
        if (res.code === 200) {
            emits('updateSession');
            ElMessage.success('修改成功');
            isModalVisible.value = false;
        } else {
            ElMessage.error(`修改失败: ${res.msg || '未知错误'}`);
        }
    } catch (error) {
        ElMessage.error(`修改失败: ${error.message || '网络错误'}`);
    }
};

// 删除确认流程
const confirmDelete = (id) => {
    deletingSessionId.value = id;
    deleteConfirmVisible.value = true;
};

const handleDeleteConfirm = async () => {
    try {
        const res = await deleteUserSession(deletingSessionId.value);
        if (res.code === 200) {
            emits('deleteSession');
            ElMessage.success('删除成功');
            deleteConfirmVisible.value = false;
        } else {
            ElMessage.error(`删除失败: ${res.msg || '未知错误'}`);
        }
    } catch (error) {
        ElMessage.error(`删除失败: ${error.message || '网络错误'}`);
    }
};

const handleDeleteCancel = () => {
    deletingSessionId.value = null;
};

// 选择会话
const selectSession = (sessionId) => {
    emits('selectSession', sessionId);
};
</script>

<style scoped>
.sessionList {
    width: 250px;
    padding: 15px;
    background-color: white;
    border-right: 1px solid rgb(249, 249, 249);
    /* border-radius: 8px; */
    /* box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); */
}

.session-ul {
    width: 100%;
    padding: 0;
}

.session-item {
    width: 100%;
    margin-bottom: 10px;
    padding: 12px 15px;
    border: 1px solid gray;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    background-color: white;
    transition: all 0.2s ease;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.session-item:hover {
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.active-session {
    border-color: #409eff;
    background-color: #f5f7fa;
    box-shadow: inset 0 0 0 2px #409eff;
}

.session-content {
    flex-grow: 1;
    margin-right: 15px;
}

.session-name {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
}

.session-tag {
    margin-left: 8px;
    font-size: 12px;
    color: #606266;
    background-color: #f0f2f5;
    padding: 2px 6px;
    border-radius: 12px;
    display: inline-block;
    margin-top: 3px;
}

.button-group {
    display: flex;
    gap: 8px;
}

.el-button.circle {
    width: 32px;
    height: 32px;
    padding: 0;
    border-radius: 50%;
    font-size: 14px;
}

.el-button.edit-button {
    background-color: #409eff;
    color: white;
    
    &:hover {
        background-color: #3583d0;
    }
}

.el-button.delete-button {
    background-color: #dc3545;
    color: white;
    
    &:hover {
        background-color: #c22535;
    }
}

.el-dialog.delete-confirm {
    width: 300px;
}
</style>