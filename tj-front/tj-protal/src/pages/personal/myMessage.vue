<template>
  <div class="myMessageWrapper">
    <div class="personalCards">
      <div style="display: flex;">
        <CardsTitle class="marg-bt-20" title="我的消息" />
        <div class="btn" style="float: right;">
          <span class="bt-grey bt-round" style="margin-right: 20px;" @click="getBlockedList">查看黑名单</span>
          <span class="bt bt-round" @click="markAllMessage">全部已读</span>
        </div>
      </div>
      <div class="message-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="私信" name="private">
            <div class="message-list">
              <PrivateMessage :my-id="myId" :my-icon="myIcon" :filter-tab="filterTab"
                :private-message-page-size="privateMessagePageSize"
                :private-message-current-page="privateMessageCurrentPage"
                :message-history-page-size="messageHistoryPageSize"
                :message-history-current-page="messageHistoryCurrentPage" 
               />
            </div>
          </el-tab-pane>
          <el-tab-pane :label="`系统通知${systemUnReadCount > 0 ? ' (' + systemUnReadCount + ')' : ''}`" name="system">
            <div class="message-list">
              <NotificationMessage :messages="systemNotifications" :total="systemNotificationTotal"
                :page-size="notificationPageSize" type="system" @select-message="selectNotification"
                @mark-read="markAsRead" @scroll="(e) => handleScroll('system', e)" />
            </div>
          </el-tab-pane>
          <el-tab-pane :label="`笔记通知${noteUnReadCount > 0 ? ' (' + noteUnReadCount + ')' : ''}`" name="note">
            <div class="message-list">
              <NotificationMessage :messages="noteNotifications" :total="noteNotificationTotal"
                :page-size="notificationPageSize" type="note" @select-message="selectNotification"
                @mark-read="markAsRead" @scroll="(e) => handleScroll('note', e)" />
            </div>
          </el-tab-pane>
          <el-tab-pane :label="`问答通知${QAUnReadCount > 0 ? ' (' + QAUnReadCount + ')' : ''}`" name="QA">
            <div class="message-list">
              <NotificationMessage :messages="QANotifications" :total="QANotificationTotal"
                :page-size="notificationPageSize" type="QA" @select-message="selectNotification" @mark-read="markAsRead"
                @scroll="(e) => handleScroll('QA', e)" />
            </div>
          </el-tab-pane>
          <el-tab-pane :label="`其他通知${otherUnReadCount > 0 ? ' (' + otherUnReadCount + ')' : ''}`" name="other">
            <div class="message-list">
              <NotificationMessage :messages="otherNotifications" :total="otherNotificationTotal"
                :page-size="notificationPageSize" type="other" @select-message="selectNotification"
                @mark-read="markAsRead" @scroll="(e) => handleScroll('other', e)" />
            </div>
          </el-tab-pane>
          <el-tab-pane :label="`在线群聊`" name="online">
            <div class="message-list">
              <WebSocketMessage />
            </div>
          </el-tab-pane>
        </el-tabs>
        <el-dialog v-model="showBlockedUsersDialog" title="屏蔽用户列表">
          <el-table :data="blockedUsers" style="width: 100%" empty-text="暂无屏蔽用户">
            <el-table-column prop="otherAvatar" label="用户头像" width="100">
              <template #default="scope">
                <div style="display: flex; align-items: center">
                  <el-avatar :src="scope.row.otherAvatar" />
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="otherUsername" label="用户名" width="150" />
            <el-table-column prop="lastMessage" label="最后消息" />
            <el-table-column prop="lastMessageTime" label="时间" width="180" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="danger" size="small" @click="unblockUser(scope.row)">
                  取消屏蔽
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination @size-change="handleBlockedUsersSizeChange" @current-change="handleBlockedUsersCurrentChange"
            :current-page="blockedUsersCurrentPage" :page-sizes="[10, 20, 30]" :page-size="blockedUsersPageSize"
            layout="total, sizes, prev, pager, next, jumper" :total="blockedUsersTotal">
          </el-pagination>
          <template #footer>
            <el-button @click="showBlockedUsersDialog = false" style="color: white;">关闭</el-button>
          </template>
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import CardsTitle from './components/CardsTitle.vue';
import PrivateMessage from './components/PrivateMessage.vue';
import NotificationMessage from './components/NotificationMessage.vue';
import { useUserStore } from "@/store"
import { markMessageAsRead, queryUserInbox, getUnReadCountByType, markAllMessageAsRead, unblockUserConversation ,queryUserConversation} from "../../api/message";

