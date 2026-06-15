/**
 * 解析 Manus 多步骤 SSE 消息
 * 格式: "步骤: {n} , 结果: {result}"
 */
const STEP_PATTERN = /^步骤:\s*(\d+)\s*,\s*结果:\s*(.*)$/s
const ERROR_PATTERN = /^错误[:：]/
const COMPLETE_PATTERN = /^执行结束[:：]/

export function parseManusEvent(raw) {
  const text = (raw ?? '').trim()
  if (!text) return null

  if (ERROR_PATTERN.test(text)) {
    return { type: 'error', content: text }
  }

  if (COMPLETE_PATTERN.test(text)) {
    return { type: 'complete', content: text }
  }

  const match = text.match(STEP_PATTERN)
  if (match) {
    return {
      type: 'step',
      step: Number(match[1]),
      content: match[2].trim()
    }
  }

  return { type: 'info', content: text }
}

export function formatStepLabel(step) {
  return `步骤 ${step}`
}
