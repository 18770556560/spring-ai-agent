<template>
  <div class="km-container">
    <!-- 背景 -->
    <div class="bg-layer">
      <div class="dot-grid"></div>
      <div class="scan-lines"></div>
    </div>
    <div class="bg-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
    </div>

    <!-- 头部 -->
    <header class="km-header">
      <button class="back-btn" @click="goHome">
        <span class="back-arrow">←</span>
        <span>返回首页</span>
      </button>
      <h1 class="km-title">
        <span class="title-icon">📚</span>
        知识库管理
      </h1>
      <p class="km-subtitle">文档上传 · 智能分块 · 向量索引 · RAG 增强检索</p>
    </header>

    <!-- 上传区域 -->
    <section class="upload-section">
      <div
        class="upload-zone"
        :class="{ dragging: isDragging, uploading: isUploading }"
        @dragover.prevent="isDragging = true"
        @dragleave.prevent="isDragging = false"
        @drop.prevent="onDrop"
        @click="triggerFileInput"
      >
        <input
          ref="fileInput"
          type="file"
          accept=".md,.txt,.json,.pdf"
          class="file-input-hidden"
          @change="onFileSelected"
        />
        <div class="upload-content">
          <div class="upload-icon">{{ isUploading ? '⏳' : '📤' }}</div>
          <p class="upload-text" v-if="!isUploading">
            <span v-if="!isDragging">拖拽文件到此处，或<span class="link">点击上传</span></span>
            <span v-else>释放文件以上传</span>
          </p>
          <p class="upload-text" v-else>文件上传中...</p>
          <p class="upload-hint">支持 Markdown / TXT / JSON / PDF（最大 50MB）</p>
        </div>
      </div>

      <!-- 上传成功提示 -->
      <div v-if="uploadResult" class="upload-result" :class="uploadResult.success ? 'success' : 'error'">
        <span class="result-icon">{{ uploadResult.success ? '✅' : '❌' }}</span>
        <span>{{ uploadResult.message }}</span>
      </div>
    </section>

    <!-- 文档列表 -->
    <section class="doc-list-section">
      <div class="list-header">
        <h2 class="list-title">
          <span class="list-icon">📋</span>
          文档列表
          <span class="list-count">{{ tasks.length }}</span>
        </h2>
        <button class="refresh-btn" @click="loadTasks" :disabled="isLoading">
          <span class="refresh-icon" :class="{ spinning: isLoading }">🔄</span>
          <span>刷新</span>
        </button>
      </div>

      <!-- 加载状态 -->
      <div v-if="isLoading && tasks.length === 0" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="tasks.length === 0" class="empty-state">
        <div class="empty-icon">📭</div>
        <p class="empty-text">还没有上传任何文档</p>
        <p class="empty-hint">上传知识库文档，AI 将能基于这些文档进行智能问答</p>
      </div>

      <!-- 文档列表表格 -->
      <div v-else class="doc-table-wrapper">
        <table class="doc-table">
          <thead>
            <tr>
              <th class="col-name">文件名</th>
              <th class="col-type">类型</th>
              <th class="col-size">大小</th>
              <th class="col-status">状态</th>
              <th class="col-chunks">分块数</th>
              <th class="col-time">上传时间</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="task in tasks" :key="task.taskId" class="doc-row">
              <td class="col-name">
                <span class="file-icon">{{ getFileIcon(task.fileType) }}</span>
                <span class="file-name" :title="task.fileName">{{ task.fileName }}</span>
              </td>
              <td class="col-type">
                <span class="type-badge">{{ task.fileType?.toUpperCase() }}</span>
              </td>
              <td class="col-size">{{ formatFileSize(task.fileSize) }}</td>
              <td class="col-status">
                <span class="status-badge" :class="getStatusClass(task.status)">
                  <span class="status-dot"></span>
                  {{ getStatusLabel(task.status) }}
                </span>
                <span v-if="task.status === 'FAILED'" class="error-tooltip" :title="task.errorMsg">⚠️</span>
              </td>
              <td class="col-chunks">{{ task.chunkCount ?? '-' }}</td>
              <td class="col-time">{{ formatTime(task.createTime) }}</td>
              <td class="col-actions">
                <button
                  class="action-btn delete-btn"
                  @click="confirmDelete(task)"
                  :disabled="task.status === 'PROCESSING'"
                  title="删除文档"
                >
                  🗑️
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="table-footer">
          共 <strong>{{ tasks.length }}</strong> 个文档
          <span v-if="processingCount > 0" class="processing-hint">
            · {{ processingCount }} 个处理中（每 3 秒自动刷新）
          </span>
        </div>
      </div>
    </section>

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
      <div class="modal-card">
        <h3 class="modal-title">确认删除</h3>
        <p class="modal-body">
          确定要删除文档 <strong>"{{ deleteTarget?.fileName }}"</strong> 吗？
        </p>
        <p class="modal-hint">此操作将同时删除文件、数据库记录及 ES 索引数据，不可恢复。</p>
        <div class="modal-actions">
          <button class="modal-btn cancel" @click="showDeleteModal = false">取消</button>
          <button class="modal-btn confirm" @click="doDelete">确认删除</button>
        </div>
      </div>
    </div>

    <!-- 底部 -->
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import AppFooter from '../components/AppFooter.vue'
import {
  uploadDocument,
  getDocumentList,
  getDocumentStatus,
  deleteDocument
} from '../api/index.js'

