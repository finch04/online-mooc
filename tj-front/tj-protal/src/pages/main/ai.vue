<template>
    <div class="aiChatWrapper">
        <div class="container">
            <Breadcrumb data="AI聊天"></Breadcrumb>
        </div>
        <div class="chatLayout fx">
            <!-- 会话列表 -->
            <div class="sessionList">
                <button @click="createSession">新增会话</button>
                <ul>
                    <li v-for="session in userSessionList" :key="session.id"
                        :class="{ active: selectedSessionId === session.sessionId }"
                        @click="selectSession(session.sessionId)">
                        {{ session.sessionId }}
                        <button @click="deleteSession(session.id, $event)">删除</button>
                    </li>
                </ul>
            </div>
            <!-- 聊天区域 -->
            <div class="chatItems container bg-wt">
                <!-- 聊天消息显示区域 -->
                <div class="chatMessages" ref="chatMessages">
                    <div class="message" v-for="(msg, index) in chatHistory" :key="index">
                        <div class="userMessage" v-if="msg.type === 'user'">
                            <div class="messageContent">{{ msg.content }}</div>
                        </div>
                        <div class="assistantMessage" v-else>
                            <div class="thinking" v-if="msg.thinkingContent">
                                <div class="thinkingLabel">AI思考中...</div>
                                <vue-markdown class="thinkingContent" :source="msg.thinkingContent"></vue-markdown>
                            </div>
                            <div class="messageContent" v-if="msg.showMarkdown">
                                <vue-markdown :source="msg.processedContent"></vue-markdown>
                            </div>
                            <div class="messageContent" v-else>
                                {{ msg.processedContent }}
                            </div>
                            <div class="typingIndicator" v-if="msg.isTyping">
                                <span class="dot"></span>
                                <span class="dot"></span>
                                <span class="dot"></span>
                            </div>
                            <!-- 添加复制图标按钮 -->
                            <button @click="copyMessage(msg.processedContent)" class="copyButton">
                                <CopyDocument />
                            </button>
                        </div>
                    </div>
                </div>
                <!-- 输入框和发送按钮 -->
                <div class="inputArea fx">
                    <input type="text" v-model="inputMessage" placeholder="请输入聊天内容" @keyup.enter="sendMessage"
                           :disabled="isLoading">
                    <div class="buttonGroup">
                        <button @click="sendMessage" :disabled="isLoading || !inputMessage">发送</button>
                        <button @click="stopStream" v-if="isStreaming" class="stopButton">停止</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { createUserSession, getUserSessionList, deleteUserSession, getChatRecord } from '@/api/ai.js';
import Breadcrumb from '@/components/Breadcrumb.vue';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import VueMarkdown from 'vue3-markdown-it';
import { CopyDocument } from '@element-plus/icons-vue'; // 引入复制图标

// 用户信息
const TOKEN = sessionStorage.getItem('token');

// 聊天历史记录
const chatHistory = ref([]);
// 输入的消息
const inputMessage = ref('');
// 聊天消息显示区域的引用
const chatMessages = ref(null);
// 是否正在加载
const isLoading = ref(false);
// 是否正在流式传输
const isStreaming = ref(false);
// 中止控制器
const abortController = ref(null); // 初始化为 null
// 结束标识
const END_FLAG = '[END]';
// 用户会话列表
const userSessionList = ref([]);
// 当前选中的会话 ID
const selectedSessionId = ref(null);

// 获取用户会话列表
const fetchUserSessionList = async () => {
    try {
        const sessions = await getUserSessionList();
        userSessionList.value = sessions;
        if (sessions.length > 0 && !selectedSessionId.value) {
            selectSession(sessions[0].sessionId);
        }
    } catch (error) {
        console.error('获取用户会话列表失败:', error);
        ElMessage.error('获取用户会话列表失败: ' + (error.message || '未知错误'));
    }
};

// 创建用户会话关联
const createSession = async () => {
    try {
        await createUserSession();
        await fetchUserSessionList();
        ElMessage.success('创建会话成功');
    } catch (error) {
        console.error('创建会话失败:', error);
        ElMessage.error('创建会话失败: ' + (error.message || '未知错误'));
    }
};

// 删除用户会话关联
const deleteSession = async (id, event) => {
    event.stopPropagation();
    try {
        await deleteUserSession(id);
        await fetchUserSessionList();
        ElMessage.success('删除会话成功');
    } catch (error) {
        console.error('删除会话失败:', error);
        ElMessage.error('删除会话失败: ' + (error.message || '未知错误'));
    }
};

