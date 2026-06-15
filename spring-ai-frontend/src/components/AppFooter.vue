<template>
  <footer class="app-footer">
    <div class="footer-grid-bg"></div>
    <div class="footer-content">
      <div class="footer-section brand-section">
        <div class="footer-logo">
          <div class="logo-icon">
            <span class="logo-symbol">{{ logoIcon }}</span>
          </div>
          <div class="logo-text">
            <h3>CCAI超级智能体应用平台</h3>
            <p>让AI为生产赋能</p>
          </div>
        </div>
      </div>

      <div class="footer-section">
        <h4>法律声明</h4>
        <div class="footer-links">
          <a href="#">《用户协议》</a>
          <a href="#">《隐私政策》</a>
        </div>
      </div>

      <div class="footer-section">
        <h4>友情链接</h4>
        <div class="footer-links">
          <a href="https://www.mianshiya.com" target="_blank">面试鸭</a>
          <a href="#" target="_blank">编程导航</a>
          <a href="#" target="_blank">代码小抄</a>
        </div>
      </div>

      <div class="footer-section">
        <h4>联系我们</h4>
        <div class="footer-links">
          <a href="#">商务合作</a>
          <a href="#">站长：CC</a>
        </div>
      </div>
    </div>

    <div class="footer-divider"></div>

    <div class="footer-bottom">
      <p>© {{ currentYear }} CCAI超级智能体应用平台</p>
      <span class="footer-dot">·</span>
      <p>Powered by Spring AI & Claude</p>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())

// Logo icon depends on page theme
const logoIcon = computed(() => '◆')
</script>

<style scoped>
/* ========================================
   AppFooter - 主题化页脚组件
   ======================================== */

.app-footer {
  position: relative;
  background: var(--footer-bg, #ffffff);
  padding: 48px 0 24px;
  color: var(--footer-text, #475569);
  border-top: 1px solid var(--footer-border, #e2e8f0);
  width: 100%;
  margin-top: auto;
  z-index: 2;
  overflow: hidden;
}

/* 背景装饰 */
.footer-grid-bg {
  position: absolute;
  inset: 0;
  background-image: var(--footer-pattern, none);
  opacity: 0.04;
  pointer-events: none;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  padding: 0 24px;
  position: relative;
  z-index: 1;
}

.footer-section {
  flex: 1;
  min-width: 180px;
  margin-bottom: 28px;
  padding: 0 12px;
}

.brand-section {
  flex: 1.5;
  min-width: 240px;
}

.footer-logo {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.logo-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md, 10px);
  background: var(--footer-logo-bg, linear-gradient(135deg, #3b82f6, #8b5cf6));
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: var(--footer-logo-shadow, 0 4px 12px rgba(59, 130, 246, 0.3));
}

.logo-symbol {
  color: #fff;
  font-size: 20px;
  font-weight: bold;
}

.logo-text h3 {
  font-size: 1.1rem;
  margin-bottom: 4px;
  color: var(--footer-heading, #1e293b);
  font-weight: 600;
}

.logo-text p {
  font-size: 0.85rem;
  color: var(--text-muted, #94a3b8);
  letter-spacing: 1px;
}

.footer-section h4 {
  font-size: 0.9rem;
  margin-bottom: 14px;
  color: var(--footer-heading, #1e293b);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 2px;
  font-size: 0.8rem;
}

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.footer-links a {
  color: var(--footer-link, #64748b);
  text-decoration: none;
  transition: all var(--transition-fast, 0.15s);
  font-size: 0.9rem;
  position: relative;
  width: fit-content;
}

.footer-links a::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 0;
  height: 1px;
  background: var(--footer-link-hover, #3b82f6);
  transition: width var(--transition-base, 0.3s);
}

.footer-links a:hover {
  color: var(--footer-link-hover, #3b82f6);
}

.footer-links a:hover::after {
  width: 100%;
}

.footer-divider {
  max-width: 1200px;
  margin: 0 auto;
  height: 1px;
  background: var(--footer-border, #e2e8f0);
  opacity: 0.6;
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
  margin-top: 4px;
  color: var(--text-muted, #94a3b8);
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.footer-dot {
  color: var(--text-muted, #94a3b8);
}

/* 响应式 */
@media (max-width: 768px) {
  .app-footer {
    padding: 36px 0 20px;
  }

  .footer-content {
    flex-direction: column;
    gap: 8px;
  }

  .footer-section {
    width: 100%;
    margin-bottom: 20px;
    padding: 0;
  }

  .brand-section {
    min-width: unset;
  }

  .footer-logo {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .app-footer {
    padding: 28px 0 16px;
  }

  .footer-section h4 {
    font-size: 0.75rem;
    margin-bottom: 10px;
  }

  .footer-links a {
    font-size: 0.85rem;
  }

  .footer-bottom {
    font-size: 0.8rem;
    flex-direction: column;
    gap: 2px;
  }

  .footer-dot {
    display: none;
  }
}
</style>
