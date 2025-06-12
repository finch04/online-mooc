<template>
    <div class="sessionList">
        <!-- 新增文件按钮 -->
        <el-button style="margin-bottom: 20px;text-align: center;color: white;" @click="openModal(null)">新增文件</el-button>
        <ul class="session-ul" ref="fileListRef" @scroll="handleScroll">
            <li v-for="file in fileList" :key="file.id"
                :class="{ 'active-session': selectedFileId === file.id }"
                @click="selectFile(file.id)" 
                class="session-item">
                <!-- 文件内容区域 -->
                <div class="session-content">
                    <!-- 文件名称 -->
                    <div class="session-name">
                        {{ file.fileName }}
                    </div>
                </div>

                <!-- 操作按钮组 -->
                <div class="button-group">
                    <!-- <el-button 
                        type="primary" 
                        class="edit-button" 
                        :icon="Edit" 
                        circle 
                        @click="openModal(file.id)"
                    ></el-button> -->
                    <el-button 
                        type="warning"
                        class="delete-button" 
                        :icon="Delete" 
                        circle 
                        @click="confirmDelete(file.id)"
                    ></el-button>
                    <el-button 
                        type="info"
                        class="view-button" 
                        :icon="View" 
                        circle 
                        @click="viewFileContent(file.id)"
                    ></el-button>
                </div>
            </li>
        </ul>
    </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import { Edit, Delete, View } from '@element-plus/icons-vue'; 

const props = defineProps({
    fileList: {
        type: Array,
        default: () => []
    },
    selectedFileId: {
        type: [Number, String],
        default: null
    },
    currentPage: {
        type: Number,
        default: 1
    },
    pageSize: {
        type: Number,
        default: 10
    },
    totalFiles: {
        type: Number,
        default: 0
    }
});

const emits = defineEmits([
    'open-modal',
    'select-file',
    'confirm-delete',
    'view-file-content',
    'load-more'
]);

const fileListRef = ref(null);

// 打开模态框
const openModal = (id) => {
    emits('open-modal', id);
};

// 选择文件
const selectFile = (fileId) => {
    emits('select-file', fileId);
};

// 删除确认流程
const confirmDelete = (id) => {
    emits('confirm-delete', id);
};

// 查看文件内容
const viewFileContent = (id) => {
    emits('view-file-content', id);
};

// 滚动加载更多
const handleScroll = () => {
    const { scrollTop, scrollHeight, clientHeight } = fileListRef.value;
    if (scrollTop + clientHeight >= scrollHeight - 10) {
        emits('load-more');
    }
};
</script>

<style scoped>
.sessionList {
    width: 350px;
    padding: 15px;
    background-color: white;
    border-right: 1px solid rgb(249, 249, 249);
}

.session-ul {
    width: 100%;
    padding: 0;
    max-height: 400px; /* 设置最大高度，使其可以滚动 */
    overflow-y: auto;
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

.el-button.view-button {
    background-color: #17a2b8;
    color: white;
    
    &:hover {
        background-color: #138496;
    }
}
</style>