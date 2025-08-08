<template>
    <div class="message-container">
        <div class="message-list" @scroll="handleScroll">
            <div v-if="sortedMessages.length === 0" class="message-empty">
                <el-empty description="暂无私信" />
            </div>
            <div v-for="item in sortedMessages" :key="item.id" class="message-item"
                :class="{ 'unread': item.unreadCount > 0, 'selected': selectedMessage?.id === item.id }"
                @click="selectMessage(item)">
                <div class="message-avatar">
                    <el-badge v-if="item.unReadCount > 0" :value="item.unReadCount" style="margin-right: 3px;">
                        <el-avatar :src="item.otherAvatar" />
                    </el-badge>
                    <el-avatar v-else :src="item.otherAvatar" />
                </div>
                <div class="message-meta">
                    <!-- 对方用户名 -->
                    <!-- <span class="sender-name">{{ item.otherUsername }}</span> -->
                    <!-- 未读消息数 -->
                    <!-- <span class="unread-badge">{{ }}</span> -->
                </div>
                <div class="message-content">
                    <div class="message-header">
                        <span class="sender-name">{{ item.otherUsername }}</span>
                        <!-- <span class="send-time">{{ item.lastMessageTime }}</span> -->
                    </div>
                    <!-- 显示最后一条消息内容 -->
                    <div class="message-preview" v-if="item.lastMessage">
                        <!-- {{ item.lastMessage.length > 10 ? item.lastMessage.slice(0, 10) + '...' : item.lastMessage }} -->
                    </div>
                </div>
            </div>
        </div>
        <!-- 右侧聊天区域 -->
        <div class="message-detail" v-if="selectedMessage">
            <div class="message-detail-header">
                <div class="sender-info">
                    <el-avatar :src="selectedMessage.otherAvatar" />
                    <span class="sender-name">{{ selectedMessage.otherUsername }}</span>
                </div>
                <div class="action-buttons" style="text-align: right; display: flex; align-items: center;">
                    <el-dropdown trigger="click">
                        <span class="el-dropdown-link">
                            更多 <i class="el-icon-arrow-down"></i>
                        </span>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item @click="blockConversation(selectedMessage.id)">
                                    {{ selectedMessage.status === 0 ? '屏蔽会话' : '解除屏蔽' }}
                                </el-dropdown-item>
                                <el-dropdown-item @click="deleteConversation(selectedMessage.id)"
                                    style="color: #f56c6c;">
                                    删除会话
                                </el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </div>

            <!-- 添加 ref 用于获取聊天区域 DOM -->
            <div ref="conversationRef" class="message-conversation" @scroll="handleChatScroll">
                <!-- 加载更多提示 -->
                <div v-if="isLoadingOlder" class="loading-indicator">
                    <i class="el-icon-loading loading-spinner"></i>加载更多...
                </div>
                <div v-for="(msg, index) in sortedConversationMessages" :key="index" class="message-bubble-wrapper"
                    :class="{ 'is-me': msg.senderId === myId }">
                    <!-- 调试信息 -->
                    <!-- <div  class="debug-info">
                        <small>SenderID: {{ msg.senderId }}, MyID: {{ myId }}, IsMe: {{ msg.senderId === myId }}</small>
                    </div> -->
                    <!-- 头像 -->
                    <el-avatar v-if="msg.senderId === myId" :src="msg.senderIcon" size="small" class="bubble-avatar" />
                    <el-avatar v-else :src="msg.senderIcon" size="small" class="bubble-avatar" />
                    <!-- 消息内容 -->
                    <div class="bubble-content-wrapper">
                        <div class="bubble-content">
                            {{ msg.content }}
                        </div>
                        <div class="bubble-time">
                            {{ msg.pushTime }}
                        </div>
                    </div>
                </div>
            </div>
            <div class="message-reply">
                <el-input v-model="replyContent" type="textarea" :rows="3" placeholder="请输入回复内容"></el-input>
                <div class="reply-actions">
                    <span class="bt bt-round" style="width: 30%;float: right;" @click="sendMessage">发送</span>
                    <!-- <el-button @click="$emit('mark-read')" v-if="selectedMessage.unreadCount > 0">标记已读</el-button> -->
                </div>
            </div>
        </div>
        <div class="message-empty" v-else>
            <el-empty description="请选择一条消息查看详情" />
        </div>
        <!-- <WebSocketMessage /> -->
    </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus'; // 引入 ElMessage
