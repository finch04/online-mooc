<template>
    <div class="websocket-message-container">
        <!-- 顶部用户信息栏 -->
        <div class="user-info-bar">
            <div class="current-user" v-if="currentGroup">
                <img :src="currentGroup.icon" class="user-avatar" />
                <span class="user-name">{{ currentGroup.name }}</span>

                <el-tag v-if="currentGroup.tags" size="small" effect="plain" class="group-tag">
                    {{ currentGroup.tags }}
                </el-tag>
                <span class="group-member-count" style="font-size: 12px;">共{{ currentGroup.memberCount }}人</span>
            </div>
            <div class="current-user" v-else-if="recipientId">
                <img :src="recipientIcon" class="user-avatar" />
                <span class="user-name">{{ recipientName }}</span>
            </div>
            <div class="current-user" v-else>
                <img src="/src/assets/logo-small.png" class="user-avatar" />
                <span class="user-name">请选择一个聊天对象</span>
            </div>

            <div class="connection-control">
                <span @click="isConnected ? disconnectWebSocket() : initWebSocket()" size="medium"
                    class="connect-button bt-round bt-red">
                    {{ isConnected ? '断开连接' : '开始连接' }}
                </span>
                <el-tag class="connection-status" :class="{ connected: isConnected }">
                    {{ isConnected ? '已连接' : '未连接' }}
                </el-tag>
            </div>
        </div>

        <!-- 主聊天区域 -->
        <div class="chat-main-area">
            <!-- 消息列表 -->
            <div class="message-list" ref="messageList" @scroll="handleScroll">
                <div v-if="messages.length === 0" class="message-empty">
                    <el-empty description="暂无消息" />
                </div>
                <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="{
                    'is-me': msg.isMe,
                    'is-private': msg.isPrivate,
                    'is-broadcast': msg.isBroadcast,
                    'is-group': msg.isGroup // 新增群组消息样式类
                }">
                    <div class="message-content">
                        <div class="message-header">
                            <!-- 显示发送者头像和姓名 -->
                            <div v-if="!msg.isMe" class="sender-info">
                                <img :src="msg.senderIcon || '/default-avatar.png'" class="user-avatar" />
                                <span class="sender-name">{{ msg.senderName || '系统' }}</span>
                                <el-tag v-if="msg.isPrivate" size="small" effect="plain" class="private-tag">
                                    <span>{{ msg.isMe ? '私聊给' : '悄悄对你说' }} </span>
                                    <span v-if="msg.isMe">{{ msg.recipientName || msg.senderName }}</span>
                                </el-tag>
                                <el-tag v-if="msg.isGroup" size="small" effect="plain" class="group-tag">
                                    群聊
                                </el-tag>
                            </div>
                            <div v-if="msg.isMe" class="self-info">
                                <el-tag v-if="msg.isPrivate" size="small" effect="plain" class="private-tag">
                                    私聊给 {{ msg.recipientName || msg.targetName }}
                                </el-tag>
                                <el-tag v-if="msg.isGroup" size="small" effect="plain" class="group-tag">
                                    群聊
                                </el-tag>
                                <span class="sender-name">我</span>
                                <img :src="user.icon || '/default-avatar.png'" class="user-avatar" />
                            </div>
                            <span class="send-time">{{ msg.time }}</span>
                        </div>
                        <div class="message-body">
                            {{ msg.content }}
                        </div>
                    </div>
                </div>
            </div>

            <!-- 在线用户列表 -->
            <div class="online-users-panel">
                <div class="panel-header">
                    <span>在线用户 ({{ onlineUserCount }})</span>
                </div>
                <div class="user-list">
                    <!-- 当前用户始终显示在第一位 -->
                    <div class="online-user-item current-user-item">
                        <img :src="user.icon || '/default-avatar.png'" class="user-avatar" />
                        <span class="user-name">{{ user.name }} (我)</span>
                        <el-tag type="primary"> {{ isConnected ? '已连接' : '未连接' }} </el-tag>
                    </div>
                    <!-- 其他在线用户 -->
                    <div v-for="user in otherOnlineUsers" :key="user.id" class="online-user-item"
                        :class="{ 'active': recipientId === user.id.toString() }">
                        <img :src="user.icon || '/default-avatar.png'" class="user-avatar" />
                        <span class="user-name">{{ user.name }}</span>
                        <el-tag class="bt-red bt-round" @click="startPrivateChat(user.id, user.name, user.icon)">
                            私聊
                        </el-tag>
                    </div>
                </div>
                <!-- 群组切换标签 -->
                <div class="tabLab fx-sb">
                    <div class="lable">
                        <span @click="groupCheck('my')" :class="{ act: groupType == 'my' }"
                            class="marg-rt-20">我的群组</span>
                        <span @click="groupCheck('all')" :class="{ act: groupType == 'all' }">全部群组</span>
                    </div>
                </div>
                <!-- 群组列表 -->
                <div class="user-list">
                    <div v-for="group in getGroupList()" :key="group.id" class="online-user-item"
                        @click="enterGroup(group)">
                        <img :src="group.icon || '/default-group-icon.png'" class="user-avatar" />
                        <!-- 修改为 user-avatar 以统一样式 -->
                        <div class="group-info">
                            <span class="group-name" style="font-size: 15px;">{{ group.name }}</span>
                            <div>
                                <!-- <el-button size="small" class="private-chat-btn">
                                进入群组
                            </el-button> -->
                                <el-tag style="margin-right: 12px;" size="small" v-if="groupType === 'all'">{{ group.tags }}</el-tag>
                                <el-tag size="small" @click="joinGroup(group.id)"
                                    v-if="groupType === 'all' && !group.isJoined" class="bt-red bt-round" >
                                    加入
                                </el-tag>
                                <el-tag size="small" v-if="groupType === 'all' && group.isJoined"
                                    class="bt-red bt-round ">已加入</el-tag>

                            </div>
                        </div>

                    </div>
                </div>
                <!-- 分页组件 -->
                <!-- <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                    :current-page="currentPage" :page-sizes="[10, 20, 30]" :page-size="pageSize"
                    layout="total, sizes, prev, pager, next, jumper" :total="totalGroups">
                </el-pagination> -->
            </div>
        </div>

        <!-- 底部消息输入区域 -->
        <div class="message-input-area">
            <div class="chat-mode-indicator" v-if="recipientId || currentGroup">
                <span style="color: red;" v-if="recipientId">私聊给 {{ getRecipientName() }}</span>
                <span v-if="currentGroup">当前群组：{{ currentGroup.name }}</span>
                <span @click="exitCurrentMode" class="exit-private-btn bt-red bt-round">
                    {{ recipientId ? '退出私聊' : '退出群组' }}
                </span>
            </div>
            <el-input v-model="messageContent" placeholder="输入消息内容" size="medium" class="message-input"
                @keyup.enter="handleSendMessage" clearable />
            <el-button @click="handleSendMessage" type="primary" size="medium" class="send-button"
                :disabled="!messageContent.trim()">
                <!-- :disabled="!messageContent.trim()||(!recipientId && !currentGroup)"> -->
                发送
            </el-button>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Client } from '@stomp/stompjs';
