<template>
    <div class="aiChatWrapper" @click="handleGlobalClick">
        <div class="container" style="display: flex;">
            <Breadcrumb data="AI知识库"/>
        </div>
        <div class="chatLayout fx">
            <!-- 文件列表 -->
            <FileList 
                :fileList="fileList"
                :selectedFileId="selectedFileId"
                :currentPage="currentPage"
                :pageSize="pageSize"
                :totalFiles="totalFiles"
                @open-modal="openModal"
                @select-file="selectFile"
                @confirm-delete="confirmDelete"
                @view-file-content="viewFileContent"
                @load-more="loadMoreFiles"
            />
            <!-- 聊天区域 -->
            <div class="chatItems container bg-wt ">
                <!-- 聊天消息显示区域 -->
                <div class="chatMessages" ref="chatMessages">
                    <div class="message" v-for="(msg, index) in chatMessagesList" :key="index">
                        <div class="userMessage" v-if="msg.type === 'user'">
                            <div class="messageContent">{{ msg.content }}</div>
                        </div>
                        <div class="assistantMessage" v-else-if="msg.type === 'assistant'">
                            <div class="messageContent" v-html="msg.processedContent"></div>
                        </div>
                        <div class="markdownChunkMessage" v-else-if="msg.type === 'markdownChunk'">
                            <div class="markdownChunkContainer">
                                <div class="markdownChunkHeader">
                                    <div class="markdownChunkTitle">{{ msg.chunk.title }}</div>
                                </div>
                                <div class="markdownChunkContent" v-html="msg.chunk.contentHtml"></div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- 提问区域 -->
                <div class="inputArea fx">
                    <el-input  v-model="question" placeholder="请输入问题" @keyup.enter="askQuestion"
                        :disabled="isLoading" style="margin-right: 10px;" />
                    <button @click="askQuestion" :disabled="isLoading || !question" style="     min-width: 100px;height: 45px;">提问</button>
                </div>
            </div>
        </div>

        <!-- 新增文件模态框 -->
        <el-dialog v-model="isAddModalVisible" title="新增文件" @close="handleAddModalClose">
            <el-form :data="addFormData" :rules="addFormRules" ref="addFormRef" label-width="80px">
                <el-form-item label="文件" prop="file">
                    <el-upload
                        ref="addUploadRef"
                        action="#"
                        :auto-upload="false"
                        :show-file-list="false"
                        :on-change="handleAddFileChange"
                    >
                        <el-button slot="trigger" type="primary">选取文件</el-button>
                    </el-upload>
                    <template>
                        <div class="el-upload__tip">请上传2MB以内的Markdown 文件</div>
                    </template>
                    <div v-if="addFormData.fileName">{{ addFormData.fileName }}</div>
                </el-form-item>
                <el-form-item label="切割等级" prop="level">
                    <el-input-number
                        v-model="addFormData.level" 
                        type="number"
                        max="5"
                        min="1"
                    ></el-input-number>
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button  @click="isAddModalVisible = false" style="color:white;">取消</el-button>
                    <el-button type="primary" @click="submitAddForm" >确定</el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 修改文件模态框 -->
        <el-dialog v-model="isEditModalVisible" title="修改文件" @close="handleEditModalClose">
            <el-form :data="editFormData" :rules="editFormRules" ref="editFormRef" label-width="80px">
                <el-form-item label="文件内容">
                    <el-input
                        v-model="editFormData.content"
                        type="textarea"
                        :rows="10"
                        placeholder="请输入文件内容"
                    ></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="isEditModalVisible = false" style="color:white;">取消</el-button>
                    <el-button type="primary" @click="submitEditForm" >确定</el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 删除确认对话框 -->
        <el-dialog 
            v-model="deleteConfirmVisible" 
            title="删除确认" 
            @close="handleDeleteCancel"
        >
            <p>确定要删除该文件吗？</p>
            <template #footer>
                <el-button @click="deleteConfirmVisible = false" size="small" style="color:white; padding: 8px 16px;">取消</el-button>
                <el-button type="danger" @click="handleDeleteConfirm" size="small" style="padding: 8px 16px;">删除</el-button>
            </template>
        </el-dialog>

        <!-- 查看文件内容对话框 -->
        <el-dialog 
            v-model="viewFileVisible" 
            title="文件内容" 
            @close="handleViewFileClose"
        >
            <div class="view-file-header">
                <el-button type="primary" @click="openEditModal(currentViewFileId)" v-if="currentViewFileId">修改</el-button>
            </div>
            <div class="view-file-content" style="max-height: 400px; overflow-y: auto;">
                <VueMarkdown :source="viewFileContentData" />
            </div>
            <template #footer>
                <el-button @click="viewFileVisible = false" size="small" style="color:white; padding: 8px 16px;">关闭</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'; // 添加 nextTick 导入
