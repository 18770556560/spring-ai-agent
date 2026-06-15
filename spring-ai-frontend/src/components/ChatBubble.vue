<template>
  <div
    class="chat-bubble-row"
    :class="[
      `chat-bubble-row--${role}`,
      variant ? `chat-bubble-row--${variant}` : ''
    ]"
  >
    <div v-if="role === 'assistant'" class="avatar avatar--ai">
      <slot name="avatar-ai">
        <span>{{ avatarLabel }}</span>
      </slot>
    </div>

    <div class="bubble-wrap">
      <div v-if="label" class="bubble-label">{{ label }}</div>
      <div
        class="bubble"
        :class="{
          'bubble--streaming': streaming,
          'bubble--error': variant === 'error',
          'bubble--complete': variant === 'complete'
        }"
      >
        <div class="bubble-content markdown-body" v-html="renderedContent"></div>
        <span v-if="streaming" class="streaming-cursor" aria-hidden="true"></span>
      </div>
      <div v-if="timestamp" class="bubble-time">{{ timestamp }}</div>
    </div>

    <div v-if="role === 'user'" class="avatar avatar--user">
      <slot name="avatar-user">
        <span>我</span>
      </slot>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'

const props = defineProps({
  role: {
    type: String,
    required: true,
    validator: (v) => ['user', 'assistant'].includes(v)
  },
  content: {
    type: String,
    default: ''
  },
  streaming: {
    type: Boolean,
    default: false
  },
  label: {
    type: String,
    default: ''
  },
  variant: {
    type: String,
    default: '',
    validator: (v) => ['', 'error', 'complete', 'step'].includes(v)
  },
  avatarLabel: {
    type: String,
    default: 'AI'
  },
  timestamp: {
    type: String,
    default: ''
  },
  enableMarkdown: {
    type: Boolean,
    default: true
  }
})

marked.setOptions({
  breaks: true,
  gfm: true
})

const renderedContent = computed(() => {
  if (!props.content) {
    return props.streaming ? '' : '<span class="placeholder">...</span>'
  }
  if (props.enableMarkdown && props.role === 'assistant') {
    return marked.parse(props.content)
  }
  return escapeHtml(props.content).replace(/\n/g, '<br>')
})

function escapeHtml(text) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}
</script>

<style scoped>
.chat-bubble-row {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  margin-bottom: 1.25rem;
  animation: bubble-in 0.35s ease;
}

.chat-bubble-row--user {
  flex-direction: row-reverse;
}

.avatar {
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.avatar--ai {
  background: var(--avatar-ai-bg);
}

.avatar--user {
  background: var(--avatar-user-bg);
}

.bubble-wrap {
  max-width: min(78%, 640px);
  min-width: 120px;
}

.bubble-label {
  font-size: 0.72rem;
  font-weight: 600;
  color: var(--badge-text);
  background: var(--badge-bg);
  display: inline-block;
  padding: 0.15rem 0.55rem;
  border-radius: 999px;
  margin-bottom: 0.35rem;
}

.bubble {
  position: relative;
  padding: 0.85rem 1.1rem;
  border-radius: 16px;
  min-height: 2.75rem;
  line-height: 1.6;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.2s ease;
}

.chat-bubble-row--assistant .bubble {
  background: var(--ai-bubble-bg);
  border: 1px solid var(--ai-bubble-border);
  color: var(--ai-bubble-text);
  border-top-left-radius: 4px;
}

.chat-bubble-row--user .bubble {
  background: var(--user-bubble-bg);
  color: var(--user-bubble-text);
  border-top-right-radius: 4px;
}

.bubble--streaming {
  min-height: 3.5rem;
}

.bubble--error {
  background: var(--error-bg) !important;
  border-color: var(--error-border) !important;
  color: var(--error-text) !important;
}

.bubble--complete {
  background: var(--complete-bg) !important;
  border-color: var(--complete-border) !important;
  color: var(--complete-text) !important;
}

.bubble-content {
  min-height: 1.2em;
}

.bubble-content :deep(.placeholder) {
  opacity: 0.4;
}

.bubble-time {
  margin-top: 0.3rem;
  font-size: 0.68rem;
  color: var(--text-muted);
  text-align: right;
}

.chat-bubble-row--assistant .bubble-time {
  text-align: left;
}

@keyframes bubble-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
