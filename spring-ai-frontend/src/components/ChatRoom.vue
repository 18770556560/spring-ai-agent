<template>
  <div class="chat-container">
    <!-- 聊天记录区域 -->
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(msg, index) in messages" :key="index" class="message-wrapper">
        <!-- AI消息 -->
        <div v-if="!msg.isUser"
             class="message ai-message"
             :class="[msg.type]">
          <div class="avatar ai-avatar">
            <AiAvatarFallback :type="aiType" />
          </div>
          <div class="message-bubble">
            <div class="message-content">
              {{ msg.content }}
              <span v-if="connectionStatus === 'connecting' && index === messages.length - 1" class="typing-indicator">▋</span>
            </div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
        </div>

        <!-- 用户消息 -->
        <div v-else class="message user-message" :class="[msg.type]">
          <div class="message-bubble">
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
          <div class="avatar user-avatar">
            <div class="avatar-placeholder">我</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-container">
      <div class="chat-input">
        <textarea
          v-model="inputMessage"
          @keydown.enter.prevent="sendMessage"
          placeholder="请输入消息..."
          class="input-box"
          :disabled="connectionStatus === 'connecting'"
        ></textarea>
        <button
          @click="sendMessage"
          class="send-button"
          :disabled="connectionStatus === 'connecting' || !inputMessage.trim()"
        >
          <span class="send-icon">➤</span>
          <span class="send-text">发送</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, computed } from 'vue'
import AiAvatarFallback from './AiAvatarFallback.vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  connectionStatus: {
    type: String,
    default: 'disconnected'
  },
  aiType: {
    type: String,
    default: 'default'
  }
})

const emit = defineEmits(['send-message'])

const inputMessage = ref('')
const messagesContainer = ref(null)

const sendMessage = () => {
  if (!inputMessage.value.trim()) return

  emit('send-message', inputMessage.value)
  inputMessage.value = ''
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

watch(() => props.messages.length, () => {
  scrollToBottom()
})

watch(() => props.messages.map(m => m.content).join(''), () => {
  scrollToBottom()
})

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
/* ========================================
   ChatRoom - 主题化聊天组件
   所有颜色通过 CSS 变量控制，由父页面注入
   ======================================== */

.chat-container {
  display: flex;
  flex-direction: column;
  height: 70vh;
  min-height: 600px;
  background: var(--chat-bg, #f8fafc);
  border-radius: var(--radius-lg, 16px);
  overflow: hidden;
  position: relative;
  border: 1px solid var(--chat-border, #e2e8f0);
  box-shadow: var(--chat-shadow, 0 4px 24px rgba(0, 0, 0, 0.06));
  transition: all var(--transition-base, 0.3s);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  padding-bottom: 90px;
  display: flex;
  flex-direction: column;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 76px;
  scroll-behavior: smooth;
}

/* 消息滚动条 */
.chat-messages::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: var(--scrollbar-thumb, rgba(148, 163, 184, 0.4));
  border-radius: 2px;
}

.message-wrapper {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.message {
  display: flex;
  align-items: flex-start;
  max-width: 80%;
  animation: messageIn 0.35s cubic-bezier(0.21, 1.02, 0.73, 1);
}

@keyframes messageIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-message {
  margin-left: auto;
  flex-direction: row;
}

.ai-message {
  margin-right: auto;
}

/* 头像 */
.avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full, 50%);
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--avatar-shadow, 0 2px 8px rgba(0, 0, 0, 0.1));
}

.user-avatar {
  margin-left: 10px;
}

.ai-avatar {
  margin-right: 10px;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--user-avatar-bg, #3b82f6);
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 1px;
}

/* 消息气泡 */
.message-bubble {
  padding: 12px 16px;
  border-radius: var(--radius-lg, 16px);
  position: relative;
  word-wrap: break-word;
  min-width: 80px;
  max-width: 100%;
  transition: box-shadow var(--transition-fast, 0.15s);
}