useHead({
  title: '知识库管理 - CCAI超级智能体应用平台',
  meta: [
    {
      name: 'description',
      content: 'CCAI知识库管理 — 上传文档、智能分块、向量索引，为RAG增强检索提供知识基础。'
    }
  ]
})

const router = useRouter()

// ========== 状态 ==========
const fileInput = ref(null)
const isDragging = ref(false)
const isUploading = ref(false)
const uploadResult = ref(null)
const tasks = ref([])
const isLoading = ref(false)
const showDeleteModal = ref(false)
const deleteTarget = ref(null)
let pollTimer = null

// ========== 计算属性 ==========
const processingCount = computed(() =>
  tasks.value.filter(t => t.status === 'PENDING' || t.status === 'PROCESSING').length
)

// ========== 生命周期 ==========
onMounted(() => {
  loadTasks()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})

// ========== 方法 ==========
const goHome = () => router.push('/')

const triggerFileInput = () => {
  if (!isUploading.value) fileInput.value?.click()
}

const onDrop = (e) => {
  isDragging.value = false
  const file = e.dataTransfer?.files?.[0]
  if (file) handleUpload(file)
}

const onFileSelected = (e) => {
  const file = e.target?.files?.[0]
  if (file) handleUpload(file)
  // 重置 input 以便重复选择同一文件
  if (fileInput.value) fileInput.value.value = ''
}

const handleUpload = async (file) => {
  // 校验文件类型
  const allowedExts = ['.md', '.txt', '.json', '.pdf']
  const ext = '.' + file.name.split('.').pop()?.toLowerCase()
  if (!allowedExts.includes(ext)) {
    uploadResult.value = { success: false, message: `不支持的文件类型: ${ext}，仅支持: ${allowedExts.join(', ')}` }
    setTimeout(() => (uploadResult.value = null), 5000)
    return
  }

  isUploading.value = true
  uploadResult.value = null
  try {
    const result = await uploadDocument(file)
    uploadResult.value = result
    if (result.success) {
      await loadTasks()
    }
  } catch (err) {
    uploadResult.value = { success: false, message: '上传失败: ' + (err.response?.data?.message || err.message) }
  } finally {
    isUploading.value = false
    setTimeout(() => (uploadResult.value = null), 8000)
  }
}

const loadTasks = async () => {
  isLoading.value = true
  try {
    const result = await getDocumentList()
    if (result.success) {
      tasks.value = result.data || []
    }
  } catch (err) {
    console.error('加载文档列表失败:', err)
  } finally {
    isLoading.value = false
  }
}

const confirmDelete = (task) => {
  deleteTarget.value = task
  showDeleteModal.value = true
}

const doDelete = async () => {
  if (!deleteTarget.value) return
  try {
    await deleteDocument(deleteTarget.value.taskId)
    showDeleteModal.value = false
    deleteTarget.value = null
    await loadTasks()
  } catch (err) {
    console.error('删除失败:', err)
  }
}

