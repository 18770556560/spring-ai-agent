import axios from 'axios'

// 根据环境变量设置 API 基础 URL
const API_BASE_URL = process.env.NODE_ENV === 'production' 
 ? '/api' // 生产环境使用相对路径，适用于前后端部署在同一域名下
 : 'http://localhost:8080/api' // 开发环境指向本地后端服务

// 创建axios实例
const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000
})

// 封装SSE连接
export const connectSSE = (url, params, onMessage, onError) => {
  // 构建带参数的URL
  const queryString = Object.keys(params)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')
  
  const fullUrl = `${API_BASE_URL}${url}?${queryString}`
  
  // 创建EventSource
  const eventSource = new EventSource(fullUrl)
  
  eventSource.onmessage = event => {
    let data = event.data
    
    // 检查是否是特殊标记
    if (data === '[DONE]') {
      if (onMessage) onMessage('[DONE]')
    } else {
      // 处理普通消息
      if (onMessage) onMessage(data)
    }
  }
  
  eventSource.onerror = error => {
    if (onError) onError(error)
    eventSource.close()
  }
  
  // 返回eventSource实例，以便后续可以关闭连接
  return eventSource
}

// AI污染防控大师聊天
export const chatWithLoveApp = (message, chatId) => {
  return connectSSE('/ai/for_love/chat/sse', { message, chatId })
}

// AI超级智能体聊天
export const chatWithManus = (message) => {
  return connectSSE('/ai/manus/chat', { message })
}

// ==================== 知识库文档管理 API ====================

/**
 * 上传文档（multipart/form-data）
 * @param {File} file - 文件对象
 * @returns {Promise} { success, taskId, fileName, fileSize, status, message }
 */
export const uploadDocument = async (file) => {
  const formData = new FormData()
  formData.append('file', file)
  const response = await request.post('/document/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000 // 上传超时 2 分钟
  })
  return response.data
}

/**
 * 查询文档处理状态
 * @param {string} taskId - 任务ID
 * @returns {Promise} { success, taskId, fileName, status, chunkCount, errorMsg, createTime, updateTime }
 */
export const getDocumentStatus = async (taskId) => {
  const response = await request.get(`/document/status/${taskId}`)
  return response.data
}

/**
 * 获取文档任务列表
 * @returns {Promise} { success, total, data: [...] }
 */
export const getDocumentList = async () => {
  const response = await request.get('/document/list')
  return response.data
}

/**
 * 删除文档任务
 * @param {string} taskId - 任务ID
 * @returns {Promise} { success, message }
 */
export const deleteDocument = async (taskId) => {
  const response = await request.delete(`/document/${taskId}`)
  return response.data
}

export default {
  chatWithLoveApp,
  chatWithManus,
  uploadDocument,
  getDocumentStatus,
  getDocumentList,
  deleteDocument
}