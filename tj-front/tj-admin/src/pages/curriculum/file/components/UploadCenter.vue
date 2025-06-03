<template>
  <div>
    <!-- 上传对话框 -->
    <el-dialog
      title="上传文件"
      v-model="dialogVisible"
      width="90%"
      @closed="handleCloseDialog"
      style="max-height: 90vh; overflow-y: auto;"
    >
      <div class="upload-files-container">
        <div 
          v-for="(file, index) in uploadingFiles" 
          :key="file.uniqueId" 
          class="upload-file-card"
        >
          <div class="file-info">
            <div class="file-name">
              <i class="el-icon-document"></i>
              <span>{{ file.name }}</span>
            </div>
            <div class="file-size">{{ (file.size / 1024).toFixed(2) }} KB</div>
          </div>
          
          <div class="progress-container">
            <el-progress 
              :percentage="file.percentage" 
              :status="getProgressStatus(file)"
              :stroke-width="16"
            ></el-progress>
            <div class="status-text">
              <span v-if="file.status === 'success'" class="success">
                <i class="el-icon-success"></i> 上传成功
              </span>
              <span v-else-if="file.status === 'error'" class="error">
                <i class="el-icon-error"></i> 上传失败
              </span>
              <span v-else-if="file.status === 'paused'" class="warning">
                <i class="el-icon-warning"></i> 已暂停
              </span>
              <span v-else>{{ file.percentage }}%</span>
            </div>
          </div>
          
          <div class="file-actions">
            <el-button
              v-if="file.status === 'uploading'"
              size="mini"
              type="warning"
              @click="pauseUpload(file)"
            >
              <i class="el-icon-pause"></i> 暂停
            </el-button>
            <el-button
              v-else-if="file.status === 'paused' || file.status === 'error'"
              size="mini"
              type="primary"
              @click="resumeUpload(file)"
            >
              <i class="el-icon-play"></i> 继续
            </el-button>
            <el-button 
              size="mini" 
              type="danger" 
              @click="removeFile(file)"
            >
              <i class="el-icon-delete"></i> 移除
            </el-button>
          </div>
        </div>
        
        <div v-if="uploadingFiles.length === 0" class="empty-upload-area">
          <i class="el-icon-upload el-icon--primary"></i>
          <p>暂无上传文件</p>
          <p class="small-text">点击下方"添加文件"按钮选择文件上传</p>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-upload
            ref="uploadRef"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            multiple
          >
            <el-button type="primary">
              <i class="el-icon-plus"></i> 添加文件
            </el-button>
          </el-upload>
          <el-button 
            type="success" 
            :loading="isUploadingAll" 
            :disabled="!hasFilesToUpload" 
            @click="uploadAll"
          >
            <i class="el-icon-upload2"></i> 全部上传
          </el-button>
          <el-button 
            type="warning" 
            :disabled="!hasUploadingFiles" 
            @click="pauseAll"
          >
            <i class="el-icon-pause-circle"></i> 全部暂停
          </el-button>
          <el-button @click="dialogVisible = false">
            <i class="el-icon-close"></i> 关闭
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from "vue";
import { ElMessage } from "element-plus";
import CryptoJS from "crypto-js";
import { upRegister, checkchunk, upChunk, mergeChunks } from "@/api/media";

const dialogVisible = ref(false);
const uploadingFiles = ref([]);
const isUploadingAll = ref(false);
const uploadRef = ref(null);
const fileStateMap = ref(new Map()); // 存储文件状态，键为唯一标识

// 计算属性
const hasFilesToUpload = computed(() => {
  return uploadingFiles.value.some(
    (file) => file.status === "ready" || file.status === "paused" || file.status === "error"
  );
});

const hasUploadingFiles = computed(() => {
  return uploadingFiles.value.some((file) => file.status === "uploading");
});

// 获取进度条状态
const getProgressStatus = (file) => {
  if (file.status === "success") return "success";
  if (file.status === "error") return "exception";
  if (file.status === "paused") return "warning";
  return "";
};

// 打开对话框
const openDialog = () => {
  dialogVisible.value = true;
};

// 关闭对话框
const handleCloseDialog = () => {
  uploadingFiles.value = [];
  fileStateMap.value.clear();
  nextTick(() => uploadRef.value?.clearFiles());
};

// 生成文件唯一标识（基于文件名、大小和时间戳）
const generateFileKey = (file) => {
  return `${file.name}-${file.size}-${Date.now()}`;
};

// 过滤重复文件（基于文件内容，使用MD5前10位）
const calculateFileFingerprint = async (file) => {
  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.readAsArrayBuffer(file.slice(0, 1024 * 1024)); // 读取文件前1MB
    reader.onload = (e) => {
      const arrayBuffer = e.target.result;
      const wordArray = CryptoJS.lib.WordArray.create(arrayBuffer);
      const md5 = CryptoJS.MD5(wordArray).toString();
      resolve(md5.substring(0, 10)); // 取MD5前10位作为指纹
    };
  });
};

