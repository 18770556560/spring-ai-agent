<template>
  <div class="love-master-container">
    <!-- 背景装饰层 -->
    <div class="bg-pattern">
      <div class="hex-grid"></div>
      <div class="data-lines"></div>
    </div>
    <div class="bg-gradient-orb orb-1"></div>
    <div class="bg-gradient-orb orb-2"></div>

    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-left">
        <button class="back-button" @click="goBack">
          <span class="back-arrow">←</span>
          <span class="back-text">返回</span>
        </button>
      </div>
      <div class="header-center">
        <div class="title-row">
          <span class="title-icon">🛡️</span>
          <h1 class="title">污染防控智能助手</h1>
        </div>
        <div class="header-badge">
          <span class="badge-dot"></span>
          <span class="badge-text">知识库已就绪</span>
        </div>
      </div>
      <div class="header-right">
        <div class="status-indicator">
          <span class="status-label">会话</span>
          <span class="status-id">{{ chatIdShort }}</span>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <div class="content-wrapper">
      <!-- 信息面板 -->
      <div class="info-panel">
        <div class="panel-item">
          <div class="panel-icon">📋</div>
          <div class="panel-text">
            <span class="panel-label">知识库状态</span>
            <span class="panel-value online">在线</span>
          </div>
        </div>
        <div class="panel-divider"></div>
        <div class="panel-item">
          <div class="panel-icon">🔍</div>
          <div class="panel-text">
            <span class="panel-label">检索模式</span>
            <span class="panel-value">RAG 增强检索</span>
          </div>
        </div>
        <div class="panel-divider"></div>
        <div class="panel-item">
          <div class="panel-icon">📊</div>
          <div class="panel-text">
            <span class="panel-label">文档索引</span>
            <span class="panel-value">{{ messageCount }} 条记录</span>
          </div>
        </div>
      </div>

      <!-- 聊天区域 -->
      <div class="chat-area">
        <ChatRoom
          :messages="messages"
          :connection-status="connectionStatus"
          ai-type="love"
          @send-message="sendMessage"
        />
      </div>
    </div>

    <!-- 页脚 -->
    <div class="footer-container">
      <AppFooter />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import ChatRoom from '../components/ChatRoom.vue'
import AppFooter from '../components/AppFooter.vue'
import { chatWithLoveApp } from '../api'

useHead({
  title: '污染防控智能助手 - CCAI超级智能体应用平台',
  meta: [
    {
      name: 'description',
      content: '污染防控智能助手，基于RAG增强检索技术，为企业提供专业的环境污染防控知识库查询与智能咨询服务'
    },
    {
      name: 'keywords',
      content: '污染防控,环保知识库,AI助手,RAG检索,环境咨询,智能问答,CCAI'
    }
  ]
})

const router = useRouter()
const messages = ref([])
const chatId = ref('')
const connectionStatus = ref('disconnected')
let eventSource = null

const chatIdShort = computed(() => {
  return chatId.value ? chatId.value.substring(0, 12) + '...' : '---'
})

const messageCount = computed(() => messages.value.length)

const generateChatId = () => {
  return 'env_' + Math.random().toString(36).substring(2, 14)
}

const addMessage = (content, isUser) => {
  messages.value.push({
    content,
    isUser,
    time: new Date().getTime()
  })
}

const sendMessage = (message) => {
  addMessage(message, true)

  if (eventSource) {
    eventSource.close()
  }

  const aiMessageIndex = messages.value.length
  addMessage('', false)

  connectionStatus.value = 'connecting'
  eventSource = chatWithLoveApp(message, chatId.value)

  eventSource.onmessage = (event) => {
    const data = event.data
    if (data && data !== '[DONE]') {
      if (aiMessageIndex < messages.value.length) {
        messages.value[aiMessageIndex].content += data
      }
    }

    if (data === '[DONE]') {
      connectionStatus.value = 'disconnected'
      eventSource.close()
    }
  }

  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    connectionStatus.value = 'error'
    eventSource.close()
  }
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  chatId.value = generateChatId()
  addMessage('您好！我是污染防控智能助手，已接入企业环保知识库。我可以帮您查询污染防控相关政策法规、技术标准和最佳实践，请问有什么可以帮您的？', false)
})

onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
/* ========================================
   污染防控智能助手 - 企业环保科技主题
   设计理念：智慧环境监测指挥中心
   色调：翠绿 + 青蓝 + 白
   ======================================== */