// ========== 轮询刷新（处理中的任务自动更新状态） ==========
const startPolling = () => {
  pollTimer = setInterval(async () => {
    if (processingCount.value === 0) return
    // 只更新处理中的任务状态
    const processingTasks = tasks.value.filter(
      t => t.status === 'PENDING' || t.status === 'PROCESSING'
    )
    for (const task of processingTasks) {
      try {
        const result = await getDocumentStatus(task.taskId)
        if (result.success) {
          const idx = tasks.value.findIndex(t => t.taskId === task.taskId)
          if (idx >= 0) {
            tasks.value[idx] = { ...tasks.value[idx], ...result }
          }
        }
      } catch (err) {
        // 忽略单条查询失败
      }
    }
  }, 3000) // 每 3 秒轮询一次
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

// ========== 格式化工具 ==========
const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return new Date(timeStr).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

const getFileIcon = (type) => {
  const icons = { md: '📝', txt: '📄', json: '📋', pdf: '📕' }
  return icons[type?.toLowerCase()] || '📎'
}

const getStatusClass = (status) => {
  return {
    PENDING: 'status-pending',
    PROCESSING: 'status-processing',
    COMPLETED: 'status-completed',
    FAILED: 'status-failed'
  }[status] || ''
}

const getStatusLabel = (status) => {
  const labels = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    COMPLETED: '已完成',
    FAILED: '失败'
  }
  return labels[status] || status
}
</script>

<style scoped>
/* ========================================
   知识库管理 - 黑暗科技主题
   ======================================== */

.km-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #0a0e1a;
  position: relative;
  overflow-x: hidden;
}

/* -------- 背景 -------- */
.bg-layer { position: absolute; inset: 0; pointer-events: none; z-index: 0; }
.dot-grid {
  position: absolute; inset: 0;
  background-image: radial-gradient(rgba(59, 130, 246, 0.06) 1px, transparent 1px);
  background-size: 40px 40px;
  mask-image: radial-gradient(ellipse 60% 50% at 50% 30%, black 30%, transparent 70%);
}
.scan-lines {
  position: absolute; inset: 0;
  background: repeating-linear-gradient(0deg, transparent, transparent 3px, rgba(0,0,0,0.02) 3px, rgba(0,0,0,0.02) 6px);
}
.bg-orbs { position: absolute; inset: 0; pointer-events: none; z-index: 0; overflow: hidden; }
.orb { position: absolute; border-radius: 50%; filter: blur(120px); }
.orb-1 { width: 400px; height: 400px; background: rgba(5, 150, 105, 0.05); top: -100px; right: -100px; animation: orbFloat 20s ease-in-out infinite; }
.orb-2 { width: 300px; height: 300px; background: rgba(59, 130, 246, 0.05); bottom: 10%; left: -80px; animation: orbFloat 25s ease-in-out infinite reverse; }
@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(20px, -20px) scale(1.1); }
  66% { transform: translate(-15px, 15px) scale(0.95); }
}

