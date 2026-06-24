<template>
  <div class="home-container">
    <!-- 背景效果 -->
    <div class="bg-layer">
      <div class="dot-grid"></div>
      <div class="scan-lines"></div>
    </div>
    <div class="bg-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>

    <!-- 头部 -->
    <header class="hero">
      <div class="glitch-wrapper">
        <h1 class="hero-title" data-text="CCAI超级智能体">CCAI超级智能体</h1>
      </div>
      <p class="hero-subtitle">NEXT-GEN AI AGENT PLATFORM</p>
      <div class="hero-line">
        <span class="line-dot"></span>
        <span class="line-dash"></span>
        <span class="line-dot"></span>
      </div>
      <p class="hero-desc">探索企业级人工智能的无限可能</p>
    </header>

    <!-- 卡片区 -->
    <main class="cards-container">
      <!-- 污染防控智能助手 -->
      <div class="app-card card-env" @click="navigateTo('/love-master')">
        <div class="card-bg-pattern"></div>
        <div class="card-header-glow"></div>
        <div class="card-icon-wrapper">
          <div class="card-icon">🛡️</div>
          <div class="icon-ring"></div>
        </div>
        <div class="card-body">
          <h2 class="card-title">污染防控智能助手</h2>
          <p class="card-desc">企业环保知识库 · RAG增强检索</p>
          <div class="card-tags">
            <span class="tag">知识图谱</span>
            <span class="tag">智能问答</span>
            <span class="tag">合规查询</span>
          </div>
        </div>
        <div class="card-footer">
          <div class="card-btn">
            <span>进入系统</span>
            <span class="btn-arrow">→</span>
          </div>
          <div class="card-status">
            <span class="status-dot online"></span>
            <span>运行中</span>
          </div>
        </div>
      </div>

      <!-- 超级AI智能体 -->
      <div class="app-card card-ai" @click="navigateTo('/super-agent')">
        <div class="card-bg-pattern"></div>
        <div class="card-header-glow"></div>
        <div class="card-icon-wrapper">
          <div class="card-icon">⚡</div>
          <div class="icon-ring"></div>
        </div>
        <div class="card-body">
          <h2 class="card-title">超级AI智能体</h2>
          <p class="card-desc">神经网络引擎 · 多领域专家</p>
          <div class="card-tags">
            <span class="tag">深度推理</span>
            <span class="tag">多模态</span>
            <span class="tag">实时响应</span>
          </div>
        </div>
        <div class="card-footer">
          <div class="card-btn">
            <span>进入系统</span>
            <span class="btn-arrow">→</span>
          </div>
          <div class="card-status">
            <span class="status-dot online"></span>
            <span>运行中</span>
          </div>
        </div>
      </div>

      <!-- 知识库管理 -->
      <div class="app-card card-kb" @click="navigateTo('/knowledge-manager')">
        <div class="card-bg-pattern"></div>
        <div class="card-header-glow"></div>
        <div class="card-icon-wrapper">
          <div class="card-icon">📚</div>
          <div class="icon-ring"></div>
        </div>
        <div class="card-body">
          <h2 class="card-title">知识库管理</h2>
          <p class="card-desc">异步文档处理 · 向量索引构建</p>
          <div class="card-tags">
            <span class="tag">文档上传</span>
            <span class="tag">智能分块</span>
            <span class="tag">RabbitMQ</span>
          </div>
        </div>
        <div class="card-footer">
          <div class="card-btn">
            <span>进入系统</span>
            <span class="btn-arrow">→</span>
          </div>
          <div class="card-status">
            <span class="status-dot online"></span>
            <span>运行中</span>
          </div>
        </div>
      </div>
    </main>

    <!-- 底部信息条 -->
    <div class="info-bar">
      <div class="info-item">
        <span class="info-value">3</span>
        <span class="info-label">AI 服务</span>
      </div>
      <div class="info-divider"></div>
      <div class="info-item">
        <span class="info-value">99.9%</span>
        <span class="info-label">可用性</span>
      </div>
      <div class="info-divider"></div>
      <div class="info-item">
        <span class="info-value">&lt;1.5s</span>
        <span class="info-label">平均响应</span>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import AppFooter from '../components/AppFooter.vue'