// 文件选择变化
const handleFileChange = async (file, fileList) => {
  // 过滤无效文件
  const validFiles = fileList.filter(beforeUpload);
  if (validFiles.length === 0) return;

  // 计算文件指纹并检查重复
  const filesWithFingerprint = await Promise.all(
    validFiles.map(async (f) => {
      const fingerprint = await calculateFileFingerprint(f.raw);
      return { ...f, fingerprint };
    })
  );

  // 检查是否有重复指纹的文件
  const existingFingerprints = new Set(uploadingFiles.value.map(f => f.fingerprint));
  const newFiles = filesWithFingerprint.filter(f => !existingFingerprints.has(f.fingerprint));

  // 显示重复文件提示
  const duplicateCount = filesWithFingerprint.length - newFiles.length;
  if (duplicateCount > 0) {
    ElMessage.warning(`已过滤 ${duplicateCount} 个重复文件`);
  }

  // 添加新文件到列表
  uploadingFiles.value = [
    ...uploadingFiles.value,
    ...newFiles.map((f) => ({
      ...f,
      uniqueId: generateFileKey(f), // 使用唯一ID替代uid
      percentage: 0,
      status: "ready",
      abortController: new AbortController(),
    }))
  ];

  // 重置el-upload内部状态
  nextTick(() => uploadRef.value?.clearFiles());
};

// 上传前校验
const beforeUpload = (file) => {
  const MAX_SIZE = 10 * 1024 * 1024 * 1024; // 10GB
  if (file.size > MAX_SIZE) {
    ElMessage.error(`文件大小不能超过 ${MAX_SIZE / 1024 / 1024}MB`);
    return false;
  }
  return true;
};

// 上传单个文件
const uploadFile = (file) => {
  const fileIndex = uploadingFiles.value.findIndex((f) => f.uniqueId === file.uniqueId);
  if (fileIndex === -1) return;

  uploadingFiles.value[fileIndex].status = "uploading";

  uploadByPieces({
    file: file.raw,
    pieceSize: 5, // 5MB 分片大小
    signal: uploadingFiles.value[fileIndex].abortController.signal,
    success: (data) => {
      const fileIndex = uploadingFiles.value.findIndex((f) => f.uniqueId === file.uniqueId);
      if (fileIndex === -1) return;
      
      const percentage = Math.min(Math.round((data.num / data.chunkCount) * 100), 100);
      uploadingFiles.value[fileIndex].percentage = percentage;
      
      if (data.state === "success") {
        uploadingFiles.value[fileIndex].status = "success";
        ElMessage.success(`文件 ${file.name} 上传成功`);
      }
    },
    error: (e) => {
      const fileIndex = uploadingFiles.value.findIndex((f) => f.uniqueId === file.uniqueId);
      if (fileIndex === -1) return;
      
      uploadingFiles.value[fileIndex].status = "error";
      ElMessage.error(`文件 ${file.name} 上传失败: ${e.message || "未知错误"}`);
    },
  });
};

// 暂停上传
const pauseUpload = (file) => {
  const fileIndex = uploadingFiles.value.findIndex((f) => f.uniqueId === file.uniqueId);
  if (fileIndex === -1) return;

  uploadingFiles.value[fileIndex].abortController.abort();
  uploadingFiles.value[fileIndex].status = "paused";
};

// 继续上传
const resumeUpload = (file) => {
  const fileIndex = uploadingFiles.value.findIndex((f) => f.uniqueId === file.uniqueId);
  if (fileIndex === -1) return;

  uploadingFiles.value[fileIndex].abortController = new AbortController();
  uploadFile(file);
};

// 移除文件
const removeFile = (file) => {
  // 从状态映射中移除
  fileStateMap.value.delete(file.uniqueId);
  
  // 从文件列表中移除
  uploadingFiles.value = uploadingFiles.value.filter((f) => f.uniqueId !== file.uniqueId);
  
  // 确保el-upload组件的内部状态也被更新
  nextTick(() => {
    if (uploadRef.value) {
      // 尝试找到el-upload内部的文件对象并移除
      const internalFile = uploadRef.value.fileList.find(
        (f) => f.uid === file.uid || (f.name === file.name && f.size === file.size)
      );
      
      if (internalFile) {
        uploadRef.value.handleRemove(internalFile);
      }
      
      // 清空所有文件（可选，根据实际情况决定是否需要）
      uploadRef.value.clearFiles();
    }
  });
};

// 上传所有文件
const uploadAll = () => {
  isUploadingAll.value = true;
  const filesToUpload = uploadingFiles.value.filter(
    (f) => f.status === "ready" || f.status === "paused" || f.status === "error"
  );

  if (filesToUpload.length === 0) {
    ElMessage.info("没有需要上传的文件");
    isUploadingAll.value = false;
    return;
  }

  filesToUpload.forEach((file) => {
    if (!file.abortController || file.abortController.signal.aborted) {
      file.abortController = new AbortController();
    }
    uploadFile(file);
  });

  isUploadingAll.value = false;
};

// 暂停所有上传
const pauseAll = () => {
  const uploadingFilesList = uploadingFiles.value.filter(
    (f) => f.status === "uploading"
  );

  if (uploadingFilesList.length === 0) {
    ElMessage.info("没有正在上传的文件");
    return;
  }

  uploadingFilesList.forEach((file) => {
    pauseUpload(file);
  });
};