import { useUserStore } from "@/store"
import { getOnlineCount, getAllGroups, getMyGroups, addToGroup, getHistoryMessages } from '@/api/message.js'

const store = useUserStore()
const userInfo = ref(store.getUserInfo)
const user = reactive({
    id: userInfo.value.id,
    name: userInfo.value.name,
    icon: userInfo.value.icon
})

const config = {
    wsUrl: 'ws://localhost:10010/sms/ws',
}

const messages = ref([]);
const stompClient = ref(null);
const recipientId = ref('');
const recipientName = ref('');
const recipientIcon = ref('');
const messageContent = ref('');
const isConnected = ref(false);
const messageList = ref(null);
const sentMessageIds = ref(new Set());
const onlineUserCount = ref(0);
const onlineUserInfoList = ref([]);
const myGroups = ref([]); // 我的群组
const allGroups = ref([]); // 所有群组
const currentGroup = ref(null); // 当前激活的群组
const groupType = ref('my'); // 群组类型，默认显示我的群组
const currentPage = ref(1); // 当前页码
const pageSize = ref(10); // 每页显示数量
const totalGroups = ref(0); // 群组总数
const loadingHistory = ref(false); // 加载历史消息的状态
const currentHistoryPage = ref(1); // 当前历史消息的页码
const countInterval = ref(null); // 定时器引用

const otherOnlineUsers = computed(() => {
    return onlineUserInfoList.value.filter(u => u.id.toString() !== user.id.toString());
});

const getRecipientName = () => {
    const user = onlineUserInfoList.value.find(u => u.id.toString() === recipientId.value);
    return user ? user.name : '未知用户';
};