useHead({
  title: 'CCAI超级智能体应用平台 - 企业级AI智能服务',
  meta: [
    {
      name: 'description',
      content: 'CCAI超级智能体应用平台，提供污染防控智能助手与超级AI智能体两大企业级AI服务，赋能生产，智联未来。'
    },
    {
      name: 'keywords',
      content: 'AI智能体,企业AI,污染防控,环保知识库,超级AI,智能助手,CCAI,神经网络,RAG'
    }
  ]
})

const router = useRouter()

const navigateTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
/* ========================================
   CCAI 首页 - 企业科技门户
   设计理念：高科技产品展示中心
   ======================================== */

.home-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #0a0e1a;
  position: relative;
  overflow: hidden;
}

/* -------- 背景层 -------- */
.bg-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.dot-grid {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(59, 130, 246, 0.08) 1px, transparent 1px);
  background-size: 40px 40px;
  mask-image: radial-gradient(ellipse 60% 50% at 50% 40%, black 40%, transparent 80%);
  -webkit-mask-image: radial-gradient(ellipse 60% 50% at 50% 40%, black 40%, transparent 80%);
}

.scan-lines {
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 3px,
    rgba(0, 0, 0, 0.02) 3px,
    rgba(0, 0, 0, 0.02) 6px
  );
}

.bg-orbs {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(140px);
}

.orb-1 {
  width: 500px;
  height: 500px;
  background: rgba(59, 130, 246, 0.08);
  top: -150px;
  right: -150px;
  animation: orbFloat 20s ease-in-out infinite;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: rgba(139, 92, 246, 0.06);
  bottom: 20%;
  left: -120px;
  animation: orbFloat 25s ease-in-out infinite reverse;
}

.orb-3 {
  width: 350px;
  height: 350px;
  background: rgba(5, 150, 105, 0.05);
  top: 50%;
  right: 15%;
  animation: orbFloat 18s ease-in-out infinite;
}

@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

/* -------- 头部 -------- */
.hero {
  padding: 80px 20px 50px;
  text-align: center;
  position: relative;
  z-index: 2;
}

.glitch-wrapper {
  display: inline-block;
  position: relative;
  margin-bottom: 16px;
}

.hero-title {
  font-family: var(--font-display);
  font-size: 3.4rem;
  font-weight: 800;
  color: #f1f5f9;
  letter-spacing: 3px;
  position: relative;
  text-shadow: 0 0 30px rgba(59, 130, 246, 0.3);
}

/* 故障效果层 */
.hero-title::before,
.hero-title::after {
  content: attr(data-text);
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
}

.hero-title::before {
  color: #3b82f6;
  animation: glitchBlue 4s infinite;
  z-index: -1;
}

.hero-title::after {
  color: #8b5cf6;
  animation: glitchPurple 4s infinite;
  z-index: -2;
}

@keyframes glitchBlue {
  0%, 90%, 100% { opacity: 0; transform: translate(0); }
  91% { opacity: 0.6; transform: translate(-3px, 2px); }
  93% { opacity: 0.6; transform: translate(2px, -2px); }
  95% { opacity: 0; transform: translate(0); }
}

@keyframes glitchPurple {
  0%, 90%, 100% { opacity: 0; transform: translate(0); }
  92% { opacity: 0.5; transform: translate(3px, -1px); }
  94% { opacity: 0; transform: translate(0); }
}

.hero-subtitle {
  font-family: var(--font-mono);
  font-size: 0.85rem;
  letter-spacing: 6px;
  color: #475569;
  margin-bottom: 20px;
}

.hero-line {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 20px;
}

.line-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #3b82f6;
  box-shadow: 0 0 12px rgba(59, 130, 246, 0.6);
}