/* -------- CSS 主题变量 (供子组件继承) -------- */
.love-master-container {
  /* 背景色系 */
  --bg-root: #f0fdf4;
  --bg-surface: #ffffff;
  --bg-elevated: #f8fafc;

  /* 主色调 - 环保绿 */
  --accent-primary: #059669;
  --accent-primary-light: #10b981;
  --accent-primary-dark: #047857;
  --accent-secondary: #0284c7;
  --accent-teal: #0d9488;

  /* 文字色系 */
  --text-primary: #0f172a;
  --text-secondary: #334155;
  --text-muted: #94a3b8;

  /* 边框 */
  --border-color: #e2e8f0;
  --border-light: #f1f5f9;

  /* -------- ChatRoom 子组件变量 -------- */
  --chat-bg: #ffffff;
  --chat-border: #e2e8f0;
  --chat-shadow: 0 4px 24px rgba(0, 0, 0, 0.05), 0 0 0 1px rgba(5, 150, 105, 0.05);

  --user-bubble-bg: linear-gradient(135deg, #059669, #047857);
  --user-bubble-text: #ffffff;
  --user-bubble-shadow: 0 4px 14px rgba(5, 150, 105, 0.3);

  --ai-bubble-bg: #f0fdf4;
  --ai-bubble-text: #1e293b;
  --ai-bubble-border: #d1fae5;

  --user-avatar-bg: #0284c7;

  --input-area-bg: rgba(255, 255, 255, 0.95);
  --input-bg: #f8fafc;
  --input-text: #0f172a;
  --input-border: #cbd5e1;
  --input-focus-border: #059669;
  --input-focus-bg: #ffffff;
  --input-focus-shadow: 0 0 0 3px rgba(5, 150, 105, 0.1);

  --send-btn-bg: linear-gradient(135deg, #059669, #047857);
  --send-btn-text: #ffffff;
  --send-btn-shadow: 0 2px 8px rgba(5, 150, 105, 0.3);
  --send-btn-hover-shadow: 0 6px 20px rgba(5, 150, 105, 0.4);

  --scrollbar-thumb: rgba(5, 150, 105, 0.2);
  --avatar-shadow: 0 2px 8px rgba(5, 150, 105, 0.15);

  /* AI 头像 */
  --ai-avatar-bg-love: linear-gradient(135deg, #059669, #0d9488);
  --ai-avatar-shadow-love: 0 2px 10px rgba(5, 150, 105, 0.35);

  /* -------- AppFooter 子组件变量 -------- */
  --footer-bg: #064e3b;
  --footer-text: #a7f3d0;
  --footer-heading: #ecfdf5;
  --footer-border: rgba(255, 255, 255, 0.1);
  --footer-link: #6ee7b7;
  --footer-link-hover: #34d399;
  --footer-logo-bg: linear-gradient(135deg, #10b981, #059669);
  --footer-logo-shadow: 0 4px 14px rgba(5, 150, 105, 0.4);
  --footer-pattern:
    linear-gradient(60deg, rgba(255,255,255,0.03) 25%, transparent 25%),
    linear-gradient(-60deg, rgba(255,255,255,0.03) 25%, transparent 25%);
}

/* -------- 页面容器 -------- */
.love-master-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: linear-gradient(170deg, #f0fdf4 0%, #ecfeff 40%, #f0fdf4 100%);
  position: relative;
  overflow: hidden;
}

/* -------- 背景装饰 -------- */
.bg-pattern {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

/* 六边形网格 */
.hex-grid {
  position: absolute;
  inset: 0;
  background-image:
    url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='60' height='52' viewBox='0 0 60 52'%3E%3Cpolygon points='30,0 60,15 60,37 30,52 0,37 0,15' fill='none' stroke='%23059669' stroke-width='0.4' opacity='0.06'/%3E%3C/svg%3E");
  background-size: 80px 69px;
  animation: hexDrift 60s linear infinite;
}

@keyframes hexDrift {
  0% { transform: translate(0, 0); }
  100% { transform: translate(80px, 69px); }
}

/* 数据流线 */
.data-lines {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, transparent 49.5%, rgba(5, 150, 105, 0.03) 50%, transparent 50.5%),
    linear-gradient(0deg, transparent 49.5%, rgba(5, 150, 105, 0.02) 50%, transparent 50.5%);
  background-size: 120px 120px;
}

/* 光晕 */
.bg-gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  pointer-events: none;
  z-index: 0;
}

.orb-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(5, 150, 105, 0.12), transparent 70%);
  top: -150px;
  right: -150px;
  animation: orbFloat1 20s ease-in-out infinite;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(2, 132, 199, 0.1), transparent 70%);
  bottom: 10%;
  left: -100px;
  animation: orbFloat2 25s ease-in-out infinite;
}

@keyframes orbFloat1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(-40px, 30px) scale(1.1); }
  66% { transform: translate(20px, -20px) scale(0.95); }
}

@keyframes orbFloat2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(50px, -30px) scale(1.15); }
}

/* -------- 顶部导航栏 -------- */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 28px;
  background: linear-gradient(135deg, #064e3b 0%, #065f46 50%, #0f766e 100%);
  color: #fff;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
  position: sticky;
  top: 0;
  z-index: 20;
  backdrop-filter: blur(12px);
}

.header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, #34d399, #2dd4bf, #34d399, transparent);
  opacity: 0.8;
}

.header-left, .header-right {
  flex: 0 0 auto;
  min-width: 140px;
}

.header-right {
  display: flex;
  justify-content: flex-end;
}

.header-center {
  flex: 1;
  text-align: center;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  padding: 8px 18px;
  border-radius: var(--radius-full, 24px);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast, 0.15s);
  font-family: var(--font-sans);
  backdrop-filter: blur(4px);
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
  transform: translateX(-2px);
}