// 滚动到消息列表底部
const scrollToBottom = () => {
    nextTick(() => {
        if (messageList.value) {
            messageList.value.scrollTop = messageList.value.scrollHeight;
        }
    });
};

const startPrivateChat = async (userId, userName, userIcon) => {
    recipientId.value = userId.toString();
    recipientName.value = userName;
    recipientIcon.value = userIcon;
    currentGroup.value = null; // 退出群组状态
    ElMessage.success(`已切换至私聊：${userName}`);
    currentHistoryPage.value = 1;
    messages.value = [];

    await fetchHistoryMessages();
    scrollToBottom();
};

const exitCurrentMode = () => {
    if (recipientId.value) {
        recipientId.value = '';
        ElMessage.info('已退出私聊');
    } else if (currentGroup.value) {
        currentGroup.value = null;
        ElMessage.info('已退出群组聊天');
    }
    messages.value = [];
    scrollToBottom();
};

const handleSendMessage = () => {
    if (!isConnected.value) {
        ElMessage.warning('请先连接服务器');
        return;
    }
    if (!messageContent.value.trim()) {
        ElMessage.warning('消息内容不能为空');
        return;
    }
    if ((!recipientId.value && !currentGroup.value)) {
        ElMessage.warning('请先选择聊天对象');
        return;
    }
    if (recipientId.value) {
        sendPrivateMessage();
    } else if (currentGroup.value) {
        sendGroupMessage();
    } else {
        sendBroadcastMessage();
    }
};

const addMessage = (msg) => {
    const newMessage = {
        content: msg.content || '',
        senderId: msg.senderId || '未知用户',
        senderName: msg.senderName || '未知用户',
        senderIcon: msg.senderIcon || '/default-avatar.png',
        time: msg.time || new Date().toLocaleString(),
        isMe: msg.isMe || false,
        isBroadcast: msg.isBroadcast || false,
        isPrivate: msg.isPrivate || false,
        isGroup: msg.isGroup || false,
        recipientId: msg.recipientId || (msg.isMe ? recipientId.value : user.id.toString()),
        recipientName: msg.recipientName || (msg.isMe ? getRecipientName() : user.name),
        messageId: msg.messageId || Date.now().toString() + Math.random().toString()
    };

    if (newMessage.isMe && sentMessageIds.value.has(newMessage.messageId)) {
        return;
    }

    messages.value.push(newMessage);

    if (newMessage.isMe) {
        sentMessageIds.value.add(newMessage.messageId);
    }

    scrollToBottom();
};

const TOKEN = sessionStorage.getItem('token');

const initWebSocket = () => {
    stompClient.value = new Client({
        brokerURL: config.wsUrl,
        connectHeaders: {
            'user-id': user.id,
            "authorization": TOKEN
        },
        reconnectDelay: 5000,
        debug: (str) => console.log(str)
    });

    stompClient.value.onConnect = (frame) => {
        isConnected.value = true;
        console.log(`已连接，用户ID: ${user.id}`);

        stompClient.value.publish({
            destination: '/app/connect',
            headers: {
                'user-id': user.id,
                "authorization": TOKEN
            }
        });

        const privateSubscription = stompClient.value.subscribe(`/queue/private/${user.id}`, (message) => {
            console.log('收到私聊消息:', message.body);
            try {
                const senderId = message.headers['senderId'];
                const senderName = message.headers['senderName'];
                const senderIcon = message.headers['senderIcon'] || '/default-avatar.png';
                const content = message.body;
                addMessage({
                    content: content,
                    senderId: senderId,
                    senderName: senderName,
                    senderIcon: senderIcon,
                    time: new Date().toLocaleString(),
                    isMe: senderId === user.id.toString(),
                    isPrivate: true,
                    messageId: message.headers['message-id'] || undefined
                });
            } catch (error) {
                console.error('解析私聊消息失败:', error);
                addMessage({
                    content: message.body,
                    senderId: '系统',
                    senderName: '系统',
                    senderIcon: '/default-avatar.png',
                    time: new Date().toLocaleString(),
                    isMe: false,
                    isPrivate: true
                });
            }
        });

        privateSubscription.onError = (error) => {
            console.error('私聊订阅失败:', error);
        };

        const broadcastSubscription = stompClient.value.subscribe(`/topic/messages`, (message) => {
            console.log('收到广播消息:', message.body);
            try {
                const senderId = message.headers['senderId'];
                const senderName = message.headers['senderName'];
                const senderIcon = message.headers['senderIcon'] || '/default-avatar.png';

                const content = message.body;

                if (senderId === user.id.toString()) {
                    return;
                }

                addMessage({
                    content: content,
                    senderId: senderId,
                    senderName: senderName,
                    senderIcon: senderIcon,
                    time: new Date().toLocaleString(),
                    isMe: senderId === user.id.toString(),
                    isBroadcast: true,
                    messageId: message.headers['message-id'] || undefined
                });
            } catch (error) {
                console.error('解析广播消息失败:', error);
                addMessage({
                    content: message.body,
                    senderId: '系统',
                    senderName: '系统',
                    senderIcon: '/default-avatar.png',
                    time: new Date().toLocaleString(),
                    isMe: false,
                    isBroadcast: true
                });
            }
        });
        broadcastSubscription.onError = (error) => {
            console.error('广播订阅失败:', error);
        };

        const onlineUserSubscription = stompClient.value.subscribe('/topic/onlineUsers', (message) => {
            const data = JSON.parse(message.body);
            onlineUserCount.value = data.count;
            onlineUserInfoList.value = data.userInfoList;
        });

        onlineUserSubscription.onError = (error) => {
            console.error('在线用户信息订阅失败:', error);
        };
    };

    stompClient.value.onStompError = (frame) => {
        console.error(`STOMP错误: ${frame.headers.message}`);
        ElMessage.error(`STOMP错误: ${frame.headers.message}`);
    };

    stompClient.value.activate();
};

