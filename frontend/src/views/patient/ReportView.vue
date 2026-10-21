<template>
  <div class="page-wrap">
    <el-card>
      <template #header>报告解读</template>

      <el-upload
        drag
        :auto-upload="false"
        :limit="1"
        accept=".jpg,.jpeg,.png,.pdf"
        :on-change="onFileChange"
        :on-remove="onFileRemove"
        :on-exceed="onExceed"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-text">拖拽化验单到此处，或点击上传</div>
        <template #tip>
          <div class="tip">支持 jpg / jpeg / png / pdf，单个文件 ≤ 10MB。上传即代表同意用于解读分析，可随时删除。</div>
        </template>
      </el-upload>

      <el-button type="primary" class="upload-btn" :loading="uploading" @click="upload" :disabled="!selectedFile">
        上传并解读
      </el-button>
    </el-card>

    <el-card v-if="report" class="result">
      <template #header>
        <div class="result-header">
          <span>解读结果</span>
          <el-button v-if="report.status !== 'PENDING'" link type="danger" @click="onDelete">删除此报告</el-button>
        </div>
      </template>

      <div v-if="report.status === 'PENDING'" class="pending">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在识别报告，请稍候…</span>
      </div>

      <el-alert v-else-if="report.status === 'FAILED'" :title="report.message || '报告识别失败'" type="error" :closable="false" show-icon />

      <template v-else-if="report.status === 'READY'">
        <EmergencyBanner v-if="report.criticalFlag" message="报告中发现危急值，请立即就医或前往急诊。" />

        <div v-if="report.summary" class="summary">
          <div class="label">综合小结</div>
          <p class="text">{{ report.summary }}</p>
        </div>

        <el-table v-if="report.indicators && report.indicators.length" :data="report.indicators" border class="table">
          <el-table-column prop="item" label="指标" width="120" />
          <el-table-column prop="value" label="结果" width="100">
            <template #default="{ row }">{{ row.value }} {{ row.unit }}</template>
          </el-table-column>
          <el-table-column prop="refRange" label="参考范围" width="140" />
          <el-table-column label="异常程度" width="100">
            <template #default="{ row }">
              <el-tag :type="levelType(row.level)" size="small">{{ row.level }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="meaning" label="解读" min-width="160" />
        </el-table>

        <DisclaimerBar v-if="report.disclaimer" :text="report.disclaimer" />
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onUnmounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, UploadFilled } from '@element-plus/icons-vue'
import type { UploadFile } from 'element-plus'
import { deleteReport, getReport, uploadReport } from '@/api/report'
import EmergencyBanner from '@/components/EmergencyBanner.vue'
import DisclaimerBar from '@/components/DisclaimerBar.vue'
import type { ReportDetailVO } from '@/types'

const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const report = ref<ReportDetailVO | null>(null)
let pollTimer: number | undefined

function onFileChange(file: UploadFile) {
  const raw = file.raw
  if (!raw) return
  if (raw.size > 10 * 1024 * 1024) {
    ElMessage.error('文件过大，最大 10MB')
    return
  }
  selectedFile.value = raw
}

function onFileRemove() {
  selectedFile.value = null
}

function onExceed() {
  ElMessage.warning('一次仅支持解读一份报告，请先移除已选文件')
}

async function upload() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  uploading.value = true
  report.value = null
  try {
    const id = await uploadReport(selectedFile.value)
    await poll(id)
  } catch {
    /* 拦截器已提示 */
  } finally {
    uploading.value = false
  }
}

async function poll(id: number) {
  window.clearTimeout(pollTimer)
  const detail = await getReport(id)
  report.value = detail
  if (detail.status === 'PENDING') {
    pollTimer = window.setTimeout(() => poll(id), 2000)
  }
}

async function onDelete() {
  if (!report.value) return
  await ElMessageBox.confirm('删除后不可恢复，确定删除该报告？', '删除报告', { type: 'warning' })
  await deleteReport(report.value.id)
  ElMessage.success('已删除')
  report.value = null
  selectedFile.value = null
}

function levelType(level: string) {
  if (level === '正常') return 'success'
  if (level === '显著') return 'warning'
  if (level === '危急') return 'danger'
  return 'info'
}

onUnmounted(() => window.clearTimeout(pollTimer))
</script>

<style scoped>
.page-wrap {
  max-width: 860px;
  margin: 24px auto;
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.upload-icon {
  font-size: 48px;
  color: #c0c4cc;
}

.upload-text {
  color: #606266;
  margin-top: 8px;
}

.tip {
  color: #909399;
  font-size: 12px;
}

.upload-btn {
  margin-top: 16px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pending {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  padding: 24px 0;
}

.summary {
  margin-bottom: 16px;
}

.label {
  font-weight: 600;
  margin-bottom: 8px;
}

.text {
  color: #606266;
  line-height: 1.8;
  margin: 0;
  white-space: pre-wrap;
}

.table {
  margin-bottom: 8px;
}
</style>
