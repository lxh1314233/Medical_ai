<template>
  <div>
    <el-card>
      <el-tabs v-model="tab">
        <el-tab-pane label="纠错反馈" name="feedback">
          <el-table :data="feedbackList" v-loading="feedbackLoading" border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="userId" label="用户ID" width="90" />
            <el-table-column prop="messageId" label="回答ID" width="90" />
            <el-table-column label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.type === 'HELPFUL' ? 'success' : row.type === 'NOT_HELPFUL' ? 'info' : 'warning'" size="small">
                  {{ typeName(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="纠错说明" min-width="200" show-overflow-tooltip />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'PROCESSED' ? 'success' : 'warning'" size="small">
                  {{ row.status === 'PROCESSED' ? '已处理' : '待处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="提交时间" width="170">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 'PENDING' && auth.canManage" link type="primary" @click="process(row)">处理</el-button>
                <span v-else-if="row.status === 'PROCESSED'">—</span>
                <span v-else>无权限</span>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="feedbackPage"
            v-model:page-size="feedbackSize"
            :total="feedbackTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            class="pager"
            @current-change="loadFeedback"
            @size-change="loadFeedback"
          />
        </el-tab-pane>

        <el-tab-pane label="操作留痕" name="audit">
          <el-table :data="auditList" v-loading="auditLoading" border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="operatorId" label="操作者ID" width="100" />
            <el-table-column prop="targetType" label="对象类型" width="110" />
            <el-table-column prop="targetId" label="对象ID" width="90" />
            <el-table-column prop="action" label="动作" width="120" />
            <el-table-column prop="detail" label="详情" min-width="220" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" width="170">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="auditPage"
            v-model:page-size="auditSize"
            :total="auditTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            class="pager"
            @current-change="loadAudit"
            @size-change="loadAudit"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { listAuditLogs, listFeedbackReviews, processFeedback } from '@/api/admin/review'
import { useAuthStore } from '@/stores/auth'
import type { AuditLogVO, FeedbackReviewVO } from '@/types'

const auth = useAuthStore()
const tab = ref('feedback')

const feedbackList = ref<FeedbackReviewVO[]>([])
const feedbackLoading = ref(false)
const feedbackPage = ref(1)
const feedbackSize = ref(20)
const feedbackTotal = ref(0)

const auditList = ref<AuditLogVO[]>([])
const auditLoading = ref(false)
const auditPage = ref(1)
const auditSize = ref(20)
const auditTotal = ref(0)

function typeName(t: string) {
  if (t === 'HELPFUL') return '有帮助'
  if (t === 'NOT_HELPFUL') return '无帮助'
  return '内容纠错'
}

function formatTime(t: string) {
  return dayjs(t).format('YYYY-MM-DD HH:mm')
}

async function loadFeedback() {
  feedbackLoading.value = true
  try {
    const res = await listFeedbackReviews({ page: feedbackPage.value, size: feedbackSize.value })
    feedbackList.value = res.records
    feedbackTotal.value = res.total
  } catch {
    /* 拦截器已提示 */
  } finally {
    feedbackLoading.value = false
  }
}

async function loadAudit() {
  auditLoading.value = true
  try {
    const res = await listAuditLogs({ page: auditPage.value, size: auditSize.value })
    auditList.value = res.records
    auditTotal.value = res.total
  } catch {
    /* 拦截器已提示 */
  } finally {
    auditLoading.value = false
  }
}

async function process(row: FeedbackReviewVO) {
  await processFeedback(row.id)
  ElMessage.success('已标记为已处理')
  loadFeedback()
}

onMounted(() => {
  loadFeedback()
  loadAudit()
})
</script>

<style scoped>
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
