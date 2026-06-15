<template>
  <div class="home-page">
    <div class="home-bg" aria-hidden="true">
      <div class="grid-pattern"></div>
      <div class="glow glow-1"></div>
      <div class="glow glow-2"></div>
    </div>

    <div class="home-content">
      <header class="home-header">
        <div class="logo-wrap">
          <span class="logo-icon">🌿</span>
          <div>
            <h1 class="home-title">污染防控智能助手平台</h1>
            <p class="home-tagline">专业 · 智能 · 守护绿水青山</p>
          </div>
        </div>
      </header>

      <section class="apps-section">
        <h2 class="section-title">选择应用</h2>
        <div class="app-cards">
          <router-link
            v-for="app in apps"
            :key="app.route"
            :to="app.route"
            class="app-card"
            :class="`app-card--${app.theme}`"
          >
            <div class="card-glow" aria-hidden="true"></div>
            <div class="card-icon">{{ app.icon }}</div>
            <h3 class="card-title">{{ app.title }}</h3>
            <p class="card-desc">{{ app.description }}</p>
            <ul class="card-features">
              <li v-for="(feat, i) in app.features" :key="i">{{ feat }}</li>
            </ul>
            <span class="card-cta">
              进入应用
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M5 12h14M12 5l7 7-7 7" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
          </router-link>
        </div>
      </section>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import AppFooter from '@/components/AppFooter.vue'

const apps = [
  {
    route: '/knowledge',
    theme: 'knowledge',
    icon: '🍃',
    title: '污染防控知识助手',
    description: '基于本地知识库的 RAG 智能问答，快速检索污染防控专业知识。',
    features: ['本地知识库检索', 'SSE 流式对话', '会话上下文记忆']
  },
  {
    route: '/manus',
    theme: 'manus',
    icon: '🚀',
    title: '污染防控AI超级助手',
    description: '多步骤智能体，自主规划任务并逐步执行，解决复杂防控问题。',
    features: ['多步骤推理', '工具调用能力', '实时步骤展示']
  }
]
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(160deg, #0c1f17 0%, #1a3a2f 30%, #0f2922 60%, #1e293b 100%);
  color: #ecfdf5;
  position: relative;
  overflow: hidden;
}

.home-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.grid-pattern {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 48px 48px;
}

.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
}

.glow-1 {
  width: 500px;
  height: 500px;
  top: -150px;
  left: -100px;
  background: rgba(16, 185, 129, 0.2);
}

.glow-2 {
  width: 450px;
  height: 450px;
  bottom: -100px;
  right: -80px;
  background: rgba(99, 102, 241, 0.18);
}

.home-content {
  position: relative;
  z-index: 1;
  flex: 1;
  max-width: 1100px;
  width: 100%;
  margin: 0 auto;
  padding: 3rem 1.5rem 2rem;
}

.home-header {
  text-align: center;
  margin-bottom: 3rem;
}

.logo-wrap {
  display: inline-flex;
  align-items: center;
  gap: 1rem;
  text-align: left;
}

.logo-icon {
  font-size: 3rem;
  filter: drop-shadow(0 4px 12px rgba(16, 185, 129, 0.4));
}

.home-title {
  font-size: clamp(1.6rem, 4vw, 2.4rem);
  font-weight: 800;
  background: linear-gradient(135deg, #ecfdf5, #6ee7b7, #a7f3d0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.home-tagline {
  margin-top: 0.35rem;
  font-size: 0.95rem;
  color: #94a3b8;
  letter-spacing: 0.08em;
}

.section-title {
  text-align: center;
  font-size: 1rem;
  font-weight: 600;
  color: #94a3b8;
  margin-bottom: 1.75rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.app-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.app-card {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 2rem 1.75rem;
  border-radius: 20px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.app-card--knowledge {
  background: linear-gradient(145deg, rgba(6, 78, 59, 0.6), rgba(16, 185, 129, 0.15));
}

.app-card--manus {
  background: linear-gradient(145deg, rgba(49, 46, 129, 0.6), rgba(139, 92, 246, 0.15));
}

.card-glow {
  position: absolute;
  top: -50%;
  right: -30%;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.app-card--knowledge .card-glow {
  background: rgba(16, 185, 129, 0.4);
}

.app-card--manus .card-glow {
  background: rgba(139, 92, 246, 0.4);
}

.app-card:hover {
  transform: translateY(-6px);
}

.app-card:hover .card-glow {
  opacity: 1;
}

.card-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin-bottom: 0.6rem;
}

.card-desc {
  font-size: 0.88rem;
  color: #cbd5e1;
  line-height: 1.6;
  margin-bottom: 1rem;
}

.card-features {
  list-style: none;
  margin-bottom: 1.5rem;
  flex: 1;
}

.card-features li {
  font-size: 0.82rem;
  color: #94a3b8;
  padding: 0.3rem 0;
  padding-left: 1.1rem;
  position: relative;
}

.card-features li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #6ee7b7;
  font-size: 0.75rem;
}

.app-card--manus .card-features li::before {
  color: #c4b5fd;
}

.card-cta {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.88rem;
  font-weight: 600;
  color: #ecfdf5;
  margin-top: auto;
}

.app-card--knowledge .card-cta {
  color: #6ee7b7;
}

.app-card--manus .card-cta {
  color: #c4b5fd;
}

.app-card:hover .card-cta svg {
  transform: translateX(4px);
}

.card-cta svg {
  transition: transform 0.2s ease;
}

:deep(.app-footer) {
  --footer-bg: rgba(15, 23, 42, 0.8);
  --footer-text: #64748b;
  --header-border: rgba(255, 255, 255, 0.06);
}
</style>
