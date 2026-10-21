<template>
  <div class="emergency-banner">
    <el-icon class="icon"><WarningFilled /></el-icon>
    <div class="content">
      <div class="title">紧急情况提示</div>
      <div class="text">
        {{ message || '请立即拨打 120 或前往最近医院急诊科就诊，本服务不处理紧急情况。' }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { WarningFilled } from '@element-plus/icons-vue'

defineProps<{ message?: string }>()
</script>

<style scoped>
.emergency-banner {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fef0f0;
  border: 1px solid #fbc4c4;
  border-radius: 8px;
  margin-bottom: 16px;
}

.icon {
  font-size: 24px;
  color: #f56c6c;
  flex-shrink: 0;
}

.title {
  font-weight: 600;
  color: #f56c6c;
  margin-bottom: 4px;
}

.text {
  color: #c45656;
  line-height: 1.6;
  font-size: 14px;
}
</style>
