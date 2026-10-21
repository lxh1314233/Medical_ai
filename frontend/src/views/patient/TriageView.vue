<template>
  <div class="page-wrap">
    <el-card>
      <template #header>症状分诊</template>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="主要症状 *" prop="chiefComplaint">
          <el-input v-model="form.chiefComplaint" type="textarea" :rows="3" placeholder="请描述主要症状，如：头痛、发热、腹痛等" />
        </el-form-item>
        <el-form-item label="持续时间">
          <el-input v-model="form.duration" placeholder="如：3 天、1 周" />
        </el-form-item>
        <el-form-item label="严重程度">
          <el-select v-model="form.severity" placeholder="请选择" clearable style="width: 100%">
            <el-option label="轻微" value="轻微" />
            <el-option label="中等" value="中等" />
            <el-option label="严重" value="严重" />
          </el-select>
        </el-form-item>
        <el-form-item label="伴随症状">
          <el-input v-model="form.accompanyingSymptoms" type="textarea" :rows="2" placeholder="如：恶心、呕吐、乏力等" />
        </el-form-item>
        <el-form-item label="既往病史">
          <el-input v-model="form.pastHistory" type="textarea" :rows="2" placeholder="如：高血压、糖尿病等" />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="onSubmit">开始分诊</el-button>
      </el-form>
    </el-card>

    <el-card v-if="result" class="result">
      <template #header>分诊结果</template>

      <EmergencyBanner v-if="result.redFlag" :message="result.redFlagMessage" />

      <template v-else>
        <div class="section">
          <div class="label">推荐科室</div>
          <el-tag v-for="d in result.departments" :key="d" size="large" class="dept" type="primary">{{ d }}</el-tag>
        </div>

        <div class="section">
          <div class="label">紧急程度</div>
          <el-tag :type="urgencyType" size="large">{{ result.urgencyLevel }}</el-tag>
        </div>

        <div v-if="result.reasons && result.reasons.length" class="section">
          <div class="label">推荐理由</div>
          <ul class="list">
            <li v-for="(r, i) in result.reasons" :key="i">{{ r }}</li>
          </ul>
        </div>

        <div v-if="result.observationNotes" class="section">
          <div class="label">观察要点</div>
          <p class="text">{{ result.observationNotes }}</p>
        </div>
      </template>

      <DisclaimerBar v-if="result.disclaimer" :text="result.disclaimer" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { triage } from '@/api/triage'
import EmergencyBanner from '@/components/EmergencyBanner.vue'
import DisclaimerBar from '@/components/DisclaimerBar.vue'
import type { TriageVO } from '@/types'

const formRef = ref<FormInstance>()
const loading = ref(false)
const result = ref<TriageVO | null>(null)

const form = reactive({
  chiefComplaint: '',
  duration: '',
  severity: '',
  accompanyingSymptoms: '',
  pastHistory: '',
})

const rules: FormRules = {
  chiefComplaint: [{ required: true, message: '请描述主要症状', trigger: 'blur' }],
}

const urgencyType = computed(() => {
  if (!result.value) return 'info'
  if (result.value.urgencyLevel === '立即就医') return 'danger'
  if (result.value.urgencyLevel === '尽快就医') return 'warning'
  return 'success'
})

async function onSubmit() {
  if (!(await formRef.value?.validate())) return
  loading.value = true
  try {
    result.value = await triage({ ...form })
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page-wrap {
  max-width: 720px;
  margin: 24px auto;
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result {
  margin-top: 8px;
}

.section {
  margin-bottom: 16px;
}

.label {
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  font-size: 14px;
}

.dept {
  margin-right: 8px;
}

.list {
  margin: 0;
  padding-left: 18px;
  color: #606266;
  line-height: 1.8;
}

.text {
  color: #606266;
  line-height: 1.8;
  margin: 0;
  white-space: pre-wrap;
}
</style>