/* -------- 头部 -------- */
.km-header {
  padding: 30px 32px 20px;
  position: relative;
  z-index: 2;
}
.back-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  background: rgba(255,255,255,0.04);
  color: #94a3b8;
  font-size: 13px;
  border: 1px solid rgba(255,255,255,0.06);
  transition: all 0.3s;
  margin-bottom: 16px;
}
.back-btn:hover { background: rgba(255,255,255,0.08); color: #e2e8f0; }
.back-arrow { font-size: 16px; }
.km-title {
  font-family: 'Segoe UI', 'PingFang SC', sans-serif;
  font-size: 2rem; font-weight: 700; color: #f1f5f9;
  display: flex; align-items: center; gap: 12px;
  letter-spacing: 1px;
}
.title-icon { font-size: 1.6rem; }
.km-subtitle {
  color: #64748b; font-size: 0.9rem; margin-top: 8px; margin-left: 4px;
  letter-spacing: 1px;
}

/* -------- 上传区域 -------- */
.upload-section {
  max-width: 900px; margin: 0 auto; padding: 0 32px; width: 100%;
  position: relative; z-index: 2;
}
.upload-zone {
  border: 2px dashed rgba(255,255,255,0.1);
  border-radius: 16px;
  padding: 44px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(8px);
}
.upload-zone:hover { border-color: rgba(5, 150, 105, 0.3); background: rgba(5, 150, 105, 0.03); }
.upload-zone.dragging { border-color: #059669; background: rgba(5, 150, 105, 0.06); }
.upload-zone.uploading { pointer-events: none; opacity: 0.7; }
.file-input-hidden { display: none; }
.upload-icon { font-size: 42px; margin-bottom: 12px; }
.upload-text { color: #cbd5e1; font-size: 1rem; margin-bottom: 8px; }
.upload-text .link { color: #34d399; text-decoration: underline; }
.upload-hint { color: #475569; font-size: 0.8rem; }

.upload-result {
  margin-top: 12px; padding: 12px 16px; border-radius: 10px;
  font-size: 0.9rem; display: flex; align-items: center; gap: 8px;
}
.upload-result.success { background: rgba(5, 150, 105, 0.1); color: #6ee7b7; border: 1px solid rgba(5, 150, 105, 0.2); }
.upload-result.error { background: rgba(239, 68, 68, 0.1); color: #fca5a5; border: 1px solid rgba(239, 68, 68, 0.2); }

/* -------- 文档列表 -------- */
.doc-list-section {
  max-width: 1100px; margin: 36px auto 40px; padding: 0 32px; width: 100%;
  position: relative; z-index: 2; flex: 1;
}
.list-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 16px;
}
.list-title {
  font-size: 1.2rem; font-weight: 600; color: #e2e8f0;
  display: flex; align-items: center; gap: 8px;
}
.list-icon { font-size: 1.1rem; }
.list-count {
  font-size: 0.8rem; background: rgba(59, 130, 246, 0.15);
  color: #93c5fd; padding: 2px 10px; border-radius: 12px;
}
.refresh-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 16px; border-radius: 8px;
  background: rgba(255,255,255,0.04); color: #94a3b8;
  font-size: 13px; border: 1px solid rgba(255,255,255,0.06);
  transition: all 0.3s;
}
.refresh-btn:hover:not(:disabled) { background: rgba(255,255,255,0.08); color: #e2e8f0; }
.refresh-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.spinning { animation: spin 1s linear infinite; display: inline-block; }
@keyframes spin { to { transform: rotate(360deg); } }

/* -------- 空状态 / 加载状态 -------- */
.loading-state, .empty-state {
  text-align: center; padding: 60px 20px;
  background: rgba(15, 23, 42, 0.5); border-radius: 16px;
  border: 1px solid rgba(255,255,255,0.04);
}
.loading-spinner {
  width: 36px; height: 36px; margin: 0 auto 16px;
  border: 3px solid rgba(255,255,255,0.08); border-top-color: #3b82f6;
  border-radius: 50%; animation: spin 0.8s linear infinite;
}
.loading-state p { color: #64748b; }
.empty-icon { font-size: 48px; margin-bottom: 16px; }
.empty-text { color: #94a3b8; font-size: 1.05rem; margin-bottom: 8px; font-weight: 600; }
.empty-hint { color: #475569; font-size: 0.85rem; }

/* -------- 表格 -------- */
.doc-table-wrapper {
  background: rgba(15, 23, 42, 0.6); border-radius: 16px;
  border: 1px solid rgba(255,255,255,0.06); overflow: hidden;
  backdrop-filter: blur(8px);
}
.doc-table { width: 100%; border-collapse: collapse; }
.doc-table thead { background: rgba(255,255,255,0.02); }
.doc-table th {
  padding: 14px 16px; text-align: left;
  color: #64748b; font-size: 0.75rem; font-weight: 600;
  text-transform: uppercase; letter-spacing: 1px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.doc-table td {
  padding: 14px 16px; color: #cbd5e1; font-size: 0.9rem;
  border-bottom: 1px solid rgba(255,255,255,0.03);
}
.doc-row { transition: background 0.2s; }
.doc-row:hover { background: rgba(255,255,255,0.02); }
.doc-row:last-child td { border-bottom: none; }

.col-name { min-width: 200px; }
.col-name .file-icon { margin-right: 8px; font-size: 1.1rem; }
.col-name .file-name {
  max-width: 280px; overflow: hidden; text-overflow: ellipsis;
  white-space: nowrap; display: inline-block; vertical-align: middle;
}
.col-type { width: 70px; }
.type-badge {
  font-size: 0.7rem; padding: 3px 8px; border-radius: 4px;
  background: rgba(59, 130, 246, 0.12); color: #93c5fd;
  font-family: 'Cascadia Code', Consolas, monospace;
  letter-spacing: 0.5px;
}
.col-size { width: 80px; color: #94a3b8; font-size: 0.85rem; }
.col-status { width: 130px; }
.col-chunks { width: 70px; text-align: center; }
.col-time { width: 150px; font-size: 0.85rem; color: #94a3b8; }
.col-actions { width: 70px; text-align: center; }

/* 状态徽章 */
.status-badge {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 4px 12px; border-radius: 20px;
  font-size: 0.78rem; font-weight: 500;
}
.status-dot { width: 6px; height: 6px; border-radius: 50%; }
.status-pending { background: rgba(251, 191, 36, 0.1); color: #fbbf24; }
.status-pending .status-dot { background: #fbbf24; }
.status-processing { background: rgba(59, 130, 246, 0.1); color: #93c5fd; }
.status-processing .status-dot { background: #3b82f6; animation: statusPulse 1.5s ease-in-out infinite; }
.status-completed { background: rgba(5, 150, 105, 0.1); color: #6ee7b7; }
.status-completed .status-dot { background: #22c55e; }
.status-failed { background: rgba(239, 68, 68, 0.1); color: #fca5a5; }
.status-failed .status-dot { background: #ef4444; }

@keyframes statusPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.error-tooltip { margin-left: 6px; cursor: help; font-size: 0.85rem; }

/* 操作按钮 */
.action-btn {
  padding: 6px 10px; border-radius: 6px; font-size: 1rem;
  transition: all 0.2s; opacity: 0.6;
}
.action-btn:hover:not(:disabled) { opacity: 1; background: rgba(239, 68, 68, 0.1); }
.action-btn:disabled { opacity: 0.2; cursor: not-allowed; }

.table-footer {
  padding: 12px 16px; color: #475569; font-size: 0.8rem;
  border-top: 1px solid rgba(255,255,255,0.04);
}
.table-footer strong { color: #94a3b8; }
.processing-hint { color: #3b82f6; }

/* -------- 弹窗 -------- */
.modal-overlay {
  position: fixed; inset: 0; z-index: 100;
  background: rgba(0, 0, 0, 0.6); backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
}
.modal-card {
  background: #1e293b; border-radius: 16px; padding: 28px 32px;
  max-width: 420px; width: 90%;
  border: 1px solid rgba(255,255,255,0.08);
  box-shadow: 0 20px 60px rgba(0,0,0,0.5);
}
.modal-title { font-size: 1.15rem; font-weight: 600; color: #f1f5f9; margin-bottom: 16px; }
.modal-body { color: #cbd5e1; font-size: 0.95rem; margin-bottom: 8px; }
.modal-body strong { color: #f1f5f9; }
.modal-hint { color: #ef4444; font-size: 0.8rem; margin-bottom: 24px; }
.modal-actions { display: flex; gap: 12px; justify-content: flex-end; }
.modal-btn {
  padding: 10px 24px; border-radius: 8px; font-size: 0.9rem; font-weight: 500;
  transition: all 0.3s;
}
.modal-btn.cancel { background: rgba(255,255,255,0.06); color: #94a3b8; border: 1px solid rgba(255,255,255,0.08); }
.modal-btn.cancel:hover { background: rgba(255,255,255,0.1); }
.modal-btn.confirm { background: linear-gradient(135deg, #ef4444, #dc2626); color: #fff; }
.modal-btn.confirm:hover { box-shadow: 0 4px 16px rgba(239, 68, 68, 0.3); }

/* -------- 响应式 -------- */
@media (max-width: 768px) {
  .km-header, .upload-section, .doc-list-section { padding-left: 16px; padding-right: 16px; }
  .km-title { font-size: 1.5rem; }
  .doc-table th, .doc-table td { padding: 10px 8px; font-size: 0.8rem; }
  .col-size, .col-chunks, .col-type { display: none; }
  .col-name .file-name { max-width: 140px; }
}
</style>