const disconnectWebSocket = () => {
    if (stompClient.value) {
        stompClient.value.publish({
            destination: '/app/disconnect',
            headers: {
                'user-id': user.id,
                "authorization": TOKEN
            }
        });

        stompClient.value.deactivate();
        isConnected.value = false;
        onlineUserCount.value--;
        ElMessage.info('已断开连接');
    }
};

const sendPrivateMessage = () => {
    if (!stompClient.value?.connected) return;

    const messageId = Date.now().toString() + Math.random().toString();
    const recipientName = getRecipientName();

    stompClient.value.publish({
        destination: `/app/privateMessage`,
        body: JSON.stringify({
            recipientId: recipientId.value,
            content: messageContent.value
        }),
        headers: {
            'sender-id': user.id,
            "authorization": TOKEN,
            'message-id': messageId,
            'recipient-name': recipientName
        }
    });

    addMessage({
        content: messageContent.value,
        senderId: user.id,
        senderName: user.name,
        senderIcon: user.icon,
        time: new Date().toLocaleString(),
        isMe: true,
        isPrivate: true,
        recipientId: recipientId.value,
        recipientName: recipientName,
        messageId: messageId
    });

    messageContent.value = '';
};

const sendBroadcastMessage = () => {
    if (!stompClient.value?.connected) return;

    const messageId = Date.now().toString() + Math.random().toString();

    stompClient.value.publish({
        destination: '/app/broadcast',
        body: messageContent.value,
        headers: {
            'sender-id': user.id,
            "authorization": TOKEN,
            'message-id': messageId
        }
    });

    addMessage({
        content: messageContent.value,
        senderId: user.id,
        senderName: user.name,
        senderIcon: user.icon,
        time: new Date().toLocaleString(),
        isMe: true,
        isBroadcast: true,
        messageId: messageId
    });

    messageContent.value = '';
};

const sendGroupMessage = () => {
    if (!stompClient.value?.connected || !currentGroup.value) return;

    const messageId = Date.now().toString() + Math.random().toString();

    stompClient.value.publish({
        destination: `/app/group/${currentGroup.value.id}`,
        body: messageContent.value,
        headers: {
            'sender-id': user.id,
            'group-id': currentGroup.value.id,
            'group-name': currentGroup.value.name,
            'message-id': messageId,
            'authorization': TOKEN
        }
    });

    addMessage({
        content: messageContent.value,
        senderId: user.id,
        senderName: user.name,
        senderIcon: user.icon,
        time: new Date().toLocaleString(),
        isMe: true,
        isGroup: true,
        targetId: currentGroup.value.id,
        targetName: currentGroup.value.name,
        messageId: messageId
    });

    messageContent.value = '';
};

