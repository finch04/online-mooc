<template>
    <div></div>
  </template>
  
  <script setup>
  import { onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { parseShareUrl } from '@/api/class.js'; // 假设这里有一个解析分享链接的 API
  import { ElMessage } from 'element-plus';
  
  const router = useRouter();
  
  onMounted(async () => {
    
    console.log(router.currentRoute)
    const currentRoute = router.currentRoute.value;
    const shortCode = currentRoute.params.code;
    if (!shortCode) {
      ElMessage({
        message: '分享链接无效，请检查后重试',
        type: 'error'
      });
      return;
    }
  
    try {
      const res = await parseShareUrl(shortCode);
      if (res.code === 200) {
        const { shareId, userId, userIcon, userName, courseId, courseName, coverUrl, createTime } = res.data;
        router.push({
          path: '/details/index',
          query: {
            id: courseId,
            shareUserName: userName,
            shareUserIcon: userIcon
          }
        });
      } else {
        ElMessage({
          message: res.message || '解析分享链接失败',
          type: 'error'
        });
      }
    } catch (error) {
      console.error('解析分享链接出错', error);
      ElMessage({
        message: '网络错误，请稍后再试',
        type: 'error'
      });
    }
  });
  </script>
  
  <style scoped>
  </style>