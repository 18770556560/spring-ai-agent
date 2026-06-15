<template>
  <ChatRoom
    theme="knowledge"
    title="污染防控知识助手"
    subtitle="基于本地知识库 · RAG 流式问答"
    :session-id="chatId"
    :messages="messages"
    :loading="loading"
    empty-icon="🍃"
    empty-title="您好，我是污染防控知识助手"
    empty-description="我可以帮您检索污染防控相关知识，请随时提问"
    :suggestions="suggestions"
    input-placeholder="请输入污染防控相关问题..."
    avatar-label="知"
    @send="handleSend"
    @suggest="handleSend"
  />
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import ChatRoom from '@/components/ChatRoom.vue'
import { generateChatId } from '@/utils/uuid'
import { streamSSE, buildUrl } from '@/utils/sse'

const chatId = ref('')
const messages = ref([])
const loading = ref(false)
let abortController = null
let messageIdCounter = 0

const suggestions = [
  '什么是 VOCs 污染？',
  '工业废水应急处理步骤有哪些？',
  '大气污染监测的主要指标是什么？'
]

const SSE_ENDPOINT = '/api/ai/for_love/chat/sse'

onMounted(() => {
  chatId.value = generateChatId()
})

onUnmounted(() => {
  abortController?.abort()
})

function nextId() {
  messageIdCounter += 1
  return `msg-${messageIdCounter}`
}

function handleSend(text) {
  if (!text.trim() || loading.value) return

  messages.value.push({
    id: nextId(),
    role: 'user',
    content: text.trim()
  })

  const aiMessage = {
    id: nextId(),
    role: 'assistant',
    content: '',
    streaming: true
  }
  messages.value.push(aiMessage)
  loading.value = true

  abortController?.abort()
  abortController = new AbortController()

  const url = buildUrl(SSE_ENDPOINT, {
    message: text.trim(),
    chatId: chatId.value
  })

  streamSSE(url, {
    signal: abortController.signal,
    onMessage: (chunk) => {
      aiMessage.content += chunk
    },
    onError: (err) => {
      aiMessage.content = aiMessage.content || `抱歉，请求出错：${err.message}`
      aiMessage.streaming = false
      loading.value = false
    },
    onComplete: () => {
      aiMessage.streaming = false
      if (!aiMessage.content) {
        aiMessage.content = '未收到回复，请稍后重试。'
      }
      loading.value = false
    }
  })
}
</script>