.line-dash {
  width: 60px;
  height: 1px;
  background: linear-gradient(90deg, transparent, #3b82f6, transparent);
}

.hero-desc {
  font-size: 1.05rem;
  color: #64748b;
  max-width: 500px;
  margin: 0 auto;
  line-height: 1.8;
  letter-spacing: 1px;
}

/* -------- 卡片区 -------- */
.cards-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 40px;
  max-width: 1100px;
  margin: 40px auto 50px;
  padding: 0 24px;
  flex: 1;
  position: relative;
  z-index: 2;
}

.app-card {
  width: 420px;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: var(--radius-xl, 24px);
  padding: 36px 32px 28px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.app-card::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  padding: 1px;
  background: linear-gradient(135deg, rgba(255,255,255,0.06), transparent 50%, rgba(255,255,255,0.03));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  pointer-events: none;
}

.card-bg-pattern {
  position: absolute;
  inset: 0;
  opacity: 0.03;
  pointer-events: none;
  background-image:
    linear-gradient(60deg, currentColor 25%, transparent 25%),
    linear-gradient(-60deg, currentColor 25%, transparent 25%);
  background-size: 20px 35px;
}

.card-header-glow {
  position: absolute;
  top: 0;
  left: 20%;
  right: 20%;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--glow-color, #3b82f6), transparent);
  filter: blur(2px);
  transition: all 0.5s;
}

.card-env {
  --glow-color: #10b981;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.4);
}

.card-env:hover {
  --glow-color: #34d399;
}

.card-ai {
  --glow-color: #3b82f6;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.4);
}

.card-ai:hover {
  --glow-color: #60a5fa;
}

.card-kb {
  --glow-color: #8b5cf6;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.4);
}

.card-kb:hover {
  --glow-color: #a78bfa;
}

.app-card:hover {
  transform: translateY(-8px);
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.6),
    0 0 0 1px var(--glow-color, #3b82f6),
    0 0 40px rgba(59, 130, 246, 0.1);
}

.app-card:hover .card-header-glow {
  left: 5%;
  right: 5%;
}

.card-icon-wrapper {
  position: relative;
  margin-bottom: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-icon {
  width: 76px;
  height: 76px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  border-radius: 20px;
  position: relative;
  z-index: 1;
  transition: transform 0.4s;
}

.card-env .card-icon {
  background: linear-gradient(135deg, rgba(5, 150, 105, 0.15), rgba(13, 148, 136, 0.1));
  border: 1px solid rgba(5, 150, 105, 0.3);
  box-shadow: 0 8px 24px rgba(5, 150, 105, 0.1);
}

.card-ai .card-icon {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(139, 92, 246, 0.1));
  border: 1px solid rgba(59, 130, 246, 0.3);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.1);
}

.card-kb .card-icon {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.15), rgba(168, 85, 247, 0.1));
  border: 1px solid rgba(139, 92, 246, 0.3);
  box-shadow: 0 8px 24px rgba(139, 92, 246, 0.1);
}

.app-card:hover .card-icon {
  transform: scale(1.08);
}

.icon-ring {
  position: absolute;
  width: 90px;
  height: 90px;
  border-radius: 50%;
  border: 1px dashed var(--glow-color, #3b82f6);
  opacity: 0.3;
  animation: ringSpin 20s linear infinite;
}

@keyframes ringSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.card-body {
  text-align: center;
  margin-bottom: 28px;
}

.card-title {
  font-family: var(--font-display);
  font-size: 1.5rem;
  font-weight: 700;
  color: #f1f5f9;
  margin-bottom: 10px;
  letter-spacing: 1px;
}

.card-desc {
  font-size: 0.9rem;
  color: #64748b;
  margin-bottom: 18px;
  letter-spacing: 0.5px;
}

.card-tags {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  font-family: var(--font-mono);
  font-size: 11px;
  padding: 4px 12px;
  border-radius: var(--radius-full, 20px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #94a3b8;
  letter-spacing: 0.5px;
  background: rgba(255, 255, 255, 0.02);
}

.card-env .tag {
  border-color: rgba(5, 150, 105, 0.2);
  color: #6ee7b7;
}

.card-ai .tag {
  border-color: rgba(59, 130, 246, 0.2);
  color: #93c5fd;
}

.card-kb .tag {
  border-color: rgba(139, 92, 246, 0.2);
  color: #c4b5fd;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}

.card-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 22px;
  border-radius: var(--radius-full, 24px);
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
  color: #fff;
}

.card-env .card-btn {
  background: linear-gradient(135deg, #059669, #0d9488);
  box-shadow: 0 4px 16px rgba(5, 150, 105, 0.3);
}

.card-ai .card-btn {
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);
}

.card-kb .card-btn {
  background: linear-gradient(135deg, #7c3aed, #a855f7);
  box-shadow: 0 4px 16px rgba(139, 92, 246, 0.3);
}

.card-btn::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.15), transparent);
  transition: left 0.6s;
}