import { blockUserConversation, unblockUserConversation, deleteUserConversation, queryUserConversation, getMessageRecords, sendMessageToUser } from "../../../api/message";
// import  WebSocketMessage from './WebSocketMessage.vue';

const props = defineProps({
    myId: {
        type: Number,
        default: 0
    },
    myIcon: {
        type: String,
        default: ''
    },
    filterTab: {
        type: String,
        default: 'normal'
    },
    privateMessagePageSize: {
        type: Number,
        default: 10
    },
    privateMessageCurrentPage: {
        type: Number,
        default: 1
    },
    messageHistoryPageSize: {
        type: Number,
        default: 10
    },
    messageHistoryCurrentPage: {
        type: Number,
        default: 1
    }
});

const emit = defineEmits(['select-message', 'scroll', 'send-reply', 'mark-read', 'load-more-chat', 'page-change', 'update:messages', 'reload-private-messages']);

const selectedMessage = ref(null);
const replyContent = ref('');
const conversationRef = ref(null);
const isLoadingOlder = ref(false);
const hasMoreOlder = ref(true);
const isSending = ref(false);
const prevScrollHeight = ref(0);
const messages = ref([]);
const conversationMessages = ref([]);
const messageHistoryTotal = ref(0);
const currentPage = ref(props.messageHistoryCurrentPage);

const privateMessagePageSize = ref(10);
const privateMessageCurrentPage = ref(1);
const privateMessageTotal = ref(0);
const isLoading = ref(false); // 加载状态
const hasMoreData = ref(true); // 是否还有更多数据


// 对 messages 数组进行排序
const sortedMessages = computed(() => {
    return [...messages.value].sort((a, b) => {
        return new Date(a.lastMessageTime) - new Date(b.lastMessageTime);
    });
});

// 对 conversationMessages 数组进行排序
const sortedConversationMessages = computed(() => {
    return [...conversationMessages.value].sort((a, b) => {
        return new Date(a.pushTime) - new Date(b.pushTime);
    });
});

const selectMessage = async (message) => {
    selectedMessage.value = message;

    // 重置页码和消息数据
    emit('select-message', {
        ...message,
        conversationMessages: [],
        conversationCurrentPage: 1
    });
    // 重置加载状态
    hasMoreOlder.value = true;
    currentPage.value = 1; // 重置当前页码
    // 滚动到顶部
    await nextTick();
    // 清 0 未读消息数并标记为已读
    if (message.unReadCount > 0) {
        console.log('清 0 未读消息数并标记为已读', message)
        message.unReadCount = 0;
    }
    scrollToBottom();



    // 加载对话记录
    await loadConversationMessages(message.otherUserId, 1);
};

const sendMessage = async () => {
    if (isSending.value) return; // 防止重复发送
    if (!replyContent.value.trim()) {
        ElMessage.warning('请输入回复内容');
        return;
    }

    isSending.value = true;

    try {
        const res = await sendMessageToUser({
            userId: selectedMessage.value.otherUserId,
            content: replyContent.value
        });
        console.log('发送消息成功:', res);
        if (res.code === 200) {
            ElMessage.success('回复发送成功');
            // // 更新左侧会话列表的最后一条消息
            // updateLastMessage(replyContent.value);

            // 将新消息添加到 conversationMessages 数组中
            const newMessage = {
                senderId: props.myId,
                senderIcon: props.myIcon,
                receiverIcon: selectedMessage.value.otherAvatar,
                content: replyContent.value,
                pushTime: new Date().toLocaleString() // 假设使用当前时间作为消息发送时间
            };
            conversationMessages.value.push(newMessage);

            replyContent.value = '';

            // 使用 nextTick 确保 DOM 更新后再滚动
            await nextTick();
            scrollToBottom();

            // 触发重新加载私信列表
            await loadPrivateMessages(true);
        }else{
            console.log('发送消息失败:', res);
            ElMessage.error(res.msg);
        }
    } catch (error) {
        console.error('发送消息失败:', error);
    } finally {
        isSending.value = false;
    }
};

