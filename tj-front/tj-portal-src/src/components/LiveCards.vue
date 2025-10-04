<template>
    <div class="classCards" @click="goDetails(data.id)">
      <div class="image">
        <!-- 根据直播间状态显示不同标签 -->
        <span
          class="label"
          :class="{
            'live': data.status === 1,
            'not-start': data.status === 0,
            'closed': data.status === 2,
            'forbidden': data.status === 3
          }"
        >
          {{ statusLabel }}
        </span>
        <img :src="data.roomCover" alt="" />
      </div>
      <div class="pd-10">
        <div class="title marg-bt-10 ft-14" v-html="data.roomTitle"></div>
        <!-- 主播信息 -->
        <div class="ft-cl-des" v-if="type == 'default' || type == 'search'">
          <span>主播：</span>
          {{ data.anchorName || '未知主播' }}
        </div>
        <!-- 在线人数 -->
        <div class="ft-cl-des" v-if="type == 'default'">
          <span>在线：</span>
          {{ data.onlineCount }} 人
        </div>
        <!-- 综合信息（搜索页） -->
        <div class="ft-cl-des fx-sb" v-if="type == 'search'">
          <span>
            最高{{ data.maxOnlineCount || 0 }}人在线 <em>.</em> 
            点赞{{ data.likeCount }}次
          </span>
        </div>
        <!-- 点赞与分享（推荐页） -->
        <div class="ft-cl-des fx-sb" v-if="type == 'like'">
          <span>点赞：{{ data.likeCount }} 次</span>
          <span>分享：{{ data.shareCount }} 次</span>
        </div>
        <!-- 私有标识 -->
        <div class="ft-cl-des" v-if="data.isPrivate">
          <span class="private-tag">私有直播间</span>
        </div>
      </div>
    </div>
  </template>
  <script setup>
  import router from '../router';
  import { computed } from 'vue';
  const props = defineProps({
    data: {
      type: Object,
      default: () => ({})
    },
    type: {
      type: String,
      default: 'default'
    }
  });
  
  // 计算直播间状态标签
  const statusLabel = computed(() => {
    const statusMap = {
      0: '未开播',
      1: '直播中',
      2: '已关闭',
      3: '禁播'
    };
    return statusMap[props.data.status] || '未知状态';
  });
  
  const goDetails = (id) => {
    router.push({ path: '/liveDetails', query: {id} });
  };
  </script>
  <style lang="scss" scoped>
  .classCards {
    position: relative;
    width: 23%;
    background: #FFFFFF;
    border: 1px solid #EEEEEE;
    border-radius: 8px;
    font-size: 12px;
    line-height: 24px;
    cursor: pointer;
    transition: all 0.3s ease;
  
    &:hover {
      box-shadow: 0 4px 6px 2px rgba(108, 112, 118, 0.17);
      top: -3px;
    }
  
    .title {
      line-height: 22px;
      :deep(em) {
        font-style: normal;
        color: var(--color-main);
      }
    }
  
    .image {
      width: 100%;
      height: 160px;
      overflow: hidden;
      position: relative;
  
      .label {
        position: absolute;
        top: 10px;
        left: 10px;
        font-size: 12px;
        padding: 2px 5px;
        color: #fff;
        border-radius: 3px;
      }
  
      // 状态标签样式
      .live {
        background: rgba(255, 0, 0, 0.6);
      }
      .not-start {
        background: rgba(100, 100, 100, 0.6);
      }
      .closed {
        background: rgba(150, 150, 150, 0.6);
      }
      .forbidden {
        background: rgba(200, 0, 0, 0.6);
      }
  
      img {
        width: 100%;
        border-radius: 8px 8px 0 0;
      }
    }
  
    em {
      position: relative;
      top: -3px;
    }
  
    // 私有直播间标签
    .private-tag {
      background-color: #ff4d4f;
      color: #fff;
      padding: 2px 5px;
      border-radius: 3px;
      font-size: 12px;
    }
  }
  </style>