const fetchOnlineCount = async () => {
    try {
        const response = await getOnlineCount();
        onlineUserCount.value = response.data;
    } catch (error) {
        console.error('获取在线人数失败:', error);
    }
};

const fetchMyGroups = async () => {
    try {
        const groups = await getMyGroups({
            pageNo: currentPage.value,
            pageSize: pageSize.value,
            name: '',
            status: 0
        });
        myGroups.value = groups.data;
        totalGroups.value = groups.data.total;
        updateAllGroupsStatus();
    } catch (error) {
        console.error('获取群组列表失败:', error);
        ElMessage.error('获取群组列表失败');
    }
};

const fetchAllGroups = async () => {
    try {
        const groups = await getAllGroups({
            pageNo: currentPage.value,
            pageSize: pageSize.value,
            name: '',
            status: 0
        });
        allGroups.value = groups.data.list;
        totalGroups.value = groups.data.total;
        updateAllGroupsStatus();
    } catch (error) {
        console.error('获取所有群组失败:', error);
    }
};

const updateAllGroupsStatus = () => {
    if (allGroups.value && myGroups.value) {
        allGroups.value.forEach(group => {
            group.isJoined = myGroups.value.some(mg => mg.id === group.id);
        });
    }
};

const currentGroupSubscription = ref(null);

const enterGroup = async (group) => {
    if (currentGroupSubscription.value) {
        currentGroupSubscription.value.unsubscribe();
    }

    currentGroup.value = group;
    recipientId.value = '';
    currentHistoryPage.value = 1;
    messages.value = [];

    if (stompClient.value?.connected) {
        currentGroupSubscription.value = stompClient.value.subscribe(`/topic/group/${group.id}`, (message) => {
            console.log('收到群组消息:', message);
            try {
                const senderId = message.headers['senderId'];
                const senderName = message.headers['senderName'];
                const senderIcon = message.headers['senderIcon'] || '/default-avatar.png';
                const groupId = message.headers['groupId'];
                const groupName = message.headers['groupName'];

                if (currentGroup.value && currentGroup.value.id === groupId && senderId !== user.id.toString()) {
                    addMessage({
                        content: message.body,
                        senderId,
                        senderName,
                        senderIcon,
                        time: new Date().toLocaleString(),
                        isMe: senderId === user.id.toString(),
                        isGroup: true,
                        targetId: groupId,
                        targetName: groupName
                    });
                }
            } catch (error) {
                console.error('解析群组消息失败:', error);
            }
        });

        ElMessage.success(`已进入群组 "${group.name}"`);
    }

    await fetchHistoryMessages();
    scrollToBottom();
};

const joinGroup = async (groupId) => {
    try {
        await addToGroup(groupId);
        ElMessage.success('加入群组成功');
        await fetchMyGroups();
    } catch (error) {
        console.error('加入群组失败:', error);
        ElMessage.error('加入群组失败');
    }
};

const groupCheck = async (type) => {
    groupType.value = type;
    currentPage.value = 1;
    if (type === 'my') {
        await fetchMyGroups();
    } else if (type === 'all') {
        await fetchAllGroups();
    }
};

const getGroupList = () => {
    return groupType.value === 'my' ? myGroups.value : allGroups.value;
};

const handleSizeChange = (newSize) => {
    pageSize.value = newSize;
    if (groupType.value === 'my') {
        fetchMyGroups();
    } else {
        fetchAllGroups();
    }
};

const handleCurrentChange = (newPage) => {
    currentPage.value = newPage;
    if (groupType.value === 'my') {
        fetchMyGroups();
    } else {
        fetchAllGroups();
    }
};

const handleScroll = () => {
    const { scrollTop, scrollHeight, clientHeight } = messageList.value;
    if (scrollTop === 0 && !loadingHistory.value) {
        currentHistoryPage.value++;
        fetchHistoryMessages();
    }
};

const fetchHistoryMessages = async (callback) => {
    if (loadingHistory.value) return;
    loadingHistory.value = true;

    let query = {
        pageNo: currentHistoryPage.value,
        pageSize: pageSize.value
    };

    if (recipientId.value) {
        query.id = recipientId.value;
        query.type = 1;
    } else if (currentGroup.value) {
        query.id = currentGroup.value.id;
        query.type = 2;
    } else {
        loadingHistory.value = false;
        return;
    }

    try {
        const response = await getHistoryMessages(query);
        const newMessages = response.data.list.map(msg => ({
            content: msg.content,
            senderId: msg.senderId,
            senderName: msg.senderName,
            targetName: msg.targetName,
            senderIcon: msg.senderIcon,
            time: msg.sentAt.toLocaleString(),
            isMe: msg.senderId === user.id,
            isPrivate: query.type === 1,
            isGroup: query.type === 2,
            messageId: msg.id.toString()
        }));

        messages.value = [...newMessages.reverse(), ...messages.value];
    } catch (error) {
        console.error('获取历史消息失败:', error);
    } finally {
        loadingHistory.value = false;
    }
};

