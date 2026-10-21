<template>
  <div class="dashboard">
    <el-row :gutter="16" v-loading="loading">
      <el-col v-for="card in cards" :key="card.label" :xs="12" :sm="8" :md="4">
        <el-card class="stat-card">
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card">
      <template #header>高频问题 Top {{ topCount }}</template>
      <div v-if="topCount === 0" class="empty">
        <el-empty description="暂无问答数据" />
      </div>
      <div v-else ref="chartRef" class="chart"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getStatsOverview, getTopQuestions } from '@/api/admin/stats'
import type { StatsOverviewVO, TopQuestionVO } from '@/types'

const loading = ref(false)
const overview = ref<StatsOverviewVO | null>(null)
const topQuestions = ref<TopQuestionVO[]>([])
const chartRef = ref<HTMLDivElement>()

let chart: echarts.ECharts | null = null

const topCount = computed(() => topQuestions.value.length)

const cards = computed(() => {
  const o = overview.value
  const rate = o && o.satisfactionRate != null ? `${o.satisfactionRate.toFixed(2)}%` : '—'
  return [
    { label: '注册用户', value: o ? o.userCount : '-' },
    { label: '问答提问数', value: o ? o.chatQuestionCount : '-' },
    { label: '分诊次数', value: o ? o.triageCount : '-' },
    { label: '报告解读数', value: o ? o.reportCount : '-' },
    { label: '反馈总数', value: o ? o.feedbackCount : '-' },
    { label: '满意度', value: rate },
  ]
})

async function load() {
  loading.value = true
  try {
    overview.value = await getStatsOverview()
    topQuestions.value = await getTopQuestions(10)
    renderChart()
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

function truncate(s: string) {
  return s.length > 18 ? `${s.slice(0, 18)}…` : s
}

function renderChart() {
  if (!chartRef.value || topQuestions.value.length === 0) return
  if (!chart) chart = echarts.init(chartRef.value)
  const items = [...topQuestions.value].reverse()
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 8, right: 32, top: 8, bottom: 8, containLabel: true },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: { type: 'category', data: items.map((q) => truncate(q.content)), inverse: true },
    series: [
      {
        type: 'bar',
        data: items.map((q) => q.count),
        barMaxWidth: 18,
        itemStyle: { color: '#409eff', borderRadius: [0, 4, 4, 0] },
      },
    ],
  })
}

function onResize() {
  chart?.resize()
}

onMounted(() => {
  load()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.stat-card {
  text-align: center;
  margin-bottom: 16px;
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
}

.chart-card {
  margin-top: 8px;
}

.chart {
  height: 360px;
}

.empty {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
