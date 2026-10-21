<template>
  <div class="page-wrap">
    <el-card>
      <template #header>
        <div class="header">
          <span>个人知识库</span>
          <el-button :icon="Refresh" @click="load" :loading="loading">刷新</el-button>
        </div>
      </template>

      <p class="hint">上传个人健康文档，作为智能问答的参考素材（txt / md / pdf / docx，≤10MB）。</p>

      <el-upload
        :auto-upload="false"
        :limit="1"
        accept=".txt,.md,.pdf,.docx"
        :on-change="onFileChange"
        :on-exceed="onExceed"
      >
        <el-button :icon="Upload" type="primary">选择文档</el-button>
        <template #tip>
          <div class="tip">选择后将立即上传并解析入库</div>
        </template>
      </el-upload>

      <el-table :data="list" v-loading="loading" border class="table">
        <el-table-column prop="fileName" label="文件名" min-width="200" />
        <el-table-column prop="fileType" label="类型" width="80" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chunkCount" label="分块数" width="80" />
        <el-table-column prop="errorMsg" label="错误信息" min-width="140">
          <template #default="{ row }">{{ row.errorMsg || '—' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button link type="danger" @click="onDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Upload } from '@element-plus/icons-vue'
import type { UploadFile } from 'element-plus'
import { deleteDocument, listDocuments, uploadDocument } from '@/api/knowledge'
import type { DocumentVO } from '@/types'

const list = ref<DocumentVO[]>([])
const loading = ref(false)

function statusName(s: string) {
  if (s === 'PROCESSING') return '处理中'
  if (s === 'READY') return '就绪'
  return '失败'
}

function statusType(s: string) {
  if (s === 'READY') return 'success'
  if (s === 'PROCESSING') return 'warning'
  return 'danger'
}

function formatTime(t: string) {
  return dayjs(t).format('YYYY-MM-DD HH:mm')
}

async function onFileChange(file: UploadFile) {
  const raw = file.raw
  if (!raw) return
  if (raw.size > 10 * 1024 * 1024) {
    ElMessage.error('文件过大，最大 10MB')
    return
  }
  try {
    await uploadDocument(raw)
    ElMessage.success('已上传，正在解析入库')
    await load()
  } catch {
    /* 拦截器已提示 */
  }
}

function onExceed() {
  ElMessage.warning('一次仅上传一份文档')
}

async function onDelete(id: number) {
  await ElMessageBox.confirm('确定删除该文档？', '删除', { type: 'warning' })
  await deleteDocument(id)
  ElMessage.success('已删除')
  load()
}

async function load() {
  loading.value = true
  try {
    list.value = await listDocuments()
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page-wrap {
  max-width: 960px;
  margin: 24px auto;
  padding: 0 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hint {
  color: #909399;
  font-size: 13px;
  margin-bottom: 12px;
}

.tip {
  color: #909399;
  font-size: 12px;
}

.table {
  margin-top: 16px;
}
</style>
