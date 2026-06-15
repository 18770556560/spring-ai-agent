<template>
  <div class="super-agent-container">
    <!-- 背景效果层 -->
    <div class="bg-layer">
      <div class="dot-grid"></div>
      <div class="scan-line"></div>
      <div class="particle-field">
        <div class="particle p1"></div>
        <div class="particle p2"></div>
        <div class="particle p3"></div>
        <div class="particle p4"></div>
        <div class="particle p5"></div>
        <div class="particle p6"></div>
      </div>
    </div>
    <div class="bg-glow glow-1"></div>
    <div class="bg-glow glow-2"></div>

    <!-- 顶部命令栏 -->
    <header class="header">
      <div class="header-border-glow"></div>
      <div class="header-left">
        <button class="back-button" @click="goBack">
          <span class="back-arrow">⟵</span>
          <span class="back-text">返回</span>
        </button>
      </div>
      <div class="header-center">
        <div class="title-block">
          <div class="title-caret">▸</div>
          <h1 class="title">超级AI智能体</h1>
          <div class="title-flash"></div>
        </div>
        <div class="header-subtitle">
          <span class="subtitle-dot"></span>
          NEURAL COMMAND INTERFACE
          <span class="subtitle-dot"></span>
        </div>
      </div>
      <div class="header-right">
        <div class="sys-status">
          <div class="sys-row">
            <span class="sys-label">SYS</span>
            <span class="sys-value nom">NOMINAL</span>
          </div>
          <div class="sys-row">
            <span class="sys-label">NODES</span>
            <span class="sys-value">8 ACTIVE</span>
          </div>
        </div>
      </div>
    </header>

    <!-- 主内容 -->
    <div class="content-wrapper">
      <!-- 状态仪表盘 -->
      <div class="dashboard">
        <div class="dash-card">
          <div class="dash-card-inner">
            <div class="dash-icon">🧠</div>
            <div class="dash-info">
              <span class="dash-label">推理引擎</span>
              <span class="dash-value status-active">ACTIVE</span>
            </div>
            <div class="dash-bar">
              <div class="dash-bar-fill" style="width: 98%"></div>
            </div>
          </div>
        </div>
        <div class="dash-card">
          <div class="dash-card-inner">
            <div class="dash-icon">⚡</div>
            <div class="dash-info">
              <span class="dash-label">响应延迟</span>
              <span class="dash-value">~1.2s</span>
            </div>
            <div class="dash-bar">
              <div class="dash-bar-fill fast" style="width: 15%"></div>
            </div>
          </div>
        </div>
        <div class="dash-card">
          <div class="dash-card-inner">
            <div class="dash-icon">🔗</div>
            <div class="dash-info">
              <span class="dash-label">上下文窗口</span>
              <span class="dash-value">200K</span>
            </div>
            <div class="dash-bar">
              <div class="dash-bar-fill" style="width: 65%"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 聊天区 -->
      <div class="chat-area">
        <ChatRoom
          :messages="messages"
          :connection-status="connectionStatus"
          ai-type="super"
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
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import ChatRoom from '../components/ChatRoom.vue'
import AppFooter from '../components/AppFooter.vue'
import { chatWithManus } from '../api'

useHead({
  title: '超级AI智能体 - CCAI超级智能体应用平台',
  meta: [
    {
      name: 'description',
      content: '超级AI智能体是CCAI平台的全能型AI助手，搭载先进推理引擎，可解答各类专业问题，提供精准的智能建议与解决方案'
    },
    {
      name: 'keywords',
      content: '超级AI,智能体,AI助手,智能问答,专业建议,深度学习,CCAI,神经网络'
    }
  ]
})

const router = useRouter()
const messages = ref([])
const connectionStatus = ref('disconnected')
let eventSource = null

const addMessage = (content, isUser, type = '') => {
  messages.value.push({
    content,
    isUser,
    type,
    time: new Date().getTime()
  })
}