// 更新左侧会话列表的最后一条消息和时间
const updateLastMessage = (content) => {
    if (!selectedMessage.value) return;

    const index = messages.value.findIndex(msg => msg.id === selectedMessage.value.id);
    if (index !== -1) {
        messages.value[index].lastMessage = content;
        messages.value[index].lastMessageTime = new Date().toLocaleString();
    }
};

const scrollToBottom = () => {
    if (conversationRef.value) {
        conversationRef.value.scrollTop = conversationRef.value.scrollHeight;
    }
};

// 滚动处理 - 追加数据
const handleScroll = (event) => {
    const container = event.target;

    // 当滚动到底部附近时加载更多数据
    if (container.scrollTop + container.clientHeight >= container.scrollHeight - 50) {
        if (!isLoading.value && hasMoreData.value) {
            isLoading.value = true;
            privateMessageCurrentPage.value++;
            loadPrivateMessages(true).finally(() => {
                isLoading.value = false;
            });
        }
    }
};

// 组件初始化时加载数据
onMounted(async () => {
    await loadPrivateMessages(false);
    await nextTick();
    scrollToBottom();
});

// 监听筛选条件变化，重新加载数据
watch(() => props.filterTab, async () => {
    privateMessageCurrentPage.value = 1;
    await loadPrivateMessages(false);
});

// 监听消息列表变化，有新消息时滚动到底部
watch(() => sortedConversationMessages.value, async (newVal, oldVal) => {
    // 使用 nextTick 确保 DOM 更新后再滚动
    await nextTick();

    // 如果是新发送的消息或首次加载，滚动到底部
    if (isSending.value || oldVal.length === 0) {
        scrollToBottom();
    } else if (newVal.length > oldVal.length && newVal[0].id !== oldVal[0]?.id) {
        // 如果是加载了更旧的消息，保持原来的滚动位置
        if (conversationRef.value) {
            const newScrollHeight = conversationRef.value.scrollHeight;
            conversationRef.value.scrollTop = newScrollHeight - prevScrollHeight.value;
        }
    }
});

// 处理聊天区域滚动事件
const handleChatScroll = async (event) => {
    const container = event.target;

    // 防止发送消息时触发滚动加载
    if (isSending.value) return;

    // 向上滚动到顶部时加载更多
    if (container.scrollTop === 0 && hasMoreOlder.value && !isLoadingOlder.value) {
        // 检查是否还有更多数据
        if (currentPage.value * props.messageHistoryPageSize >= messageHistoryTotal.value) {
            hasMoreOlder.value = false;
            return;
        }

        isLoadingOlder.value = true;

        try {
            // 保存当前滚动高度
            prevScrollHeight.value = container.scrollHeight;

            // 触发加载更多事件
            const nextPage = currentPage.value + 1;
            await loadConversationMessages(selectedMessage.value.otherUserId, nextPage);
            currentPage.value = nextPage; // 更新当前页码

            // 等待数据加载完成
            await nextTick();

            // 调整滚动位置，保持在原来的位置
            const newScrollHeight = container.scrollHeight;
            container.scrollTop = newScrollHeight - prevScrollHeight.value;
        } catch (error) {
            console.error('加载更多消息失败:', error);
        } finally {
            isLoadingOlder.value = false;
        }
    }
};

// 屏蔽会话方法
const blockConversation = async (id) => {
    try {
        const res = await blockUserConversation(id)
        if (res.code === 200) {
            ElMessage.success("屏蔽用户成功");
            // 重置选中状态并刷新列表
            selectedMessage.value = null;
            await loadPrivateMessages();
        }
    } catch (error) {
        ElMessage.error('操作失败');
    }
};

