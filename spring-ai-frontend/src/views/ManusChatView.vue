<template>
  <ChatRoom
    theme="manus"
    title="污染防控AI超级助手"
    subtitle="多步骤智能体 · 自主规划与执行"
    :messages="messages"
    :loading="loading"
    empty-icon="🚀"
    empty-title="您好，我是 AI 超级助手"
    empty-description="我可以分步骤为您规划并执行复杂的污染防控任务"
    :suggestions="suggestions"
    input-placeholder="描述您需要解决的复杂问题..."
    avatar-label="超"
    @send="handleSend"
    @suggest="handleSend"
  />
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import ChatRoom from '@/components/ChatRoom.vue'
import { streamSSE, buildUrl } from '@/utils/sse'
import { parseManusEvent, formatStepLabel } from '@/utils/manusParser'

const messages = ref([])
const loading = ref(false)
let abortController = null
let messageIdCounter = 0

const suggestions = [
  '帮我分析某工厂废气排放超标的可能原因',
  '制定一份突发水污染事件应急响应方案',
  '查询并总结最新的 VOCs 治理技术'
]

const SSE_ENDPOINT = '/api/ai/manus/chat'

onUnmounted(() => {
  abortController?.abort()
})

function nextId() {
  messageIdCounter += 1
  return `msg-${messageIdCounter}`
}

function addAssistantMessage({ content, label = '', variant = '', streaming = false }) {
  const msg = {
    id: nextId(),
    role: 'assistant',
    content,
    label,
    variant,
    streaming
  }
  messages.value.push(msg)
  return msg
}

function handleSend(text) {
  if (!text.trim() || loading.value) return

  messages.value.push({
    id: nextId(),
    role: 'user',
    content: text.trim()
  })

  loading.value = true
  abortController?.abort()
  abortController = new AbortController()

  const url = buildUrl(SSE_ENDPOINT, { message: text.trim() })

  streamSSE(url, {
    signal: abortController.signal,
    onMessage: (raw) => {
      const parsed = parseManusEvent(raw)
      if (!parsed) return

      if (parsed.type === 'step') {
        addAssistantMessage({
          content: parsed.content,
          label: formatStepLabel(parsed.step),
          variant: 'step'
        })
      } else if (parsed.type === 'error') {
        addAssistantMessage({
          content: parsed.content,
          variant: 'error'
        })
      } else if (parsed.type === 'complete') {
        addAssistantMessage({
          content: parsed.content,
          variant: 'complete'
        })
      } else {
        addAssistantMessage({
          content: parsed.content
        })
      }
    },
    onError: (err) => {
      addAssistantMessage({
        content: `抱歉，请求出错：${err.message}`,
        variant: 'error'
      })
      loading.value = false
    },
    onComplete: () => {
      loading.value = false
    }
  })
}
</script>