const sendMessage = (message) => {
  addMessage(message, true, 'user-question')

  if (eventSource) {
    eventSource.close()
  }

  connectionStatus.value = 'connecting'

  let messageBuffer = []
  let lastBubbleTime = Date.now()
  let isFirstResponse = true

  const chineseEndPunctuation = ['。', '！', '？', '…']
  const minBubbleInterval = 800

  const createBubble = (content, type = 'ai-answer') => {
    if (!content.trim()) return

    const now = Date.now()
    const timeSinceLastBubble = now - lastBubbleTime

    if (isFirstResponse) {
      addMessage(content, false, type)
      isFirstResponse = false
    } else if (timeSinceLastBubble < minBubbleInterval) {
      setTimeout(() => {
        addMessage(content, false, type)
      }, minBubbleInterval - timeSinceLastBubble)
    } else {
      addMessage(content, false, type)
    }

    lastBubbleTime = now
    messageBuffer = []
  }

  eventSource = chatWithManus(message)

  eventSource.onmessage = (event) => {
    const data = event.data

    if (data && data !== '[DONE]') {
      messageBuffer.push(data)

      const combinedText = messageBuffer.join('')
      const lastChar = data.charAt(data.length - 1)
      const hasCompleteSentence = chineseEndPunctuation.includes(lastChar) || data.includes('\n\n')
      const isLongEnough = combinedText.length > 40

      if (hasCompleteSentence || isLongEnough) {
        createBubble(combinedText)
      }
    }

    if (data === '[DONE]') {
      if (messageBuffer.length > 0) {
        const remainingContent = messageBuffer.join('')
        createBubble(remainingContent, 'ai-final')
      }

      connectionStatus.value = 'disconnected'
      eventSource.close()
    }
  }

  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    connectionStatus.value = 'error'
    eventSource.close()

    if (messageBuffer.length > 0) {
      const remainingContent = messageBuffer.join('')
      createBubble(remainingContent, 'ai-error')
    }
  }
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  addMessage('> SYSTEM BOOT SEQUENCE COMPLETE\n> NEURAL CORE ONLINE\n> READY FOR INPUT\n\n你好，我是超级AI智能体。搭载先进推理引擎，可解答各类专业问题，提供精准建议与解决方案。请告诉我你需要什么帮助？', false)
})

onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
/* ========================================
   超级AI智能体 - 未来AI指挥中心主题
   设计理念：深度神经网络操作界面
   色调：深空蓝黑 + 电光蓝 + 霓虹紫 + 青色
   ======================================== */

