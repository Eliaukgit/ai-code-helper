<template>
  <div class="chat-container">
    <!-- 顶部标题栏 -->
    <header class="chat-header">
      <div class="header-content">
        <span class="logo">🤖</span>
        <h1>AI 编程小助手</h1>
        <span class="subtitle">编程学习 · 求职面试 · 技术答疑</span>
      </div>
    </header>

    <!-- 聊天消息区域 -->
    <main class="chat-messages" ref="messagesContainer">
      <!-- 欢迎消息 -->
      <div v-if="messages.length === 0" class="welcome">
        <div class="welcome-icon">🤖</div>
        <h2>你好，我是 AI 编程小助手！</h2>
        <p>我可以帮你解答编程学习和求职面试相关的问题，快来试试吧~</p>
        <div class="suggestions">
          <div class="suggestion-item" @click="sendSuggestion('Java 学习路线是什么？')">Java 学习路线是什么？</div>
          <div class="suggestion-item" @click="sendSuggestion('Spring Boot 常见面试题有哪些？')">Spring Boot 常见面试题有哪些？</div>
          <div class="suggestion-item" @click="sendSuggestion('如何准备技术面试？')">如何准备技术面试？</div>
        </div>
      </div>

      <!-- 消息列表 -->
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message-wrapper', msg.role === 'user' ? 'message-right' : 'message-left']"
      >
        <div class="avatar">
          {{ msg.role === 'user' ? '🧑‍💻' : '🤖' }}
        </div>
        <div :class="['message-bubble', msg.role === 'user' ? 'user-bubble' : 'ai-bubble']">
          <div class="message-text" v-html="formatMessage(msg.content)"></div>
        </div>
      </div>

      <!-- AI 正在输入的指示器 -->
      <div v-if="isLoading" class="message-wrapper message-left">
        <div class="avatar">🤖</div>
        <div class="message-bubble ai-bubble">
          <div class="typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </main>

    <!-- 底部输入区域 -->
    <footer class="chat-input-area">
      <div class="input-wrapper">
        <textarea
          ref="inputRef"
          v-model="inputMessage"
          @keydown.enter.exact="handleSend"
          placeholder="输入你的问题，按 Enter 发送..."
          rows="1"
          :disabled="isSending"
        ></textarea>
        <button class="send-btn" @click="handleSend" :disabled="!inputMessage.trim() || isSending">
          <span v-if="isSending">⏳</span>
          <span v-else>📤</span>
        </button>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'

// 生成会话ID
const memoryId = ref(Math.floor(Math.random() * 1000000))
const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const isSending = ref(false)
const messagesContainer = ref(null)
const inputRef = ref(null)

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 简单的消息格式化（换行 + 代码块）
const formatMessage = (text) => {
  if (!text) return ''
  // 处理代码块 ```...```
  let formatted = text.replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
  // 处理行内代码
  formatted = formatted.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
  // 处理换行
  formatted = formatted.replace(/\n/g, '<br>')
  return formatted
}

// 发送建议问题
const sendSuggestion = (text) => {
  inputMessage.value = text
  handleSend()
}

// 发送消息
const handleSend = async (event) => {
  if (event) event.preventDefault()
  const msg = inputMessage.value.trim()
  if (!msg || isSending.value) return

  // 添加用户消息
  messages.value.push({ role: 'user', content: msg })
  inputMessage.value = ''
  isSending.value = true
  isLoading.value = true
  await scrollToBottom()

  // 添加 AI 消息占位
  const aiMessageIndex = messages.value.length
  messages.value.push({ role: 'ai', content: '' })

  try {
    // 使用 SSE 调用后端接口
    const url = `/api/ai/chat?memoryId=${memoryId.value}&message=${encodeURIComponent(msg)}`
    const eventSource = new EventSource(url)

    isLoading.value = false

    eventSource.onmessage = (event) => {
      const data = event.data
      messages.value[aiMessageIndex].content += data
      scrollToBottom()
    }

    eventSource.onerror = (e) => {
      eventSource.close()
      isLoading.value = false
      isSending.value = false
      // SSE 正常结束也会触发 onerror，只有内容为空时才提示错误
      if (!messages.value[aiMessageIndex].content) {
        messages.value[aiMessageIndex].content = '抱歉，出了点问题，请稍后重试。'
      }
      scrollToBottom()
    }
  } catch (error) {
    isLoading.value = false
    isSending.value = false
    messages.value[aiMessageIndex].content = '抱歉，网络出错了，请稍后重试。'
    scrollToBottom()
  }
}

onMounted(() => {
  inputRef.value?.focus()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 900px;
  margin: 0 auto;
  background: #fff;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.08);
}

/* 顶部标题栏 */
.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 16px 24px;
  flex-shrink: 0;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-content .logo {
  font-size: 28px;
}

.header-content h1 {
  font-size: 20px;
  font-weight: 600;
}

.header-content .subtitle {
  font-size: 12px;
  opacity: 0.8;
  margin-left: auto;
}

/* 聊天消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f9fafb;
}

/* 欢迎区域 */
.welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  color: #666;
}

.welcome-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.welcome h2 {
  font-size: 22px;
  color: #333;
  margin-bottom: 8px;
}

.welcome p {
  font-size: 14px;
  color: #888;
  margin-bottom: 24px;
}

.suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.suggestion-item {
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  color: #555;
  transition: all 0.2s;
}

.suggestion-item:hover {
  background: #667eea;
  color: #fff;
  border-color: #667eea;
}

/* 消息行 */
.message-wrapper {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 10px;
}

.message-right {
  flex-direction: row-reverse;
}

.avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

/* 消息气泡 */
.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.user-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-top-right-radius: 4px;
}

.ai-bubble {
  background: #fff;
  color: #333;
  border: 1px solid #e8e8e8;
  border-top-left-radius: 4px;
}

.message-text :deep(pre) {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
  font-size: 13px;
}

.message-text :deep(code) {
  font-family: 'Fira Code', 'Consolas', monospace;
}

.message-text :deep(.inline-code) {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  color: #e83e8c;
}

/* 输入指示器 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #999;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 底部输入区域 */
.chat-input-area {
  padding: 16px 20px;
  background: #fff;
  border-top: 1px solid #eee;
  flex-shrink: 0;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  background: #f5f5f5;
  border-radius: 24px;
  padding: 8px 8px 8px 20px;
  border: 1px solid #e0e0e0;
  transition: border-color 0.2s;
}

.input-wrapper:focus-within {
  border-color: #667eea;
}

.input-wrapper textarea {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  max-height: 120px;
  font-family: inherit;
}

.send-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  transition: opacity 0.2s;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn:not(:disabled):hover {
  opacity: 0.9;
}

/* 滚动条 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #aaa;
}
</style>