import { ElMessage, ElDialog, ElUpload } from 'element-plus';
import { uploadMarkdown, updateMarkdown, deleteMarkdown, queryMarkdownPage, chatByMarkdownDoc, getMarkdown } from '@/api/ai.js';
import { Edit, Delete, View } from '@element-plus/icons-vue'; 
import Breadcrumb from '@/components/Breadcrumb.vue';
// 引入 VueMarkdown 组件替代 marked
import VueMarkdown from 'vue3-markdown-it';
import FileList from './components/FileList.vue';
import MarkdownIt from 'markdown-it';

// 创建一个 MarkdownIt 实例
const md = new MarkdownIt();

// 文件列表
const fileList = ref([]);
// 当前选中的文件 ID
const selectedFileId = ref(null);
// 新增文件模态框相关变量
const isAddModalVisible = ref(false);
const addFormData = ref({ file: null, fileName: '', level: 2 });
const addFormRef = ref(null);
const addFormRules = ref({ 
    file: [
        { required: true, message: '文件为必填项', trigger: 'change' }
    ]
});
const addUploadRef = ref(null);

// 修改文件模态框相关变量
const isEditModalVisible = ref(false);
const editFormData = ref({ content: '' });
const editFormRef = ref(null);
const editFormRules = ref({ 
    content: [
        { required: true, message: '文件内容为必填项', trigger: 'change' }
    ]
});
const currentEditFileId = ref(null);

// 删除确认对话框
const deleteConfirmVisible = ref(false);
const deletingFileId = ref(null);

// 分页相关
const currentPage = ref(1);
const pageSize = ref(10);
const totalFiles = ref(0);
const isLoadingMore = ref(false); // 加载更多状态

// 提问相关
const question = ref('');
const answer = ref('');
const isLoading = ref(false);
const chatMessagesList = ref([]);

// 查看文件内容对话框
const viewFileVisible = ref(false);
const viewFileContentData = ref('');
const currentViewFileId = ref(null);

// 引用聊天消息显示区域的 DOM 元素
const chatMessages = ref(null);

// 获取文件列表
const fetchFileList = async (page = 1) => {
    try {
        const response = await queryMarkdownPage({ pageNo: page, pageSize: pageSize.value });
        if (page === 1) {
            fileList.value = response.data.list;
        } else {
            fileList.value = [...fileList.value, ...response.data.list];
        }
        totalFiles.value = response.data.total;
    } catch (error) {
        console.error('获取文件列表失败:', error);
        ElMessage.error('获取文件列表失败: ' + (error.message || '未知错误'));
    }
};

// 关闭新增文件模态框时清除校验状态
const handleAddModalClose = () => {
    addFormRef.value?.resetFields(); // 清除表单校验状态
    addFormData.value = { file: null, fileName: '', level: 2 }; // 清空输入内容
    addUploadRef.value.clearFiles();
};

// 处理新增文件选择
const handleAddFileChange = (file) => {
    addFormData.value.file = file.raw;
    addFormData.value.fileName = file.name;
};

// 打开新增文件模态框
const openAddModal = () => {
    isAddModalVisible.value = true;
};

// 提交新增文件表单
const submitAddForm = () => {
    addFormRef.value.validate(valid => {
        if (valid) {
            createFile();
        }
    });
};

// 新增文件
const createFile = async () => {
    try {
        const res = await uploadMarkdown(addFormData.value.file, addFormData.value.level);
        if (res.code === 200) {
            ElMessage.success('创建文件成功');
            isAddModalVisible.value = false;
            currentPage.value = 1;
            fetchFileList();
        } else {
            ElMessage.error(`创建失败: ${res.msg || '未知错误'}`);
        }
    } catch (error) {
        ElMessage.error(`创建失败: ${error.message || '网络错误'}`);
    }
};

