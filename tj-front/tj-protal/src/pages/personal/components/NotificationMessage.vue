<template>
  <div class="message-container">
    <div class="message-list" @scroll="handleScroll">
      <div v-if="messages.length === 0" class="message-empty">
        <el-empty :description="`暂无${typeName}`" />
      </div>
      <div v-if="isLoading" class="loading-indicator">
        <el-icon class="loading-spinner">
          <Loading />
        </el-icon>
        加载中...
      </div>
      <div v-for="item in messages" :key="item.id" class="message-item"
        :class="{ 'unread': !item.isRead, 'selected': selectedMessage?.id === item.id }" @click="selectMessage(item)">
        <div class="notification-icon">
          <el-icon :size="20" :color="item.isRead ? '#909399' : '#409EFF'">
            <component :is="iconComponent" />
          </el-icon>
        </div>
        <div class="message-content">
          <div class="message-header">
            <span class="sender-name">{{ item.title || typeName }}</span>
            <span class="send-time">{{ item.pushTime }}</span>
          </div>
          <div class="message-preview">{{ item.content.length > 10 ? item.content.slice(0, 10) + '...' : item.content }}
            <el-tag type="primary" style="float: right;" v-if="item.publisher == 0">系统消息</el-tag>
          </div>
          <div v-if="!item.isRead" class="unread-badge"></div>
        </div>
      </div>
    </div>
    <div class="message-detail" v-if="selectedMessage">
      <div class="message-detail-header">
        <div class="sender-info">
          <el-icon :size="24" color="#409EFF">
            <component :is="iconComponent" />
          </el-icon>
          <span class="sender-name">{{ selectedMessage.title || typeName }}</span>
        </div>
        <div class="message-time">{{ selectedMessage.pushTime }}</div>

      </div>
      <div class="sender-profile" v-if="selectedMessage.publisher == 0">
        <el-avatar :size="24" src="src/assets/system.jpg" />
        <span class="sender-name">System</span>
      </div>
      <div class="sender-profile" v-else>
        <el-avatar :size="24" :src="selectedMessage.publisherIcon" />
        <span class="sender-name">{{ selectedMessage.publisherName }}</span>
      </div>
      <div class="message-detail-content">
        {{ selectedMessage.content }}
      </div>
      <div class="message-reply">
        <el-button @click="$emit('mark-read')" v-if="!selectedMessage.isRead">标记已读</el-button>
      </div>
    </div>
    <div class="message-empty" v-else>
      <el-empty :description="`请选择一条${typeName}查看详情`" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { Bell, Document, ChatDotRound, Memo, Loading } from '@element-plus/icons-vue';

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  total: {
    type: Number,
    default: 0
  },
  pageSize: {
    type: Number,
    default: 10
  },
  type: {
    type: String,
    default: 'system'
  }
});

const emit = defineEmits(['select-message', 'scroll', 'mark-read']);

const selectedMessage = ref(null);
const currentPage = ref(1);
const isLoading = ref(false);

const typeName = computed(() => {
  const names = {
    system: '系统通知',
    note: '笔记通知',
    QA: '问答通知',
    other: '其他通知'
  };
  return names[props.type] || '通知';
});

const iconComponent = computed(() => {
  const icons = {
    system: Bell,
    note: Document,
    QA: ChatDotRound,
    other: Memo
  };
  return icons[props.type] || Bell;
});

const selectMessage = (message) => {
  selectedMessage.value = message;
  emit('select-message', message);
};
const handleScroll = (event) => {
  emit('scroll', event);
};
</script>
<style lang="scss" scoped>
.sender-profile {
  display: flex;
  float: ri;
  align-items: center;
  gap: 8px;
  /* 控制头像与文字之间的间距 */
  font-size: 14px;
  align-self: flex-end; // 让这个盒子右对齐
  margin-bottom: 16px;
}

// message.scss
.message-container {
  display: flex;
  height: 600px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;

  .message-list {
    width: 300px;
    border-right: 1px solid #ebeef5;
    overflow-y: auto;

    .message-item {
      display: flex;
      padding: 15px;
      cursor: pointer;
      border-bottom: 1px solid #f5f7fa;
      position: relative;

      &:hover {
        background-color: #f5f7fa;
      }

      &.unread {
        background-color: #f0f7ff;
      }

      &.selected {
        background-color: #e6f1ff;
      }
    }

    .message-avatar,
    .notification-icon {
      margin-right: 10px;
    }

    .message-content {
      flex: 1;
      min-width: 0;
    }

    .message-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 5px;
    }

    .sender-name {
      font-weight: 500;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .send-time {
      font-size: 12px;
      color: #909399;
      white-space: nowrap;
      margin-left: 8px;
    }

    .message-preview {
      font-size: 13px;
      color: #606266;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .unread-badge {
      position: absolute;
      top: 15px;
      right: 15px;
      width: 8px;
      height: 8px;
      background-color: #f56c6c;
      border-radius: 50%;
    }
  }

  .message-detail {
    flex: 1;
    padding: 20px;
    display: flex;
    flex-direction: column;
    overflow-y: auto;

    .message-detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-bottom: 15px;
      border-bottom: 1px solid #ebeef5;
      margin-bottom: 20px;
    }

    .sender-info {
      display: flex;
      align-items: center;

      .sender-name {
        margin-left: 10px;
        font-weight: 500;
        font-size: 16px;
      }
    }

    .message-time {
      color: #909399;
      font-size: 14px;
    }

    .message-conversation {
      flex: 1;
      padding: 10px 0;
      overflow-y: auto;

      .message-bubble {
        margin-bottom: 15px;
        max-width: 70%;

        .bubble-content {
          padding: 10px 15px;
          border-radius: 5px;
          font-size: 14px;
          line-height: 1.5;
        }

        .bubble-time {
          font-size: 12px;
          color: #909399;
          margin-top: 5px;
        }

        &.mine {
          margin-left: auto;
          text-align: right;

          .bubble-content {
            background-color: #409EFF;
            color: white;
          }
        }

        &.theirs {
          margin-right: auto;

          .bubble-content {
            background-color: #f5f5f5;
            color: #333;
          }
        }
      }
    }

    .message-detail-content {
      flex: 1;
      padding: 20px 0;
      line-height: 1.6;
    }

    .message-reply {
      border-top: 1px solid #ebeef5;
      padding-top: 20px;

      .reply-actions {
        margin-top: 15px;
        text-align: right;
      }
    }
  }

  .message-empty {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .message-pagination {
    margin-top: 20px;
    padding: 10px;
    text-align: center;
    border-top: 1px solid #ebeef5;
  }
}
</style>