.back-arrow {
  font-size: 16px;
}

.title-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 4px;
}

.title-icon {
  font-size: 24px;
  filter: drop-shadow(0 0 4px rgba(52, 211, 153, 0.5));
}

.title {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
  letter-spacing: 1px;
  color: #ecfdf5;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.header-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(52, 211, 153, 0.15);
  border: 1px solid rgba(52, 211, 153, 0.3);
  padding: 4px 14px;
  border-radius: var(--radius-full, 20px);
  font-size: 12px;
}

.badge-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 8px #34d399;
  animation: statusPulse 2s ease-in-out infinite;
}

@keyframes statusPulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 8px #34d399; }
  50% { opacity: 0.5; box-shadow: 0 0 2px #34d399; }
}

.badge-text {
  color: #a7f3d0;
  font-weight: 500;
}

/* 状态指示器 */
.status-indicator {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  background: rgba(255, 255, 255, 0.08);
  padding: 6px 14px;
  border-radius: var(--radius-md, 10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.status-label {
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: rgba(255, 255, 255, 0.6);
}

.status-id {
  font-family: var(--font-mono);
  font-size: 12px;
  color: #a7f3d0;
}

/* -------- 内容区 -------- */
.content-wrapper {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 24px 24px 0;
  position: relative;
  z-index: 1;
  max-width: 1100px;
  width: 100%;
  margin: 0 auto;
}

/* 信息面板 */
.info-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  background: #ffffff;
  border-radius: var(--radius-lg, 16px);
  padding: 0 28px;
  margin-bottom: 20px;
  box-shadow:
    0 2px 12px rgba(0, 0, 0, 0.04),
    0 0 0 1px rgba(5, 150, 105, 0.08);
  backdrop-filter: blur(8px);
  flex-wrap: wrap;
}

.panel-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 24px;
}

.panel-icon {
  font-size: 28px;
  width: 46px;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0fdf4;
  border-radius: var(--radius-md, 10px);
  border: 1px solid #d1fae5;
}

.panel-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.panel-label {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  color: #94a3b8;
  font-weight: 600;
}

.panel-value {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.panel-value.online {
  color: #059669;
  display: flex;
  align-items: center;
  gap: 6px;
}

.panel-value.online::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 6px rgba(34, 197, 94, 0.5);
}

.panel-divider {
  width: 1px;
  height: 40px;
  background: linear-gradient(0deg, transparent, #e2e8f0, transparent);
}

/* 聊天区域 */
.chat-area {
  flex: 1;
  overflow: hidden;
  position: relative;
  min-height: calc(100vh - 240px);
  margin-bottom: 16px;
}

/* 页脚容器 */
.footer-container {
  margin-top: auto;
  position: relative;
  z-index: 1;
}

/* ========================================
   响应式设计
   ======================================== */
@media (max-width: 768px) {
  .header {
    padding: 12px 16px;
  }

  .header-left, .header-right {
    min-width: auto;
  }

  .title {
    font-size: 18px;
  }

  .title-icon {
    font-size: 20px;
  }

  .header-badge {
    font-size: 11px;
    padding: 3px 10px;
  }

  .back-text {
    display: none;
  }

  .back-button {
    padding: 8px 12px;
  }

  .status-indicator {
    padding: 4px 10px;
  }

  .status-label {
    font-size: 9px;
  }

  .status-id {
    font-size: 10px;
  }

  .content-wrapper {
    padding: 16px 12px 0;
  }

  .info-panel {
    padding: 0 16px;
    margin-bottom: 14px;
    gap: 0;
    justify-content: space-around;
  }

  .panel-item {
    padding: 14px 12px;
  }

  .panel-icon {
    font-size: 22px;
    width: 38px;
    height: 38px;
  }

  .panel-label {
    font-size: 10px;
  }

  .panel-value {
    font-size: 13px;
  }

  .panel-divider {
    height: 30px;
  }

  .chat-area {
    min-height: calc(100vh - 200px);
    margin-bottom: 12px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 10px 12px;
    flex-wrap: wrap;
    gap: 8px;
  }

  .header-center {
    order: -1;
    flex: 0 0 100%;
  }

  .title {
    font-size: 16px;
  }

  .title-icon {
    font-size: 18px;
  }

  .content-wrapper {
    padding: 12px 8px 0;
  }

  .info-panel {
    padding: 8px 12px;
    margin-bottom: 10px;
    border-radius: var(--radius-md, 10px);
  }

  .panel-item {
    padding: 10px 8px;
    gap: 8px;
  }

  .panel-icon {
    font-size: 18px;
    width: 32px;
    height: 32px;
    border-radius: 8px;
  }

  .panel-label {
    font-size: 9px;
    letter-spacing: 1px;
  }

  .panel-value {
    font-size: 12px;
  }

  .panel-divider {
    display: none;
  }

  .chat-area {
    min-height: calc(100vh - 180px);
    margin-bottom: 8px;
  }
}
</style>
