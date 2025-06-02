<template>
  <div>
    <!-- 上传对话框 -->
    <el-dialog title="上传文件" v-model="dialogVisible" width="90%" @closed="handleCloseDialog">
      <el-table :data="uploadingFiles" border style="width: 100%" :header-cell-style="{ textAlign: 'center' }">
        <el-table-column prop="name" label="文件名" align="center"></el-table-column>
        <el-table-column prop="size" label="大小(KB)" align="center"  width="100">
          <template #default="scope">
            {{ (scope.row.size / 1024).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="300">
          <template #default="scope">
            <el-progress :percentage="scope.row.percentage" :status="getProgressStatus(scope.row)"></el-progress>
            <div class="status-text">
              <span v-if="scope.row.status === 'success'" class="success">
                <i class="el-icon-success"></i> 上传成功
              </span>
              <span v-else-if="scope.row.status === 'error'" class="error">
                <i class="el-icon-error"></i> 上传失败
              </span>
              <span v-else-if="scope.row.status === 'paused'" class="warning">
                <i class="el-icon-warning"></i> 已暂停
              </span>
              <span v-else>{{ scope.row.percentage }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'uploading'" size="mini" type="warning"
              @click="pauseUpload(scope.row)">
              暂停
            </el-button>
            <el-button v-else-if="scope.row.status === 'paused' || scope.row.status === 'error'" size="mini"
              type="primary" @click="resumeUpload(scope.row)">
              继续
            </el-button>
            <el-button size="mini" type="danger" @click="removeFile(scope.row)">
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <div class="dialog-footer">
          <el-upload   ref="uploadRef" action="#" :auto-upload="false" :show-file-list="false" :on-change="handleFileChange"
            :before-upload="beforeUpload" multiple>
            <el-button type="primary">添加文件</el-button>
          </el-upload>
          <el-button type="success" :loading="isUploadingAll" :disabled="!hasFilesToUpload" @click="uploadAll">
            全部上传
          </el-button>
          <el-button type="warning" :disabled="!hasUploadingFiles" @click="pauseAll">
            全部暂停
          </el-button>
          <el-button @click="dialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { ElMessage } from "element-plus";
import CryptoJS from "crypto-js";
import { upRegister, checkchunk, upChunk, mergeChunks } from "@/api/media";

const dialogVisible = ref(false);
const uploadingFiles = ref([]);
const isUploadingAll = ref(false);

const uploadRef = ref(null); // 添加对el-upload组件的引用
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
  uploadRef.value.clearFiles();
};

// 文件选择变化
const handleFileChange = (file, fileList) => {
  if (!beforeUpload(file)) return;

  uploadingFiles.value = fileList.map((f) => ({
    ...f,
    percentage: 0,
    status: "ready", // ready, uploading, paused, success, error
    abortController: new AbortController(),
  }));
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
  console.log("上传单个文件", file)
  const fileIndex = uploadingFiles.value.findIndex((f) => f.uid === file.uid);
  if (fileIndex === -1) return;

  uploadingFiles.value[fileIndex].status = "uploading";

  uploadByPieces({
    file: file.raw,
    pieceSize: 5, // 5MB 分片大小
    signal: uploadingFiles.value[fileIndex].abortController.signal,
    success: (data) => {
      const percentage = Math.min(Math.round((data.num / data.chunkCount) * 100), 100);
      uploadingFiles.value[fileIndex].percentage = percentage;

      if (data.state === "success") {
        uploadingFiles.value[fileIndex].status = "success";
        ElMessage.success(`文件 ${file.name} 上传成功`);
      }
    },
    error: (e) => {
      uploadingFiles.value[fileIndex].status = "error";
      ElMessage.error(`文件 ${file.name} 上传失败: ${e.message || "未知错误"}`);
    },
  });
};

// 暂停上传
const pauseUpload = (file) => {
  const fileIndex = uploadingFiles.value.findIndex((f) => f.uid === file.uid);
  if (fileIndex === -1) return;

  uploadingFiles.value[fileIndex].abortController.abort();
  uploadingFiles.value[fileIndex].status = "paused";
};

// 继续上传
const resumeUpload = (file) => {
  const fileIndex = uploadingFiles.value.findIndex((f) => f.uid === file.uid);
  if (fileIndex === -1) return;

  uploadingFiles.value[fileIndex].abortController = new AbortController();
  uploadFile(file);
};

// 移除文件
const removeFile = (file) => {
  if (file.status === "uploading") {
    pauseUpload(file);
  }
  uploadingFiles.value = uploadingFiles.value.filter((f) => f.uid !== file.uid);
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
  let fileMD5 = ""; // md5加密文件的标识
  const chunkSize = pieceSize * 1024 * 1024; // 分片大小
  const chunkCount = Math.ceil(file.size / chunkSize); // 总片数

  // 得到某一片的分片
  const getChunkInfo = (file, currentChunk, chunkSize) => {
    let start = currentChunk * chunkSize;
    let end = Math.min(file.size, start + chunkSize);
    let chunk = file.slice(start, end);
    return chunk;
  };

  // 第一步：计算文件MD5
  const readFileMD5 = () => {
    return new Promise((resolve) => {
      // 得到第一片和最后一片
      const startChunk = getChunkInfo(file, 0, chunkSize);
      const fileRederInstance = new FileReader();
      fileRederInstance.readAsBinaryString(startChunk);
      fileRederInstance.addEventListener("load", (e) => {
        let fileBolb = e.target.result;
        fileMD5 = CryptoJS.MD5(CryptoJS.enc.Latin1.parse(fileBolb)).toString();
        console.log("文件MD5:", fileMD5);
        resolve(fileMD5);
      });
    });
  };

  // 第二步：上传分片
  const readChunkMD5 = async (num) => {
    if (signal.aborted) return;

    if (num <= chunkCount - 1) {
      // 得到当前需要上传的分片文件
      const chunk = getChunkInfo(file, num, chunkSize);
      
      try {
        // 1. 检查分片是否已上传
        const checkRes = await checkchunk({ fileMd5: fileMD5, chunk: num });
        console.log("检查分片结果:", checkRes);
        
        if (checkRes.code === 200 && checkRes.data === false) {
          // 2. 分片未上传，执行上传
          let fetchForm = new FormData();
          fetchForm.append("file", chunk);
          fetchForm.append("fileMd5", fileMD5);
          fetchForm.append("chunk", num);
          
          const uploadRes = await upChunk(fetchForm);
          console.log("上传分片结果:", uploadRes);
          
          if (uploadRes.code !== 200) {
            throw new Error(uploadRes.message || "分片上传失败");
          }
        }
        
        // 3. 更新进度并处理下一个分片
        success({ num, chunkCount, state: 'uploading' });
        await readChunkMD5(num + 1);
        
      } catch (err) {
        console.error("分片上传出错:", err);
        error(err);
        return;
      }
    } else {
      // 所有分片上传完成，执行合并
      try {
        console.log("开始合并文件...");
        const mergeRes = await mergeChunks({
          fileMd5: fileMD5,
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
        console.error("文件合并出错:", err);
        error(err);
      }
    }
  };

  try {
    // 1. 计算文件MD5
    fileMD5 = await readFileMD5();
    
    // 2. 注册文件上传
    const registerRes = await upRegister({ fileMd5:fileMD5 });
    console.log("注册结果:", registerRes);
    if (registerRes.code !== 200) {
      throw new Error(registerRes.message || "文件注册失败");
    }
    if(registerRes.data === true){
      ElMessage.success ("文件秒传成功，无需分片上传");
        success({ num: chunkCount, chunkCount, state: 'success' });
      return;
    }
    
    // 3. 开始上传分片
    await readChunkMD5(0);
    
  } catch (err) {
    console.error("上传流程出错:", err);
    error(err);
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
</style>