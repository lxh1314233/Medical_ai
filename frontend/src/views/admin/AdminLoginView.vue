<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2 class="title">管理端登录</h2>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" @keyup.enter="onSubmit" />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">登录</el-button>
      </el-form>

      <div class="footer">
        <router-link to="/">返回患者端</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { adminLogin } from '@/api/admin/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const loading = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({ username: '', password: '' })

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function onSubmit() {
  if (!(await formRef.value?.validate())) return
  loading.value = true
  try {
    const resp = await adminLogin({ username: form.username, password: form.password })
    auth.setAdmin(resp)
    router.push('/admin/dashboard')
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #001529;
  padding: 20px;
}

.auth-card {
  width: 400px;
  max-width: 100%;
}

.title {
  text-align: center;
  margin: 0 0 20px;
}

.submit {
  width: 100%;
  margin-top: 8px;
}

.footer {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
}
</style>