// 关闭修改文件模态框时清除校验状态
const handleEditModalClose = () => {
    editFormRef.value?.resetFields(); // 清除表单校验状态
    editFormData.value = { content: '' }; // 清空输入内容
};

// 打开修改文件模态框
const openEditModal = async (id) => {
    currentEditFileId.value = id;
    try {
        const fileData = await getMarkdown(id);
        editFormData.value.content = fileData.data;
        isEditModalVisible.value = true;
    } catch (error) {
        ElMessage.error('获取文件内容失败: ' + (error.message || '未知错误'));
    }
};

// 提交修改文件表单
const submitEditForm = () => {
    updateFile(currentEditFileId.value);
};

// 修改文件
const updateFile = async (id) => {
    try {
        const res = await updateMarkdown({ id, content: editFormData.value.content });
        if (res.code === 200) {
            ElMessage.success('修改成功');
            isEditModalVisible.value = false;
            currentPage.value = 1;
            fetchFileList();
        } else {
            ElMessage.error(`修改失败: ${res.msg || '未知错误'}`);
        }
    } catch (error) {
        ElMessage.error(`修改失败: ${error.message || '网络错误'}`);
    }
};

// 删除确认流程
const confirmDelete = (id) => {
    deletingFileId.value = id;
    deleteConfirmVisible.value = true;
};

const handleDeleteConfirm = async () => {
    try {
        const res = await deleteMarkdown(deletingFileId.value);
        if (res.code === 200) {
            ElMessage.success('删除成功');
            deleteConfirmVisible.value = false;
            currentPage.value = 1;
            fetchFileList();
        } else {
            ElMessage.error(`删除失败: ${res.msg || '未知错误'}`);
        }
    } catch (error) {
        ElMessage.error(`删除失败: ${error.message || '网络错误'}`);
    }
};

const handleDeleteCancel = () => {
    deletingFileId.value = null;
};

// 选择文件
const selectFile = (fileId) => {
    selectedFileId.value = fileId;
};

// 滚动加载更多文件
const loadMoreFiles = async () => {
    if (isLoadingMore.value || fileList.value.length >= totalFiles.value) return;
    isLoadingMore.value = true;
    currentPage.value++;
    await fetchFileList(currentPage.value);
    isLoadingMore.value = false;
};

// 提问智能体
const askQuestion = async () => {
    if (!question.value.trim()) {
        ElMessage.error('请输入问题');
        return;
    }   

    isLoading.value = true;
    chatMessagesList.value.push({ type: 'user', content: question.value });
    try {
        const res = await chatByMarkdownDoc({message:question.value});
        
        // 检查返回的数据格式
        if (res && res.data && res.data.title && res.data.content) {
            // 处理 MarkdownChunk 格式的响应
            const markdownChunk = {
                title: res.data.title,
                content: res.data.content,
                contentHtml: md.render(res.data.content)
            };
            
            chatMessagesList.value.push({ 
                type: 'markdownChunk', 
                chunk: markdownChunk 
            });
        } else if (res && res.content) {
            // 处理普通文本格式的响应
            answer.value = res.content;
            const processedContent = md.render(answer.value);
            chatMessagesList.value.push({ 
                type: 'assistant', 
                content: answer.value, 
                processedContent 
            });
        } else {
            // 处理未知格式的响应
            ElMessage.error('AI返回的响应格式不正确');
        }
    } catch (error) {
        ElMessage.error('提问失败: ' + (error.message || '未知错误'));
    } finally {
        isLoading.value = false;
        question.value = '';
        // 滚动到聊天框底部
        nextTick(() => {
            if (chatMessages.value) {
                chatMessages.value.scrollTop = chatMessages.value.scrollHeight;
            }
        });
    }
};

// 查看文件内容
const viewFileContent = async (id) => {
    currentViewFileId.value = id;
    try {
        const fileData = await getMarkdown(id);
        viewFileContentData.value = fileData.data;
        viewFileVisible.value = true;
    } catch (error) {
        ElMessage.error('获取文件内容失败: ' + (error.message || '未知错误'));
    }
};