/* -------- CSS 主题变量 (供子组件继承) -------- */
.super-agent-container {
  /* 背景色系 */
  --bg-root: #020617;
  --bg-surface: #0f172a;
  --bg-elevated: #1e293b;

  /* 主色调 - 电光蓝 + 紫 */
  --accent-primary: #3b82f6;
  --accent-primary-light: #60a5fa;
  --accent-primary-dark: #2563eb;
  --accent-secondary: #8b5cf6;
  --accent-cyan: #06b6d4;
  --accent-neon: #00d4ff;

  /* 文字色系 */
  --text-primary: #f1f5f9;
  --text-secondary: #cbd5e1;
  --text-muted: #64748b;

  /* 边框 */
  --border-color: #1e293b;
  --border-light: #334155;

  /* -------- ChatRoom 子组件变量 -------- */
  --chat-bg: rgba(15, 23, 42, 0.85);
  --chat-border: rgba(59, 130, 246, 0.2);
  --chat-shadow:
    0 8px 40px rgba(0, 0, 0, 0.5),
    0 0 0 1px rgba(59, 130, 246, 0.15),
    inset 0 0 80px rgba(59, 130, 246, 0.03);

  --user-bubble-bg: linear-gradient(135deg, #3b82f6, #6366f1);
  --user-bubble-text: #ffffff;
  --user-bubble-shadow: 0 4px 20px rgba(59, 130, 246, 0.4), 0 0 30px rgba(99, 102, 241, 0.15);

  --ai-bubble-bg: rgba(30, 41, 59, 0.8);
  --ai-bubble-text: #e2e8f0;
  --ai-bubble-border: rgba(59, 130, 246, 0.15);

  --user-avatar-bg: #6366f1;

  --input-area-bg: rgba(15, 23, 42, 0.95);
  --input-bg: rgba(30, 41, 59, 0.7);
  --input-text: #e2e8f0;
  --input-border: rgba(59, 130, 246, 0.2);
  --input-focus-border: #3b82f6;
  --input-focus-bg: rgba(30, 41, 59, 0.9);
  --input-focus-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15), 0 0 20px rgba(59, 130, 246, 0.1);

  --send-btn-bg: linear-gradient(135deg, #3b82f6, #8b5cf6);
  --send-btn-text: #ffffff;
  --send-btn-shadow: 0 2px 12px rgba(59, 130, 246, 0.4), 0 0 20px rgba(139, 92, 246, 0.2);
  --send-btn-hover-shadow: 0 6px 28px rgba(59, 130, 246, 0.6), 0 0 40px rgba(139, 92, 246, 0.3);

  --scrollbar-thumb: rgba(59, 130, 246, 0.25);
  --avatar-shadow: 0 2px 12px rgba(59, 130, 246, 0.3);

  /* AI 头像 */
  --ai-avatar-bg-super: linear-gradient(135deg, #4f46e5, #7c3aed);
  --ai-avatar-shadow-super: 0 0 16px rgba(99, 102, 241, 0.5);

  /* -------- AppFooter 子组件变量 -------- */
  --footer-bg: #020617;
  --footer-text: #64748b;
  --footer-heading: #94a3b8;
  --footer-border: rgba(59, 130, 246, 0.12);
  --footer-link: #64748b;
  --footer-link-hover: #60a5fa;
  --footer-logo-bg: linear-gradient(135deg, #3b82f6, #8b5cf6);
  --footer-logo-shadow: 0 4px 16px rgba(59, 130, 246, 0.4);
  --footer-pattern:
    linear-gradient(0deg, rgba(59,130,246,0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59,130,246,0.02) 1px, transparent 1px);
}

/* -------- 页面容器 -------- */
.super-agent-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #020617;
  position: relative;
  overflow: hidden;
}

/* -------- 背景效果层 -------- */
.bg-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

/* 点阵网格 */
.dot-grid {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(59, 130, 246, 0.12) 1px, transparent 1px);
  background-size: 32px 32px;
  mask-image: radial-gradient(ellipse 70% 60% at 50% 40%, black 30%, transparent 70%);
  -webkit-mask-image: radial-gradient(ellipse 70% 60% at 50% 40%, black 30%, transparent 70%);
}

/* 扫描线 */
.scan-line {
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 2px,
    rgba(0, 0, 0, 0.03) 2px,
    rgba(0, 0, 0, 0.03) 4px
  );
  animation: scanMove 8s linear infinite;
}

@keyframes scanMove {
  0% { transform: translateY(0); }
  100% { transform: translateY(4px); }
}

/* 粒子 */
.particle-field {
  position: absolute;
  inset: 0;
}

.particle {
  position: absolute;
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #3b82f6;
  box-shadow:
    0 0 6px #3b82f6,
    0 0 12px #3b82f6,
    0 0 20px rgba(59, 130, 246, 0.5);
  animation: particleFloat linear infinite;
}

.p1 { top: 15%; left: 10%; animation-duration: 12s; animation-delay: 0s; }
.p2 { top: 25%; left: 85%; animation-duration: 15s; animation-delay: -3s; width: 2px; height: 2px; }
.p3 { top: 60%; left: 20%; animation-duration: 18s; animation-delay: -7s; background: #8b5cf6; box-shadow: 0 0 6px #8b5cf6, 0 0 12px #8b5cf6; }
.p4 { top: 70%; left: 75%; animation-duration: 14s; animation-delay: -5s; width: 4px; height: 4px; background: #06b6d4; box-shadow: 0 0 6px #06b6d4, 0 0 12px #06b6d4; }
.p5 { top: 40%; left: 50%; animation-duration: 20s; animation-delay: -10s; width: 2px; height: 2px; }
.p6 { top: 80%; left: 45%; animation-duration: 16s; animation-delay: -2s; background: #a855f7; box-shadow: 0 0 6px #a855f7, 0 0 12px #a855f7; }

@keyframes particleFloat {
  0% {
    transform: translate(0, 0) scale(1);
    opacity: 0;
  }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% {
    transform: translate(30px, -80px) scale(0.5);
    opacity: 0;
  }
}

/* 背景光晕 */
.bg-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(150px);
  pointer-events: none;
  z-index: 0;
}

.glow-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.1), transparent 70%);
  top: -200px;
  right: -200px;
  animation: glowPulse1 8s ease-in-out infinite;
}

