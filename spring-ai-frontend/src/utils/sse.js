/**
 * SSE 流式请求工具（基于 fetch + ReadableStream）
 * 兼容 Spring WebFlux Flux<String> 与 SseEmitter
 */
export async function streamSSE(url, { onMessage, onError, onComplete, signal }) {
  let response
  try {
    response = await fetch(url, {
      method: 'GET',
      headers: {
        Accept: 'text/event-stream'
      },
      signal
    })
  } catch (err) {
    if (err.name === 'AbortError') return
    onError?.(err)
    return
  }

  if (!response.ok) {
    onError?.(new Error(`请求失败: ${response.status} ${response.statusText}`))
    return
  }

  const reader = response.body?.getReader()
  if (!reader) {
    onError?.(new Error('无法读取响应流'))
    return
  }

  const decoder = new TextDecoder()
  let buffer = ''

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const events = buffer.split('\n\n')
      buffer = events.pop() ?? ''

      for (const eventBlock of events) {
        const data = extractSSEData(eventBlock)
        if (data !== null) {
          onMessage?.(data)
        }
      }
    }

    if (buffer.trim()) {
      const data = extractSSEData(buffer)
      if (data !== null) {
        onMessage?.(data)
      }
    }

    onComplete?.()
  } catch (err) {
    if (err.name !== 'AbortError') {
      onError?.(err)
    }
  } finally {
    reader.releaseLock()
  }
}

function extractSSEData(block) {
  const lines = block.split('\n')
  const dataLines = []

  for (const line of lines) {
    if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trimStart())
    } else if (line.trim() && !line.startsWith(':') && !line.startsWith('event:') && !line.startsWith('id:')) {
      dataLines.push(line)
    }
  }

  if (dataLines.length === 0) return null
  return dataLines.join('\n')
}

export function buildUrl(base, params) {
  const url = new URL(base, window.location.origin)
  Object.entries(params).forEach(([key, value]) => {
    if (value != null && value !== '') {
      url.searchParams.set(key, value)
    }
  })
  return url.toString()
}
