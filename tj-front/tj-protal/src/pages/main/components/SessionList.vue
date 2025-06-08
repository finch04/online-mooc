<template>
    <div class="sessionList">
        <!-- 新增会话按钮 -->
        <span  class="add-button" @click="openModal(null)">新增会话</span>
        <ul>
            <li v-for="session in userSessionList" :key="session.id"
                :class="{ active: selectedSessionId === session.sessionId }"
                @click="selectSession(session.sessionId)"
                @mouseenter="showButtons(session.id)"
                @mouseleave="hideButtons(session.id)">
                {{ session.name }}
                <div class="button-group" :class="{ visible: visibleButtons[session.id] }">
                    <!-- 修改按钮 -->
                    <button class="edit-button" @click="openModal(session.id, $event)">修改</button>
                    <!-- 删除按钮 -->
                    <button class="delete-button" @click="deleteSession(session.id, $event)">删除</button>
                </div>
            </li>
        </ul>
        <!-- 新增/修改会话的模态框 -->
        <el-dialog v-model="isModalVisible" :title="dialogTitle">
            <el-form :model="formData" :rules="formRules" ref="formRef" label-width="80px">
                <el-form-item label="会话名称" prop="name">
                    <el-input v-model="formData.name"></el-input>
                </el-form-item>
                <el-form-item label="会话标签">
                    <el-input v-model="formData.tag"></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="isModalVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitForm">确定</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, reactive, toRefs } from 'vue';
import { ElMessage } from 'element-plus';
import { createUserSession, getUserSessionList, deleteUserSession, updateUserSession } from '@/api/ai.js';

const props = defineProps({
    userSessionList: {
        type: Array,
        default: () => []
    },
    selectedSessionId: {
        type: String,
        default: null
    }
});

const emits = defineEmits(['selectSession', 'createSession', 'deleteSession', 'updateSession']);

// 模态框相关变量
const isModalVisible = ref(false);
const dialogTitle = ref('');
const formData = ref({
    name: '',
    tag: ''
});
const formRef = ref(null);
const formRules = reactive({
    name: [
        { required: true, message: '会话名称为必填项', trigger: 'blur' }
    ]
});
const currentSessionId = ref(null);

// 用于记录哪些会话的按钮需要显示
const visibleButtons = ref({});

// 打开模态框
const openModal = (id, event) => {
    if (event) event.stopPropagation();
    currentSessionId.value = id;
    if (id) {
        // 修改操作
        dialogTitle.value = '修改会话';
        const session = props.userSessionList.find(s => s.id === id);
        if (session) {
            formData.value.name = session.name;
            formData.value.tag = session.tag;
        }
    } else {
        // 新增操作
        dialogTitle.value = '新增会话';
        formData.value = {
            name: '',
            tag: ''
        };
    }
    isModalVisible.value = true;
};

// 提交表单
const submitForm = async () => {
    await formRef.value.validate((valid) => {
        if (valid) {
            if (currentSessionId.value) {
                // 修改操作
                updateSession(currentSessionId.value);
            } else {
                // 新增操作
                createSession();
            }
        }
    });
};

// 新增会话
const createSession = async () => {
    try {
        const res = await createUserSession(formData.value);
        if (res.code === 200) {
            emits('createSession');
            ElMessage.success('创建会话成功');
            isModalVisible.value = false;
        } else {
            ElMessage.error('创建会话失败: ' + (res.msg || '未知错误'));
        }
    } catch (error) {
        console.error('创建会话失败:', error);
        ElMessage.error('创建会话失败: ' + (error.message || '未知错误'));
    }
};

// 删除会话
const deleteSession = async (id, event) => {
    event.stopPropagation();
    try {
        const res = await deleteUserSession(id);
        if (res.code === 200) {
            emits('deleteSession');
            ElMessage.success('删除会话成功');
        } else {
            ElMessage.error('删除会话失败: ' + (res.msg || '未知错误'));
        }
    } catch (error) {
        console.error('删除会话失败:', error);
        ElMessage.error('删除会话失败: ' + (error.message || '未知错误'));
    }
};

// 选择会话
const selectSession = (sessionId) => {
    emits('selectSession', sessionId);
};

// 修改会话
const updateSession = async (id) => {
    try {
        const res = await updateUserSession(id, formData.value);
        if (res.code === 200) {
            isModalVisible.value = false;
            emits('updateSession');
            ElMessage.success('修改会话成功');
        } else {
            ElMessage.error('修改会话失败: ' + (res.msg || '未知错误'));
        }
    } catch (error) {
        console.error('修改会话失败:', error);
        ElMessage.error('修改会话失败: ' + (error.message || '未知错误'));
    }
};

// 显示按钮
const showButtons = (id) => {
    visibleButtons.value[id] = true;
};

// 隐藏按钮
const hideButtons = (id) => {
    visibleButtons.value[id] = false;
};
</script>
<style lang="scss" src="../index.scss"> </style>
<style scoped>
.sessionList {
    width: 200px;
    padding: 20px;
    background-color: white;

    display: flex;
    flex-direction: column;
    align-items: center; 

    .add-button {
        background-color: #007BFF;
        color: white;
        border: none;
        padding: 8px 16px;
        border-radius: 4px;
        cursor: pointer;
        margin-bottom: 10px;
        transition: background-color 0.3s ease;
        align-self: center; 

        &:hover {
            background-color: #0056b3;
        }
    }

    ul {
        list-style-type: none;
        padding: 0;
        width: 100%;

        li {
            margin-bottom: 5px;
            cursor: pointer;
            position: relative;

            &.active {
                font-weight: bold;
            }

            .button-group {
                display: inline-block;
                margin-left: 10px;
                position: absolute;
                right: 0;
                top: 50%;
                transform: translateY(-50%);
                opacity: 0;
                visibility: hidden;
                transition: opacity 0.3s ease, visibility 0.3s ease;

                &.visible {
                    opacity: 1;
                    visibility: visible;
                }

                .edit-button {
                    background-color: #28a745;
                    color: white;
                    border: none;
                    padding: 4px 8px;
                    border-radius: 4px;
                    cursor: pointer;
                    margin-right: 5px;
                    transition: background-color 0.3s ease;

                    &:hover {
                        background-color: #218838;
                    }
                }

                .delete-button {
                    background-color: #dc3545;
                    color: white;
                    border: none;
                    padding: 4px 8px;
                    border-radius: 4px;
                    cursor: pointer;
                    transition: background-color 0.3s ease;

                    &:hover {
                        background-color: #c82333;
                    }
                }
            }
        }
    }
}
</style>