.glow-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.08), transparent 70%);
  bottom: 10%;
  left: -150px;
  animation: glowPulse2 10s ease-in-out infinite;
}

@keyframes glowPulse1 {
  0%, 100% { opacity: 0.6; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
}

@keyframes glowPulse2 {
  0%, 100% { opacity: 0.4; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.3); }
}

/* -------- 顶部命令栏 -------- */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 28px;
  background: rgba(2, 6, 23, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  color: #fff;
  position: sticky;
  top: 0;
  z-index: 20;
  border-bottom: 1px solid rgba(59, 130, 246, 0.15);
}

.header-border-glow {
  position: absolute;
  bottom: -1px;
  left: 5%;
  right: 5%;
  height: 1px;
  background: linear-gradient(90deg,
    transparent,
    rgba(59, 130, 246, 0.4),
    rgba(139, 92, 246, 0.6),
    rgba(6, 182, 212, 0.4),
    transparent
  );
  filter: blur(1px);
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
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.25);
  color: #93c5fd;
  padding: 8px 18px;
  border-radius: var(--radius-full, 24px);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast, 0.15s);
  font-family: var(--font-mono);
  letter-spacing: 1px;
  text-transform: uppercase;
}

.back-button:hover {
  background: rgba(59, 130, 246, 0.2);
  border-color: rgba(59, 130, 246, 0.5);
  color: #bfdbfe;
  box-shadow: 0 0 16px rgba(59, 130, 246, 0.2);
  transform: translateX(-2px);
}

.back-arrow {
  font-size: 15px;
}

/* 标题 */
.title-block {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 6px;
  position: relative;
}

.title-caret {
  color: #3b82f6;
  font-size: 18px;
  animation: caretBlink 1.5s step-end infinite;
  text-shadow: 0 0 10px rgba(59, 130, 246, 0.6);
}

@keyframes caretBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.title {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
  letter-spacing: 3px;
  background: linear-gradient(135deg, #bfdbfe, #93c5fd, #a5b4fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: none;
  filter: drop-shadow(0 0 8px rgba(59, 130, 246, 0.3));
}

.title-flash {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.1), transparent);
  animation: titleFlash 4s ease-in-out infinite;
}

@keyframes titleFlash {
  0%, 100% { transform: translateX(-100%); }
  50% { transform: translateX(100%); }
}

.header-subtitle {
  font-family: var(--font-mono);
  font-size: 10px;
  letter-spacing: 4px;
  color: #475569;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.subtitle-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #3b82f6;
  box-shadow: 0 0 6px #3b82f6;
  animation: statusPulse 2s ease-in-out infinite;
}

