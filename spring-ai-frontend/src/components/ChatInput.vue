<template>
  <div class="chat-input-area">
    <div class="input-shell">
      <textarea
        ref="textareaRef"
        v-model="inputText"
        class="chat-textarea"
        :placeholder="placeholder"
        :disabled="disabled"
        rows="1"
        @keydown="handleKeydown"
        @input="autoResize"
      ></textarea>
      <button
        class="send-btn"
        type="button"
        :disabled="disabled || !inputText.trim()"
        :title="disabled ? '正在回复中...' : '发送消息'"
        @click="handleSend"
      >
        <svg v-if="!disabled" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M22 2L11 13" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M22 2L15 22L11 13L2 9L22 2Z" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span v-else class="loading-dots">
          <i></i><i></i><i></i>
        </span>
      </button>
    </div>
    <p class="input-hint">{{ hint }}</p>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: '输入您的问题...'
  },
  hint: {
    type: String,
    default: 'Enter 发送 · Shift+Enter 换行'
  }
})

const emit = defineEmits(['send'])

const inputText = ref('')
const textareaRef = ref(null)

function handleSend() {
  const text = inputText.value.trim()
  if (!text || props.disabled) return
  emit('send', text)
  inputText.value = ''
  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
    }
  })
}

function handleKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

function autoResize() {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = `${Math.min(el.scrollHeight, 160)}px`
}
</script>

<style scoped>
.chat-input-area {
  flex-shrink: 0;
  padding: 1rem 1.25rem 0.75rem;
  background: var(--input-bg);
  border-top: 1px solid var(--input-border);
  backdrop-filter: blur(16px);
}

.input-shell {
  display: flex;
  align-items: flex-end;
  gap: 0.75rem;
  padding: 0.65rem 0.65rem 0.65rem 1rem;
  border-radius: 16px;
  border: 1px solid var(--input-border);
  background: rgba(255, 255, 255, 0.04);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.input-shell:focus-within {
  border-color: var(--input-focus);
  box-shadow: 0 0 0 3px var(--accent-soft);
}

.chat-textarea {
  flex: 1;
  resize: none;
  border: none;
  outline: none;
  background: transparent;
  color: var(--text-primary);
  font-size: 0.95rem;
  line-height: 1.5;
  max-height: 160px;
  min-height: 24px;
}

.chat-textarea::placeholder {
  color: var(--text-muted);
}

.chat-textarea:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.send-btn {
  flex-shrink: 0;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--btn-bg);
  color: #fff;
  box-shadow: var(--btn-shadow);
  transition: transform 0.15s ease, opacity 0.15s ease;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
}

.send-btn:active:not(:disabled) {
  transform: scale(0.97);
}

.send-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
}

.input-hint {
  margin-top: 0.45rem;
  text-align: center;
  font-size: 0.72rem;
  color: var(--text-muted);
}

.loading-dots {
  display: flex;
  gap: 3px;
}

.loading-dots i {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #fff;
  animation: dot-bounce 1.2s ease-in-out infinite;
}

.loading-dots i:nth-child(2) {
  animation-delay: 0.15s;
}

.loading-dots i:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes dot-bounce {
  0%,
  80%,
  100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  40% {
    transform: translateY(-4px);
    opacity: 1;
  }
}
</style>
