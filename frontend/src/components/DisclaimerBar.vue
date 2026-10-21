<template>
  <div class="disclaimer-bar">
    <el-icon><Warning /></el-icon>
    <span>{{ text || DEFAULT }}</span>
  </div>
</template>

<script setup lang="ts">
import { Warning } from '@element-plus/icons-vue'

const DEFAULT = '内容仅供参考，不构成诊疗建议，不能替代专业医生的面诊与诊断。'

defineProps<{ text?: string }>()
</script>

<style scoped>
.disclaimer-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  padding: 10px 14px;
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  border-radius: 6px;
  line-height: 1.5;
}
</style>