.user-message .message-bubble {
  background: var(--user-bubble-bg, linear-gradient(135deg, #3b82f6, #2563eb));
  color: var(--user-bubble-text, #ffffff);
  border-bottom-right-radius: 6px;
  text-align: left;
  box-shadow: var(--user-bubble-shadow, 0 4px 12px rgba(59, 130, 246, 0.3));
}

.ai-message .message-bubble {
  background: var(--ai-bubble-bg, #f1f5f9);
  color: var(--ai-bubble-text, #1e293b);
  border-bottom-left-radius: 6px;
  text-align: left;
  border: 1px solid var(--ai-bubble-border, #e2e8f0);
}

.message-content {
  font-size: 15px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-time {
  font-size: 11px;
  opacity: 0.6;
  margin-top: 6px;
  text-align: right;
  font-family: var(--font-mono, monospace);
}

/* 输入区域 */
.chat-input-container {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--input-area-bg, #ffffff);
  border-top: 1px solid var(--chat-border, #e2e8f0);
  z-index: 100;
  height: 76px;
  backdrop-filter: blur(12px);
}

.chat-input {
  display: flex;
  padding: 14px 16px;
  height: 100%;
  box-sizing: border-box;
  align-items: center;
  gap: 10px;
}

.input-box {
  flex-grow: 1;
  border: 1.5px solid var(--input-border, #e2e8f0);
  border-radius: var(--radius-full, 24px);
  padding: 10px 18px;
  font-size: 15px;
  font-family: var(--font-sans);
  resize: none;
  min-height: 20px;
  max-height: 44px;
  outline: none;
  transition: all var(--transition-fast, 0.15s);
  background: var(--input-bg, #f8fafc);
  color: var(--input-text, #1e293b);
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.input-box::placeholder {
  color: var(--text-muted, #94a3b8);
}

.input-box::-webkit-scrollbar {
  display: none;
}

.input-box:focus {
  border-color: var(--input-focus-border, #3b82f6);
  box-shadow: var(--input-focus-shadow, 0 0 0 3px rgba(59, 130, 246, 0.1));
  background: var(--input-focus-bg, #ffffff);
}

.send-button {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: 0;
  background: var(--send-btn-bg, linear-gradient(135deg, #3b82f6, #2563eb));
  color: var(--send-btn-text, #ffffff);
  border: none;
  border-radius: var(--radius-full, 24px);
  padding: 0 22px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast, 0.15s);
  height: 42px;
  white-space: nowrap;
  box-shadow: var(--send-btn-shadow, 0 2px 8px rgba(59, 130, 246, 0.3));
}

.send-icon {
  font-size: 14px;
}

.send-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: var(--send-btn-hover-shadow, 0 4px 16px rgba(59, 130, 246, 0.45));
  filter: brightness(1.05);
}

.send-button:active:not(:disabled) {
  transform: translateY(0);
}

/* 打字指示器 */
.typing-indicator {
  display: inline-block;
  animation: blink 0.8s infinite;
  margin-left: 2px;
  color: var(--accent-primary, #3b82f6);
  font-weight: bold;
}

@keyframes blink {
  0%, 100% { opacity: 0; }
  50% { opacity: 1; }
}

/* 禁用状态 */
.input-box:disabled,
.send-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  filter: grayscale(20%);
}

/* 连续消息样式 */
.ai-message + .ai-message {
  margin-top: 0;
}

.ai-message + .ai-message .avatar {
  visibility: hidden;
}

.ai-message + .ai-message .message-bubble {
  border-top-left-radius: 8px;
}

/* 消息类型变体 */
.ai-answer {
  animation: messageIn 0.35s cubic-bezier(0.21, 1.02, 0.73, 1);
}

.ai-final {
  border-left: 2px solid var(--accent-primary, #3b82f6);
}

.ai-error {
  opacity: 0.8;
  border-left: 2px solid #ef4444;
}

/* ========================================
   响应式设计
   ======================================== */
@media (max-width: 768px) {
  .chat-container {
    height: 75vh;
    min-height: 500px;
    border-radius: var(--radius-md, 10px);
  }

  .message {
    max-width: 90%;
  }

  .message-content {
    font-size: 14px;
  }

  .chat-input {
    padding: 10px 12px;
  }

  .input-box {
    padding: 8px 14px;
    font-size: 14px;
  }

  .send-button {
    padding: 0 16px;
    font-size: 14px;
    height: 38px;
  }

  .send-text {
    display: none;
  }

  .send-icon {
    font-size: 16px;
  }

  .avatar {
    width: 36px;
    height: 36px;
  }
}

@media (max-width: 480px) {
  .chat-container {
    height: 80vh;
    min-height: 400px;
    border-radius: 0;
    border-left: none;
    border-right: none;
  }

  .message {
    max-width: 95%;
  }

  .message-bubble {
    padding: 10px 14px;
  }

  .message-content {
    font-size: 13px;
  }

  .chat-input-container {
    height: 66px;
  }

  .chat-messages {
    padding: 14px;
    padding-bottom: 76px;
    bottom: 66px;
  }

  .input-box {
    padding: 8px 12px;
    font-size: 13px;
  }

  .send-button {
    padding: 0 12px;
    height: 36px;
    font-size: 13px;
  }

  .avatar {
    width: 32px;
    height: 32px;
  }
}
</style>