const handleViewFileClose = () => {
    viewFileContentData.value = '';
    currentViewFileId.value = null;
};

// 全局点击事件处理函数
const handleGlobalClick = (event) => {
    const target = event.target;
    if (!target.closest('.session-item')) {
        selectedFileId.value = null;
    }
};

onMounted(async () => {
    await fetchFileList();
});

onBeforeUnmount(() => {
    // 移除全局点击事件监听器
    window.removeEventListener('click', handleGlobalClick);
});
</script>

<style lang="scss" scoped>
.aiChatWrapper {
    margin-left: 20px;
    margin-right: 20px;
    margin-bottom: 20px;
    .chatLayout {
        display: flex;
    }

    .chatItems {
        flex: 2;
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        padding: 50px 50px 20px 5px;
    }

    .chatMessages {
        width: 100%;
        height: 400px;
        overflow-y: auto;
        border: 1px solid #EEEEEE;
        border-radius: 8px;
        padding: 20px;
        margin-bottom: 20px;
        background-color: #f9f9f9;
    }

    .message {
        margin-bottom: 20px;
    }

    .userMessage {
        text-align: right;
        margin-left: 20%;

        .messageContent {
            display: inline-block;
            background-color: #007BFF;
            color: white;
            padding: 10px 15px;
            border-radius: 18px 18px 0 18px;
            max-width: 100%;
            word-break: break-word;
        }
    }

    .assistantMessage {
        text-align: left;
        margin-right: 20%;

        .messageContent {
            display: inline-block;
            background-color: #f1f1f1;
            color: #333;
            padding: 10px 15px;
            border-radius: 18px 18px 18px 0;
            max-width: 100%;
            word-break: break-word;
        }
    }

    .markdownChunkMessage {
        text-align: left;
        margin-right: 20%;

        .markdownChunkContainer {
            background-color: #f8f9fa;
            border: 1px solid #e9ecef;
            border-radius: 18px 18px 18px 0;
            overflow: hidden;
            margin-bottom: 15px;
        }

        .markdownChunkHeader {
            background-color: #e9ecef;
            padding: 10px 15px;
        }

        .markdownChunkTitle {
            font-size: 18px;
            font-weight: bold;
            color: #333;
        }

        .markdownChunkContent {
            padding: 15px;
            color: #333;
            word-break: break-word;
            
            // 为 Markdown 渲染的内容添加样式
            h1, h2, h3, h4, h5, h6 {
                margin-top: 1.5rem;
                margin-bottom: 0.5rem;
                color: #212529;
            }
            
            p {
                margin-bottom: 1rem;
            }
            
            ul, ol {
                margin-bottom: 1rem;
                padding-left: 1.5rem;
            }
            
            pre {
                background-color: #f8f9fa;
                border: 1px solid #e9ecef;
                border-radius: 4px;
                padding: 1rem;
                overflow-x: auto;
                margin-bottom: 1rem;
            }
            
            code {
                background-color: #f8f9fa;
                padding: 0.2rem 0.4rem;
                border-radius: 4px;
            }
        }
    }

    .inputArea {
        width: 100%;
        display: flex;
        align-items: center;
    }

    input {
        flex: 1;
        height: 45px;
        border: 1px solid #EEEEEE;
        border-radius: 8px;
        padding: 0 15px;
        margin-right: 10px;
        font-size: 16px;

        &:focus {
            outline: none;
            border-color: #007BFF;
        }

        &:disabled {
            background-color: #f5f5f5;
        }
    }

    button {
        border: none;
        border-radius: 8px;
        background-color: #007BFF;
        color: white;
        cursor: pointer;
        font-size: 14px;
        transition: all 0.3s;

        &:hover {
            background-color: #0056b3;
        }

        &:disabled {
            background-color: #a0c4ff;
            cursor: not-allowed;
        }
    }

    .el-button {
        &.bt-round {
            padding: 8px 16px; // 调整按钮大小
        }
    }

    .view-file-header {
        display: flex;
        justify-content: flex-end;
        margin-bottom: 10px;
    }
}
</style>