onMounted(async () => {
    fetchOnlineCount();
    await fetchMyGroups();
    await fetchAllGroups();
    initWebSocket();

    // if (allGroups.value.length > 0) {
    //     const firstGroup = allGroups.value[0];
    //     enterGroup(firstGroup);
    //     // scrollToBottom();
    // }

    countInterval.value = setInterval(fetchOnlineCount, 3000);
});

onUnmounted(() => {
    if (countInterval.value) clearInterval(countInterval.value);
    disconnectWebSocket();
    if (stompClient.value) {
        stompClient.value.publish({
            destination: '/app/disconnect',
            headers: {
                'user-id': user.id,
                "authorization": TOKEN
            }
        });
        stompClient.value.deactivate();
    }
    if (currentGroupSubscription.value) {
        currentGroupSubscription.value.unsubscribe();
    }
});

window.addEventListener('beforeunload', () => {
    disconnectWebSocket();
});

</script>

<style lang="scss" scoped>
.websocket-message-container {
    display: flex;
    flex-direction: column;
    height: 700px;
    /* Increased initial height */
    width: 100%;
    border-radius: 8px;
    background-color: #f5f7fa;
    overflow: hidden;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.user-info-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    background-color: #409eff;
    color: white;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    z-index: 10;
    flex-shrink: 0;
    /* Prevent shrinking */

    .current-user {
        display: flex;
        align-items: center;
        gap: 12px;

        .user-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            border: 2px solid white;
            object-fit: cover;
        }

        .user-name {
            font-weight: 600;
            font-size: 18px;
        }
    }

    .connection-control {
        display: flex;
        align-items: center;
        gap: 12px;

        .connect-button {
            padding: 8px 16px;
            font-size: 14px;
        }

        .connection-status {
            padding: 8px 12px;
            border-radius: 4px;
            font-weight: 500;

            &.connected {
                background-color: #67c23a;
                color: white;
            }
        }
    }
}

.chat-main-area {
    display: flex;
    flex: 1;
    overflow: hidden;
    min-height: 0;
    /* Fix for flexbox scrolling */
}

.message-list {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
    background-color: #f5f7fa;
    display: flex;
    flex-direction: column;
    min-height: 0;
    /* Important for scrolling */

    .message-empty {
        display: flex;
        justify-content: center;
        align-items: center;
        height: 100%;
    }

    .message-item {
        margin-bottom: 16px;
        max-width: 80%;
        width: fit-content;
        flex-shrink: 0;
        /* Prevent message items from shrinking */

        &.is-me {
            align-self: flex-end;

            .message-content {
                align-items: flex-end;

                .message-body {
                    background-color: #409eff;
                    color: white;
                    border-radius: 12px 12px 0 12px;
                }

                &.is-private .message-body {
                    background-color: #8e44ad;
                }
            }
        }

        &:not(.is-me) {
            align-self: flex-start;

            .message-body {
                background-color: white;
                border-radius: 12px 12px 12px 0;
            }

            &.is-private .message-body {
                background-color: #f0e6ff;
                border-left: 3px solid #8e44ad;
            }
        }

        .message-content {
            display: flex;
            flex-direction: column;
            gap: 6px;

            .message-header {
                display: flex;
                align-items: center;
                gap: 8px;

                .sender-info,
                .self-info {
                    display: flex;
                    align-items: center;
                    gap: 8px;

                    .user-avatar {
                        width: 32px;
                        height: 32px;
                        border-radius: 50%;
                        object-fit: cover;
                    }

                    .sender-name {
                        font-weight: 500;
                        font-size: 14px;
                        color: #303133;
                    }

                    .private-tag {
                        margin-left: 6px;
                        border-color: #8e44ad;
                        color: #8e44ad;
                        background-color: rgba(142, 68, 173, 0.1);
                    }
                }

                .self-info {
                    flex-direction: row-reverse;
                }

                .send-time {
                    font-size: 12px;
                    color: #909399;
                    margin-left: 10px;
                }
            }

            .message-body {
                padding: 10px 14px;
                box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
                word-break: break-word;
                line-height: 1.5;
                min-width: 60px;
                max-width: 100%;
            }
        }
    }
}