import  WebSocketMessage from './components/WebSocketMessage.vue';

const store = useUserStore()
const userInfo = ref(store.getUserInfo)

const activeTab = ref('private');

// 消息相关数据
const systemNotifications = ref([]);
const noteNotifications = ref([]);
const QANotifications = ref([]);
const otherNotifications = ref([]);

// 未读消息数量
const privateUnReadCount = ref(0);
const systemUnReadCount = ref(0);
const noteUnReadCount = ref(0);
const QAUnReadCount = ref(0);
const otherUnReadCount = ref(0);

const selectedMessage = ref(null);
const myId = ref(userInfo.value.id);
const myIcon = ref(userInfo.value.icon)

// 屏蔽用户相关
const showBlockedUsersDialog = ref(false);
const blockedUsers = ref([]);
const blockedUsersTotal = ref(0);
const blockedUsersPageSize = ref(10);
const blockedUsersCurrentPage = ref(1);

// 分页相关
const privateMessageTotal = ref(0);
const privateMessagePageSize = ref(10);
const privateMessageCurrentPage = ref(1);

const messageHistoryPageSize = ref(10);
const messageHistoryCurrentPage = ref(1);

const notificationPageSize = ref(10);

const systemNotificationTotal = ref(0);
const systemNotificationCurrentPage = ref(1);

const noteNotificationTotal = ref(0);
const noteNotificationCurrentPage = ref(1);

const QANotificationTotal = ref(0);
const QANotificationCurrentPage = ref(1);

const otherNotificationTotal = ref(0);
const otherNotificationCurrentPage = ref(1);

// 加载状态
const isLoading = ref({
  private: false,
  system: false,
  note: false,
  QA: false,
  other: false
});

const getUnReadCounts = async () => {
  try {
    const resSystem = await getUnReadCountByType(0);
    systemUnReadCount.value = resSystem.data;

    const resNote = await getUnReadCountByType(1);
    noteUnReadCount.value = resNote.data;

    const resQA = await getUnReadCountByType(2);
    QAUnReadCount.value = resQA.data;

    const resOther = await getUnReadCountByType(3);
    otherUnReadCount.value = resOther.data;
  } catch (error) {
    ElMessage.error('获取未读消息数量失败');
  }
};

onMounted(async () => {
  await getUnReadCounts();
});
const filterTab = ref('normal');
// 新增选中会话缓存
const selectedPrivateMessage = ref(null);


// 分页大小改变
const handleBlockedUsersSizeChange = (val) => {
  blockedUsersPageSize.value = val;
  blockedUsersCurrentPage.value = 1;
  getBlockedList();
};

// 当前页改变
const handleBlockedUsersCurrentChange = (val) => {
  blockedUsersCurrentPage.value = val;
  getBlockedList();
};

