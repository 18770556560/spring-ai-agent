<template>
  <div class="ai-avatar-fallback" :class="type">
    <span v-if="type === 'love'" class="avatar-emoji">🌱</span>
    <span v-else class="avatar-emoji">⚡</span>
  </div>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'default'
  }
})
</script>

<style scoped>
.ai-avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all var(--transition-base, 0.3s);
}

.avatar-emoji {
  font-size: 16px;
  line-height: 1;
}

/* 污染防控大师 - 环保科技绿 */
.love {
  background: var(--ai-avatar-bg-love, linear-gradient(135deg, #059669, #0d9488));
  box-shadow: var(--ai-avatar-shadow-love, 0 2px 8px rgba(5, 150, 105, 0.35));
}

/* 超级智能体 - 未来科技蓝紫 */
.super {
  background: var(--ai-avatar-bg-super, linear-gradient(135deg, #4f46e5, #7c3aed));
  box-shadow: var(--ai-avatar-shadow-super, 0 0 12px rgba(99, 102, 241, 0.5));
}

.default {
  background: linear-gradient(135deg, #3b82f6, #6366f1);
}
</style>