// 新增删除会话方法
const deleteConversation = async (id) => {
    try {
        await ElMessageBox.confirm('确认删除该会话？删除该会话并不会一起删除聊天记录，您仍可以通过重新私聊进行会话', '警告', {
            type: 'warning'
        });

        const res = await deleteUserConversation(id);
        if (res.code === 200) {
            ElMessage.success('会话删除成功');
            // 重置选中状态并刷新列表
            selectedMessage.value = null;
            await loadPrivateMessages();
        }
    } catch (error) {
        if (error !== 'cancel') ElMessage.error('删除失败');
    }
};

// 加载私信列表 - 修改为支持追加
const loadPrivateMessages = async (append = false) => {
    try {
        const res = await queryUserConversation({
            pageNo: privateMessageCurrentPage.value,
            pageSize: privateMessagePageSize.value,
            status: 0 // 根据筛选条件传参
        });

        if (res.code === 200) {
            const newMessages = res.data.list || [];

            // 更新总数据量
            privateMessageTotal.value = res.data.total;

            // 判断是否还有更多数据
            hasMoreData.value = (privateMessageCurrentPage.value * privateMessagePageSize.value) < privateMessageTotal.value;

            if (append) {
                // 追加数据模式
                // 过滤掉已有的消息，避免重复
                const existingIds = messages.value.map(msg => msg.id);
                const uniqueNewMessages = newMessages.filter(msg => !existingIds.includes(msg.id));

                // 追加到现有列表
                messages.value = [...messages.value, ...uniqueNewMessages];
            }
            else {
                // 覆盖模式（如切换筛选条件或初始加载）
                messages.value = newMessages;
            }
        }
    } catch (error) {
        ElMessage.error('获取私信列表失败');
    }
};

// 加载对话记录
const loadConversationMessages = async (otherUserId, pageNo) => {
    try {
        const res = await getMessageRecords({
            otherUserId: otherUserId,
            pageNo: pageNo,
            pageSize: props.messageHistoryPageSize
        });
        if (res.code === 200) {
            // 只在API调用成功后才更新当前页码
            if (pageNo === 1) {
                conversationMessages.value = res.data.list;
            } else {
                let newConversationMessages = [...conversationMessages.value];
                res.data.list.forEach(newMessage => {
                    const index = newConversationMessages.findIndex(msg => msg.id === newMessage.id);
                    if (index !== -1) {
                        newConversationMessages[index] = newMessage;
                    } else {
                        newConversationMessages.unshift(newMessage);
                    }
                });
                conversationMessages.value = newConversationMessages;
            }
            messageHistoryTotal.value = res.data.total;
        }
    } catch (error) {
        ElMessage.error('加载对话记录失败');
    }
};

</script>
<style lang="scss" scoped>
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

        .message-avatar {
            flex-shrink: 0;
            margin-right: 12px;
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

        .unread-count-badge {
            background-color: #f56c6c;
            color: white;
            font-size: 12px;
            padding: 2px 6px;
            border-radius: 10px;
            margin-left: 8px;
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
            display: flex;
            flex-direction: column;
            gap: 16px;

            .message-bubble-group {
                display: flex;
                flex-direction: column;
                gap: 8px;
            }

            .message-bubble-wrapper {
                display: flex;
                max-width: 75%;
                gap: 12px;
                align-items: flex-start;

                .bubble-content-wrapper {
                    .bubble-content {
                        // 统一气泡样式
                        background-color: #f5f5f5;
                        color: #333;
                        border-radius: 18px;
                        padding: 10px 14px;
                        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
                    }

                    .bubble-time {
                        // 统一时间样式
                        color: #909399;
                        font-size: 12px;
                        padding-left: 14px;
                        text-align: left;
                    }
                }

                // 我的消息样式（右对齐），仅修改颜色
                &.is-me {
                    align-self: flex-end;
                    flex-direction: row-reverse;

                    .bubble-content-wrapper {
                        .bubble-content {
                            background-color: #409EFF;
                            color: white;
                        }

                        .bubble-time {
                            padding-right: 14px;
                            text-align: right;
                        }
                    }
                }
            }
        }

        .message-reply {
            margin-top: 20px;

            .reply-actions {
                margin-top: 10px;
            }
        }
    }
}
</style>