.online-users-panel {
    width: 240px;
    background-color: white;
    border-left: 1px solid #e4e7ed;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    flex-shrink: 0;

    .panel-header {
        padding: 14px 16px;
        background-color: #409eff;
        color: white;
        font-weight: 500;
        font-size: 16px;
        border-bottom: 1px solid #e4e7ed;
        flex-shrink: 0;
    }

    .user-list {
        flex: 1;
        overflow-y: auto;
        padding: 8px;
        min-height: 0;

        .online-user-item {
            display: flex;
            align-items: center;
            padding: 10px 12px;
            border-radius: 6px;
            margin-bottom: 6px;
            transition: all 0.2s;
            flex-shrink: 0;

            &:hover {
                background-color: #f5f7fa;
            }

            &.active {
                background-color: #e6f7ff;
                border-left: 3px solid #409eff;
            }

            &.current-user-item {
                background-color: #f0f7ff;
                border-left: 3px solid #409eff;
                margin-bottom: 12px;
                font-weight: 500;
            }

            .user-avatar {
                width: 36px;
                height: 36px;
                border-radius: 50%;
                margin-right: 12px;
                object-fit: cover;
            }

            .user-name {
                flex: 1;
                font-size: 14px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }

            .private-chat-btn,
            .join-btn,
            .active-btn {
                margin-left: auto;
                /* 将按钮靠左 */
            }

            /* 新增样式，使内容居中 */
            &:not(.current-user-item) {
                justify-content: center;
            }

            .join-btn {
                padding: 5px 10px;
                font-size: 12px;
                background-color: #67c23a;
                color: white;
                border: none;

                &:hover {
                    background-color: #5daf34;
                }
            }

            .active-btn {
                padding: 5px 10px;
                font-size: 12px;
                background-color: #e4e7ed;
                color: #909399;
                border: none;
            }
        }
    }

    .tabLab {
        margin-bottom: 20px;
        display: flex;
        justify-content: center;

        /* 使内容居中 */
        .lable {
            display: flex;
            align-items: center;

            span {
                display: inline-block;
                width: 96px;
                text-align: center;
                cursor: pointer;
            }
        }

        .act {
            background: #19232B;
            border-radius: 20px;
            color: #fff;
            font-size: 14px;
            font-weight: 600;
            padding: 5px 20px;
        }
    }

    .el-pagination {
        padding: 10px;
    }
}

.message-input-area {
    padding: 16px 20px;
    border-top: 1px solid #e4e7ed;
    background-color: white;
    display: flex;
    align-items: center;
    gap: 16px;
    position: relative;
    flex-shrink: 0;
    /* Prevent input area from shrinking */

    .chat-mode-indicator {
        position: absolute;
        top: -30px;
        left: 0;
        right: 0;
        background-color: #e6f7ff;
        padding: 6px 20px;
        font-size: 14px;
        color: #409eff;
        display: flex;
        align-items: center;
        justify-content: space-between;

        .exit-private-btn {
            padding: 4px 8px;
            font-size: 12px;
        }
    }

    .message-input {
        flex: 1;

        :deep(.el-input__inner) {
            border-radius: 20px;
            padding-left: 16px;
            padding-right: 16px;
            height: 40px;
            line-height: 40px;
        }
    }

    .send-button {
        border-radius: 20px;
        padding: 10px 24px;
        font-size: 14px;

        &:disabled {
            opacity: 0.6;
        }
    }
}

/* Improved scrollbar styling */
::-webkit-scrollbar {
    width: 8px;
    height: 8px;
}

::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.2);
    border-radius: 4px;

    &:hover {
        background-color: rgba(0, 0, 0, 0.3);
    }
}

::-webkit-scrollbar-track {
    background-color: rgba(0, 0, 0, 0.05);
}

.private-tag {
    margin-left: 6px;
    border-color: #8e44ad;
    color: #8e44ad;
    background-color: rgba(142, 68, 173, 0.1);
    white-space: nowrap;
    max-width: 180px;
    overflow: hidden;
    text-overflow: ellipsis;
}
</style>