.app-card:hover .card-btn::after {
  left: 100%;
}

.card-btn:hover {
  transform: scale(1.05);
}

.btn-arrow {
  font-size: 16px;
  transition: transform 0.3s;
}

.app-card:hover .btn-arrow {
  transform: translateX(3px);
}

.card-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
  font-family: var(--font-mono);
  letter-spacing: 0.5px;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.status-dot.online {
  background: #22c55e;
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.5);
  animation: statusPulse 2s ease-in-out infinite;
}

@keyframes statusPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* -------- 底部信息条 -------- */
.info-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40px;
  padding: 24px 20px;
  position: relative;
  z-index: 2;
  margin-bottom: 0;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.info-value {
  font-family: var(--font-mono);
  font-size: 1.2rem;
  font-weight: 700;
  color: #cbd5e1;
  letter-spacing: 1px;
}

.info-label {
  font-family: var(--font-mono);
  font-size: 10px;
  color: #475569;
  letter-spacing: 3px;
  text-transform: uppercase;
}

.info-divider {
  width: 1px;
  height: 32px;
  background: linear-gradient(0deg, transparent, #1e293b, transparent);
}

/* ========================================
   响应式
   ======================================== */
@media (max-width: 768px) {
  .hero {
    padding: 60px 20px 40px;
  }

  .hero-title {
    font-size: 2.4rem;
  }

  .hero-subtitle {
    font-size: 0.75rem;
    letter-spacing: 4px;
  }

  .hero-desc {
    font-size: 0.95rem;
  }

  .cards-container {
    gap: 28px;
    margin: 30px auto 40px;
    padding: 0 16px;
  }

  .app-card {
    width: 100%;
    max-width: 420px;
    padding: 28px 24px 24px;
  }

  .card-icon {
    width: 64px;
    height: 64px;
    font-size: 30px;
    border-radius: 16px;
  }

  .icon-ring {
    width: 76px;
    height: 76px;
  }

  .card-title {
    font-size: 1.3rem;
  }

  .info-bar {
    gap: 28px;
  }
}

@media (max-width: 480px) {
  .hero {
    padding: 50px 16px 30px;
  }

  .hero-title {
    font-size: 1.8rem;
    letter-spacing: 2px;
  }

  .hero-subtitle {
    font-size: 0.7rem;
    letter-spacing: 3px;
  }

  .hero-desc {
    font-size: 0.85rem;
  }

  .cards-container {
    margin: 20px auto 30px;
    padding: 0 12px;
    gap: 20px;
  }

  .app-card {
    padding: 24px 20px 20px;
    border-radius: 20px;
  }

  .card-icon {
    width: 56px;
    height: 56px;
    font-size: 26px;
    border-radius: 14px;
    margin-bottom: 20px;
  }

  .icon-ring {
    display: none;
  }

  .card-title {
    font-size: 1.2rem;
  }

  .card-desc {
    font-size: 0.8rem;
  }

  .card-tags {
    gap: 6px;
  }

  .tag {
    font-size: 10px;
    padding: 3px 10px;
  }

  .card-footer {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .info-bar {
    gap: 20px;
    padding: 16px 12px;
  }

  .info-value {
    font-size: 1rem;
  }

  .info-label {
    font-size: 9px;
    letter-spacing: 2px;
  }

  .orb-1, .orb-2, .orb-3 {
    opacity: 0.6;
  }
}
</style>