// 选择会话
const selectSession = async (sessionId) => {
    selectedSessionId.value = sessionId;
    try {
        const records = await getChatRecord(sessionId);
        chatHistory.value = records.map(record => {
            const content = JSON.parse(record.content);
            return {
                type: content.role,
                content: content.text,
                isTyping: false,
                showMarkdown: false,
                processedContent: content.text,
                thinkingContent: ''
            };
        });
        await scrollToBottom();
    } catch (error) {
        console.error('加载会话历史记录失败:', error);
        ElMessage.error('加载会话历史记录失败: ' + (error.message || '未知错误'));
    }
};

// 停止流式传输
const stopStream = () => {
    if (abortController.value) {
        abortController.value.abort();
        isStreaming.value = false;
        isLoading.value = false;

        const lastMessage = chatHistory.value[chatHistory.value.length - 1];
        if (lastMessage && lastMessage.type === 'assistant') {
            lastMessage.isTyping = false;
        }

        abortController.value = new AbortController(); // 重置 abortController
        ElMessage({
            message: '已停止当前对话',
            type: 'info'
        });
    }
};

const sendMessage = async () => {
    if (!inputMessage.value.trim()) {
        ElMessage.error('请输入聊天内容');
        return;
    }

    // 防止重复发送
    if (isLoading.value) return;

    if (!selectedSessionId.value) {
        ElMessage.error('请选择一个会话');
        return;
    }

    isLoading.value = true;
    const userMessage = inputMessage.value;
    chatHistory.value.push({ type: 'user', content: userMessage });
    inputMessage.value = '';

    await scrollToBottom();
    try {
        isStreaming.value = true;
        // 添加占位消息
        chatHistory.value.push({
            type: 'assistant',
            content: '',
            isTyping: true,
            showMarkdown: false,
            processedContent: '',
            thinkingContent: ''
        });

        const assistantMessage = chatHistory.value[chatHistory.value.length - 1];
        let content = '';
        let thinkingContent = '';
        let inThinkingTag = false;
        const AI_API_PREFIX = "http://localhost:10010/ct";

        // 构建查询参数
        const queryParams = new URLSearchParams();
        queryParams.append('message', userMessage);
        queryParams.append('sessionId', selectedSessionId.value);

        abortController.value = new AbortController(); // 每次请求前重置 abortController

        await fetchEventSource(`${AI_API_PREFIX}/chat/?${queryParams.toString()}`, {
            method: 'GET',
            headers: {
                'Accept': 'text/event-stream',
                "authorization": TOKEN
            },
            signal: abortController.value.signal,
            openWhenHidden: true, // 保持连接即使页面不可见

            onopen(response) {
                if (!response.ok || response.headers.get('content-type') !== 'text/event-stream') {
                    throw new Error(`请求失败: ${response.status}`);
                }
            },

            onmessage(msg) {
                // 后端主动关闭时会发送[DONE]事件
                if (msg.data === '[DONE]') {
                    assistantMessage.isTyping = false;
                    isStreaming.value = false;
                    abortController.value.abort(); // 主动关闭连接
                    console.log('SSE 数据接收完成');
                    return;
                }

                if (msg.data) {
                    if (msg.data === '<think>') {
                        inThinkingTag = true;
                    } else if (msg.data === '</think>') {
                        inThinkingTag = false;
                        assistantMessage.thinkingContent = thinkingContent;
                        thinkingContent = '';
                    } else if (inThinkingTag) {
                        thinkingContent += msg.data;
                        assistantMessage.thinkingContent = thinkingContent;
                    } else {
                        content += msg.data;
                        const processed = processContent(content);
                        assistantMessage.processedContent = processed.content;
                        assistantMessage.showMarkdown = processed.showMarkdown;
                    }
                    scrollToBottom();
                }
            },

            onclose() {
                // 连接关闭时清理状态
                assistantMessage.isTyping = false;
                isStreaming.value = false;
            },

            onerror(err) {
                console.error('流式传输错误:', err);
                // 不自动重试
                assistantMessage.isTyping = false;
                isStreaming.value = false;
                if (abortController.value) {
                    abortController.value.abort();
                }

                // 只有非主动中断的错误才显示
                if (err.name !== 'AbortError') {
                    assistantMessage.content = '对话出错: ' + (err.message || '连接中断');
                }
            }
        });
    } catch (error) {
        console.error('请求失败:', error);
        if (error.name !== 'AbortError') {
            ElMessage.error('请求失败: ' + (error.message || '未知错误'));

            const lastMsg = chatHistory.value[chatHistory.value.length - 1];
            if (lastMsg?.type === 'assistant') {
                lastMsg.content = '请求失败: ' + (error.message || '未知错误');
                lastMsg.isTyping = false;
            }
        }
    } finally {
        isLoading.value = false;
        isStreaming.value = false;
        await scrollToBottom();
    }
};