@keyframes statusPulse {
  0%, 100% { opacity: 0.4; box-shadow: 0 0 4px #3b82f6; }
  50% { opacity: 1; box-shadow: 0 0 10px #3b82f6; }
}

/* 系统状态 */
.sys-status {
  display: flex;
  flex-direction: column;
  gap: 3px;
  background: rgba(59, 130, 246, 0.06);
  border: 1px solid rgba(59, 130, 246, 0.12);
  padding: 8px 16px;
  border-radius: var(--radius-md, 10px);
}

.sys-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sys-label {
  font-family: var(--font-mono);
  font-size: 10px;
  letter-spacing: 2px;
  color: #475569;
  min-width: 40px;
}

.sys-value {
  font-family: var(--font-mono);
  font-size: 11px;
  color: #93c5fd;
  letter-spacing: 1px;
}

.sys-value.nom {
  color: #4ade80;
  text-shadow: 0 0 8px rgba(74, 222, 128, 0.4);
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

/* 状态仪表盘 */
.dashboard {
  display: flex;
  gap: 14px;
  margin-bottom: 20px;
}

.dash-card {
  flex: 1;
  background: rgba(15, 23, 42, 0.7);
  border: 1px solid rgba(59, 130, 246, 0.12);
  border-radius: var(--radius-lg, 16px);
  padding: 16px 20px;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  transition: all var(--transition-base, 0.3s);
  position: relative;
  overflow: hidden;
}

.dash-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(59,130,246,0.05), transparent 60%);
  pointer-events: none;
}

.dash-card:hover {
  border-color: rgba(59, 130, 246, 0.35);
  box-shadow: 0 4px 24px rgba(59, 130, 246, 0.1);
  transform: translateY(-1px);
}

.dash-card-inner {
  display: flex;
  flex-direction: column;
  gap: 10px;
  position: relative;
  z-index: 1;
}

.dash-icon {
  font-size: 24px;
}

.dash-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dash-label {
  font-family: var(--font-mono);
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: #475569;
}

.dash-value {
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
  letter-spacing: 0.5px;
}

.dash-value.status-active {
  color: #4ade80;
  text-shadow: 0 0 12px rgba(74, 222, 128, 0.4);
}

.dash-bar {
  height: 3px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 2px;
  overflow: hidden;
}

.dash-bar-fill {
  height: 100%;
  border-radius: 2px;
  background: linear-gradient(90deg, #3b82f6, #6366f1);
  box-shadow: 0 0 10px rgba(59, 130, 246, 0.4);
  transition: width 1.5s ease;
  animation: barGlow 3s ease-in-out infinite;
}

.dash-bar-fill.fast {
  background: linear-gradient(90deg, #22c55e, #4ade80);
  box-shadow: 0 0 10px rgba(34, 197, 94, 0.3);
}

@keyframes barGlow {
  0%, 100% { filter: brightness(1); }
  50% { filter: brightness(1.3); }
}

/* 聊天区 */
.chat-area {
  flex: 1;
  overflow: hidden;
  position: relative;
  min-height: calc(100vh - 280px);
  margin-bottom: 16px;
}

/* 页脚 */
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
    letter-spacing: 2px;
  }

  .title-caret {
    font-size: 15px;
  }

  .header-subtitle {
    font-size: 9px;
    letter-spacing: 3px;
  }

  .back-text {
    display: none;
  }

  .back-button {
    padding: 8px 12px;
    font-size: 12px;
  }

  .sys-status {
    padding: 6px 12px;
  }

  .sys-label, .sys-value {
    font-size: 9px;
  }

  .content-wrapper {
    padding: 16px 12px 0;
  }

  .dashboard {
    gap: 10px;
    margin-bottom: 14px;
  }

  .dash-card {
    padding: 12px 14px;
    border-radius: var(--radius-md, 10px);
  }

  .dash-icon {
    font-size: 20px;
  }

  .dash-label {
    font-size: 9px;
    letter-spacing: 1px;
  }

  .dash-value {
    font-size: 10px;
  }

  .chat-area {
    min-height: calc(100vh - 240px);
    margin-bottom: 12px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 10px 12px;
    flex-wrap: wrap;
    gap: 6px;
  }

  .header-center {
    order: -1;
    flex: 0 0 100%;
  }

  .title {
    font-size: 16px;
    letter-spacing: 1px;
  }

  .title-caret {
    font-size: 13px;
  }

  .header-right {
    display: none;
  }

  .content-wrapper {
    padding: 10px 8px 0;
  }

  .dashboard {
    gap: 6px;
    margin-bottom: 10px;
    flex-wrap: wrap;
  }

  .dash-card {
    flex: 1 1 100%;
    padding: 10px 14px;
  }

  .dash-card-inner {
    flex-direction: row;
    align-items: center;
    gap: 12px;
  }

  .dash-icon {
    font-size: 22px;
    flex-shrink: 0;
  }

  .dash-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
    flex: 1;
  }

  .dash-bar {
    display: none;
  }

  .chat-area {
    min-height: calc(100vh - 200px);
    margin-bottom: 8px;
  }

  .dot-grid {
    background-size: 24px 24px;
  }
}
</style>
