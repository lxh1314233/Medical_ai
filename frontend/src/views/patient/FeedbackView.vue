<template>
  <div class="page-wrap">
    <el-card>
      <template #header>我的反馈</template>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" size="small">{{ typeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="messageId" label="关联回答" width="90" />
        <el-table-column prop="content" label="内容" min-width="200">
          <template #default="{ row }">{{ row.content || '—' }}</template>
        </el-table-column>
        <el-table-column label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PROCESSED' ? 'success' : 'warning'" size="small">
              {{ row.status === 'PROCESSED' ? '已处理' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { listMyFeedback } from '@/api/feedback'
import type { FeedbackVO } from '@/types'

const list = ref<FeedbackVO[]>([])
const loading = ref(false)

function typeName(type: string) {
  if (type === 'HELPFUL') return '有帮助'
  if (type === 'NOT_HELPFUL') return '无帮助'
  return '内容纠错'
}

function typeTag(type: string) {
  if (type === 'HELPFUL') return 'success'
  if (type === 'NOT_HELPFUL') return 'info'
  return 'warning'
}

function formatTime(t: string) {
  return dayjs(t).format('YYYY-MM-DD HH:mm')
}

async function load() {
  loading.value = true
  try {
    list.value = await listMyFeedback()
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
  max-width: 900px;
  margin: 24px auto;
  padding: 0 16px;
}
</style>