// 复制消息的方法
const copyMessage = (text) => {
    navigator.clipboard.writeText(text)
      .then(() => {
            ElMessage({
                message: '复制成功',
                type: 'success'
            });
        })
      .catch((error) => {
            ElMessage({
                message: '复制失败: ' + error.message,
                type: 'error'
            });
        });
};

// 滚动到聊天底部
const scrollToBottom = async () => {
    await nextTick();
    if (chatMessages.value) {
        chatMessages.value.scrollTop = chatMessages.value.scrollHeight;
    }
};

// 处理内容，提取<think>标签和主要内容
const processContent = (content) => {
    let showMarkdown = true;
    let thinkingContent = '';
    let processedContent = content;

    // 处理<think>标签
    const thinkStart = '<think>';
    const thinkEnd = '</think>';
    const thinkStartIndex = content.indexOf(thinkStart);
    const thinkEndIndex = content.indexOf(thinkEnd);

    if (thinkStartIndex !== -1 && thinkEndIndex !== -1) {
        // 提取思考内容
        thinkingContent = content.slice(thinkStartIndex + thinkStart.length, thinkEndIndex);
        // 处理主要内容
        processedContent = content.slice(0, thinkStartIndex) + content.slice(thinkEndIndex + thinkEnd.length);
    } else if (thinkStartIndex !== -1) {
        // 只有开始标签没有结束标签
        thinkingContent = content.slice(thinkStartIndex + thinkStart.length);
        processedContent = content.slice(0, thinkStartIndex);
    }

    // 如果内容为空，则禁用Markdown渲染
    if (processedContent.trim() === '') {
        showMarkdown = false;
    }

    return {
        content: processedContent,
        showMarkdown,
        thinkingContent
    };
};

onMounted(async () => {
    await fetchUserSessionList();
    scrollToBottom();
});
</script>

<style lang="scss" scoped>
.aiChatWrapper {
    .chatLayout {
        display: flex;
    }

    .sessionList {
        width: 200px;
        padding: 20px;
        border-right: 1px solid #EEEEEE;

        button {
            margin-bottom: 10px;
        }

        ul {
            list-style-type: none;
            padding: 0;

            li {
                margin-bottom: 5px;
                cursor: pointer;

                &.active {
                    font-weight: bold;
                }

                button {
                    margin-left: 10px;
                }
            }
        }
    }

    .chatItems {
        flex: 1;
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        padding: 50px 50px 20px 50px;
    }

    .chatMessages {
        width: 100%;
        height: 500px;
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

        .thinking {
            background-color: #f0f7ff;
            border-left: 4px solid #007BFF;
            padding: 10px 15px;
            margin-bottom: 10px;
            border-radius: 4px;
            font-size: 0.9em;
            color: #555;

            .thinkingLabel {
                font-weight: bold;
                margin-bottom: 5px;
                color: #007BFF;
            }

            .thinkingContent {
                white-space: pre-wrap;
            }
        }

        .messageContent {
            display: inline-block;
            background-color: #f1f1f1;
            color: #333;
            padding: 10px 15px;
            border-radius: 18px 18px 18px 0;
            max-width: 100%;
            word-break: break-word;
        }

        // 添加复制按钮样式
        .copyButton {
            margin-top: 5px;
            border: none;
            background-color: #007BFF;
            color: white;
            width: 24px;
            height: 24px;
            border-radius: 50%;
            cursor: pointer;
            transition: background-color 0.3s;
            display: flex;
            justify-content: center;
            align-items: center;

            &:hover {
                background-color: #0056b3;
            }

            svg {
                width: 14px;
                height: 14px;
            }
        }
    }

    .typingIndicator {
        display: flex;
        padding: 10px 0;

        .dot {
            display: inline-block;
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background-color: #999;
            margin-right: 5px;
            animation: typingAnimation 1.4s infinite ease-in-out;

            &:nth-child(1) {
                animation-delay: 0s;
            }

            &:nth-child(2) {
                animation-delay: 0.2s;
            }

            &:nth-child(3) {
                animation-delay: 0.4s;
                margin-right: 0;
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

    .buttonGroup {
        display: flex;

        button {
            min-width: 100px;
            height: 45px;
            border: none;
            border-radius: 8px;
            background-color: #007BFF;
            color: white;
            margin-left: 10px;
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
    }
}

@keyframes typingAnimation {
    0%,
    60%,
    100% {
        transform: translateY(0);
        opacity: 0.6;
    }

    30% {
        transform: translateY(-5px);
        opacity: 1;
    }
}
</style>