// 取消屏蔽用户
const unblockUser = async (conversation) => {
  try {
    await ElMessageBox.confirm(`确定要取消屏蔽用户 ${conversation.otherUsername} 吗?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const res = await unblockUserConversation(conversation.id);
    if (res.code === 200) {
      ElMessage.success('已取消屏蔽，刷新列表后显示');
      // 刷新屏蔽列表
      getBlockedList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消屏蔽失败');
    }
  }
};

const getBlockedList = async () => {
  try {
    const res = await queryUserConversation({
      pageNo: blockedUsersCurrentPage.value,
      pageSize: blockedUsersPageSize.value,
      status: 1 // 根据筛选条件传参
    });
    if (res.code === 200) {
      showBlockedUsersDialog.value = true;
      blockedUsers.value = res.data.list;
      blockedUsersTotal.value = res.data.total;
    }
  } catch (error) {
    ElMessage.error('获取私信列表失败');
  }
};

//全部已读
const markAllMessage = async () => {
  try {
    //是否全部已读二次确认弹框
    await ElMessageBox.confirm(
      `是否确认将所有消息标记为已读？`,
      '全部已读',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    const res = await markAllMessageAsRead();
    if (res.code === 200) {
      ElMessage.success('已全部标记为已读');
      await getUnReadCounts();
    } else {
      ElMessage.error('标记全部已读失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('全部已读失败');
    }
  }
};

// 加载系统通知
const loadSystemNotifications = async () => {
  if (isLoading.value.system) return;
  isLoading.value.system = true;
  try {
    const res = await queryUserInbox({
      type: 0,
      pageNo: systemNotificationCurrentPage.value,
      pageSize: notificationPageSize.value
    });
    if (res.code === 200) {
      if (systemNotificationCurrentPage.value === 1) {
        systemNotifications.value = res.data.list;
      } else {
        systemNotifications.value = [...systemNotifications.value, ...res.data.list];
      }
      systemNotificationTotal.value = res.data.total;
    }
  } catch (error) {
    ElMessage.error('获取系统通知失败');
  } finally {
    isLoading.value.system = false;
  }
};

// 加载笔记通知
const loadNoteNotifications = async () => {
  if (isLoading.value.note) return;
  isLoading.value.note = true;
  try {
    const res = await queryUserInbox({
      type: 1,
      pageNo: noteNotificationCurrentPage.value,
      pageSize: notificationPageSize.value
    });
    if (res.code === 200) {
      if (noteNotificationCurrentPage.value === 1) {
        noteNotifications.value = res.data.list;
      } else {
        noteNotifications.value = [...noteNotifications.value, ...res.data.list];
      }
      noteNotificationTotal.value = res.data.total;
    }
  } catch (error) {
    ElMessage.error('获取笔记通知失败');
  } finally {
    isLoading.value.note = false;
  }
};

// 加载问答通知
const loadQANotifications = async () => {
  if (isLoading.value.QA) return;
  isLoading.value.QA = true;
  try {
    const res = await queryUserInbox({
      type: 2,
      pageNo: QANotificationCurrentPage.value,
      pageSize: notificationPageSize.value
    });
    if (res.code === 200) {
      if (QANotificationCurrentPage.value === 1) {
        QANotifications.value = res.data.list;
      } else {
        QANotifications.value = [...QANotifications.value, ...res.data.list];
      }
      QANotificationTotal.value = res.data.total;
    }
  } catch (error) {
    ElMessage.error('获取问答通知失败');
  } finally {
    isLoading.value.QA = false;
  }
};

// 加载其他通知
const loadOtherNotifications = async () => {
  if (isLoading.value.other) return;
  isLoading.value.other = true;
  try {
    const res = await queryUserInbox({
      type: 3,
      pageNo: otherNotificationCurrentPage.value,
      pageSize: notificationPageSize.value
    });
    if (res.code === 200) {
      if (otherNotificationCurrentPage.value === 1) {
        otherNotifications.value = res.data.list;
      } else {
        otherNotifications.value = [...otherNotifications.value, ...res.data.list];
      }
      otherNotificationTotal.value = res.data.total;
    }
  } catch (error) {
    ElMessage.error('获取其他通知失败');
  } finally {
    isLoading.value.other = false;
  }
};

// 选择通知
const selectNotification = (message) => {
  selectedMessage.value = message;
  if (!message.isRead) {
    markAsRead();
  }
};

// 标记消息为已读
const markAsRead = async () => {
  if (!selectedMessage.value) return;

  try {
    const res = await markMessageAsRead(selectedMessage.value.id);
    if (res.code === 200) {
      if (activeTab.value === 'private') {
        selectedMessage.value.is_read = 1;
        // 这里可以添加更新私信列表的逻辑
      } else {
        selectedMessage.value.isRead = true;
        const list = getCurrentList();
        const index = list.findIndex(m => m.id === selectedMessage.value.id);
        if (index !== -1) list[index].isRead = true;
        await getUnReadCounts();
      }
      ElMessage.success('标记已读成功');
    }
  } catch (error) {
    ElMessage.error('标记已读失败');
  }
};

// 获取当前标签页的消息列表
const getCurrentList = () => {
  switch (activeTab.value) {
    case 'system': return systemNotifications.value;
    case 'note': return noteNotifications.value;
    case 'QA': return QANotifications.value;
    case 'other': return otherNotifications.value;
    default: return [];
  }
};

// 切换标签页
const handleTabChange = (tabName) => {
  selectedMessage.value = null;
  switch (tabName) {
    case 'private':
      privateMessageCurrentPage.value = 1;
      break;
    case 'system':
      systemNotificationCurrentPage.value = 1;
      break;
    case 'note':
      noteNotificationCurrentPage.value = 1;
      break;
    case 'QA':
      QANotificationCurrentPage.value = 1;
      break;
    case 'other':
      otherNotificationCurrentPage.value = 1;
      break;
  }
};

// 滚动加载处理函数
const handleScroll = (tabName, event) => {
  const container = event.target;
  if (container.scrollTop + container.clientHeight >= container.scrollHeight - 20) {
    switch (tabName) {
      case 'private':
        if (!isLoading.value.private &&
          privateMessageCurrentPage.value * privateMessagePageSize.value < privateMessageTotal.value) {
          isLoading.value.private = true;
          privateMessageCurrentPage.value++;
          // 这里可以触发重新加载私信列表
          isLoading.value.private = false;
        }
        break;
      case 'system':
        if (!isLoading.value.system &&
          systemNotificationCurrentPage.value * notificationPageSize.value < systemNotificationTotal.value) {
          isLoading.value.system = true;
          systemNotificationCurrentPage.value++;
          loadSystemNotifications().finally(() => {
            isLoading.value.system = false;
          });
        }
        break;
      case 'note':
        if (!isLoading.value.note &&
          noteNotificationCurrentPage.value * notificationPageSize.value < noteNotificationTotal.value) {
          isLoading.value.note = true;
          noteNotificationCurrentPage.value++;
          loadNoteNotifications().finally(() => {
            isLoading.value.note = false;
          });
        }
        break;
      case 'QA':
        if (!isLoading.value.QA &&
          QANotificationCurrentPage.value * notificationPageSize.value < QANotificationTotal.value) {
          isLoading.value.QA = true;
          QANotificationCurrentPage.value++;
          loadQANotifications().finally(() => {
            isLoading.value.QA = false;
          });
        }
        break;
      case 'other':
        if (!isLoading.value.other &&
          otherNotificationCurrentPage.value * notificationPageSize.value < otherNotificationTotal.value) {
          isLoading.value.other = true;
          otherNotificationCurrentPage.value++;
          loadOtherNotifications().finally(() => {
            isLoading.value.other = false;
          });
        }
        break;
    }
  }
}

// 监听 activeTab 变化
watch(activeTab, (newTab) => {
  switch (newTab) {
    case 'system':
      loadSystemNotifications();
      break;
    case 'note':
      loadNoteNotifications();
      break;
    case 'QA':
      loadQANotifications();
      break;
    case 'other':
      loadOtherNotifications();
      break;
  }
}, { immediate: false });

</script>

<style lang="scss" scoped>
.btn {
  width: 250px;
  height: 40px;
  display: flex;
  align-items: center;
  margin: 10px 0;
}

// message.scss
/* 添加加载动画样式 */
.loading-indicator {
  text-align: center;
  padding: 10px;
  color: #909399;
  font-size: 14px;
}

.loading-spinner {
  display: inline-block;
  margin-right: 8px;
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

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