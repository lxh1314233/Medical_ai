<template>
  <div class="page-wrap">
    <el-card>
      <template #header>
        <div class="header">
          <span>个人健康档案</span>
          <el-button link type="danger" @click="onDelete">删除档案</el-button>
        </div>
      </template>

      <el-alert
        title="隐私说明：健康档案用于为您提供更个性化的分诊与用药提示，加密存储，仅本人可见，可随时删除。"
        type="info"
        :closable="false"
        show-icon
        class="privacy"
      />

      <el-form :model="form" label-position="top">
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
            <el-radio value="其他">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 220px" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="身高（cm）">
              <el-input v-model="form.height" placeholder="如 170" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重（kg）">
              <el-input v-model="form.weight" placeholder="如 65" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="过敏史">
          <el-input v-model="form.allergyHistory" type="textarea" :rows="2" placeholder="如：青霉素过敏" />
        </el-form-item>
        <el-form-item label="既往病史">
          <el-input v-model="form.pastHistory" type="textarea" :rows="2" placeholder="如：高血压、糖尿病" />
        </el-form-item>
        <el-form-item label="长期用药">
          <el-input v-model="form.medicationHistory" type="textarea" :rows="2" placeholder="如：阿司匹林" />
        </el-form-item>
        <el-form-item label="家族史">
          <el-input v-model="form.familyHistory" type="textarea" :rows="2" placeholder="如：父母均有高血压" />
        </el-form-item>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteProfile, getProfile, updateProfile } from '@/api/profile'
import type { Profile } from '@/types'

const saving = ref(false)
const form = reactive<Profile>({})

async function load() {
  try {
    const p = await getProfile()
    Object.assign(form, p)
  } catch {
    /* 拦截器已提示 */
  }
}

async function onSave() {
  saving.value = true
  try {
    await updateProfile({ ...form })
    ElMessage.success('档案已保存')
  } catch {
    /* 拦截器已提示 */
  } finally {
    saving.value = false
  }
}

async function onDelete() {
  await ElMessageBox.confirm('删除后健康档案将被清空，确定删除？', '删除档案', { type: 'warning' })
  await deleteProfile()
  Object.assign(form, {})
  ElMessage.success('档案已删除')
}

onMounted(load)
</script>

<style scoped>
.page-wrap {
  max-width: 640px;
  margin: 24px auto;
  padding: 0 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.privacy {
  margin-bottom: 20px;
}
</style>