// 分片上传核心方法
const uploadByPieces = async ({ file, pieceSize = 5, signal, success, error }) => {
  // 上传过程中用到的变量
  let fullFileMD5 = ""; // 完整文件的MD5
  const chunkSize = pieceSize * 1024 * 1024; // 分片大小
  const chunkCount = Math.ceil(file.size / chunkSize); // 总片数

  // 得到某一片的分片
  const getChunkInfo = (file, currentChunk, chunkSize) => {
    let start = currentChunk * chunkSize;
    let end = Math.min(file.size, start + chunkSize);
    let chunk = file.slice(start, end);
    return chunk;
  };

  // 计算完整文件的MD5
  const calculateFullFileMD5 = () => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsArrayBuffer(file);
      
      reader.onload = (e) => {
        const arrayBuffer = e.target.result;
        const wordArray = CryptoJS.lib.WordArray.create(arrayBuffer);
        fullFileMD5 = CryptoJS.MD5(wordArray).toString();
        console.log("完整文件MD5:", fullFileMD5);
        resolve(fullFileMD5);
      };
      
      reader.onerror = () => {
        reject(new Error("计算完整文件MD5失败"));
      };
    });
  };

  // 上传分片
  const uploadChunk = async (num) => {
    if (signal.aborted) return;
    
    const chunk = getChunkInfo(file, num, chunkSize);
    
    try {
      // 检查分片是否已上传（使用完整文件MD5）
      const checkRes = await checkchunk({ fileMd5: fullFileMD5, chunk: num });
      console.log(`分片${num}检查结果:`, checkRes);
      
      if (checkRes.code === 200 && !checkRes.data) {
        console.log(`开始上传分片${num}/${chunkCount}`);
        
        // 分片未上传，执行上传（使用完整文件MD5）
        const formData = new FormData();
        formData.append("file", chunk);
        formData.append("fileMd5", fullFileMD5); // 使用完整文件MD5
        formData.append("chunk", num);
        
        const uploadRes = await upChunk(formData, { signal });
        console.log(`分片${num}上传结果:`, uploadRes);
        
        if (uploadRes.code !== 200) {
          throw new Error(uploadRes.message || "分片上传失败");
        }
      }
      
      // 更新进度
      success({ num, chunkCount, state: 'uploading' });
    } catch (err) {
      console.error(`分片${num}上传出错:`, err);
      throw err;
    }
  };

  try {
    // 1. 计算完整文件的MD5（用于所有操作）
    fullFileMD5 = await calculateFullFileMD5();
    
    // 2. 注册文件上传（使用完整文件MD5）
    const registerRes = await upRegister({ fileMd5: fullFileMD5 });
    console.log("文件注册结果:", registerRes);
    
    if (registerRes.code !== 200) {
      throw new Error(registerRes.message || "文件注册失败");
    }
    
    if (registerRes.data === true) {
      ElMessage.success("文件秒传成功");
      success({ num: chunkCount, chunkCount, state: 'success' });
      return;
    }
    
    // 3. 上传所有分片（全部使用完整文件MD5）
    for (let i = 0; i < chunkCount; i++) {
      await uploadChunk(i);
      if (signal.aborted) return; // 检查是否被取消
    }
    
    // 4. 合并分片（使用完整文件MD5）
    const mergeRes = await mergeChunks({
      fileMd5: fullFileMD5,
      fileName: file.name,
      chunkTotal: chunkCount
    });
    
    console.log("合并结果:", mergeRes);
    
    if (mergeRes.code === 200) {
      success({ num: chunkCount, chunkCount, state: 'success' });
    } else {
      throw new Error(mergeRes.message || "文件合并失败");
    }
  } catch (err) {
    if (err.name !== 'AbortError') { // 非用户取消的错误
      console.error("上传流程出错:", err);
      error(err);
    }
  }
};

// 向父组件暴露方法
defineExpose({
  openDialog,
});
</script>
<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 20px;
}

.status-text {
  margin-top: 5px;
  font-size: 12px;
}

.success {
  color: #67c23a;
}

.error {
  color: #f56c6c;
}

.warning {
  color: #e6a23c;
}
.upload-files-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  padding: 16px;
}

.upload-file-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 16px;
  transition: all 0.3s ease;
}

.upload-file-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.file-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.file-name {
  display: flex;
  align-items: center;
  font-weight: 500;
  overflow: hidden;
}

.file-name i {
  margin-right: 8px;
  color: #409eff;
}

.file-name span {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  color: #909399;
  font-size: 12px;
}

.progress-container {
  margin-bottom: 12px;
}

.status-text {
  margin-top: 6px;
  text-align: center;
  font-size: 14px;
}

.success {
  color: #67c23a;
}

.error {
  color: #f56c6c;
}

.warning {
  color: #e6a23c;
}

.file-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.empty-upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #909399;
  text-align: center;
  grid-column: 1 / -1;
}

.empty-upload-area i {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-upload-area p {
  margin: 0;
}

.small-text {
  font-size: 12px;
  margin-top: 8px !important;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 20px;
}
</style>