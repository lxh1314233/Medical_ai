<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2 class="title">注册</h2>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-row">
            <el-input v-model="form.code" placeholder="6 位验证码" maxlength="6" />
            <el-button :disabled="countdown > 0" @click="sendSms(form.phone)">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="6-20 位密码" />
        </el-form-item>
        <el-form-item label="昵称（选填）" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="20" />
        </el-form-item>
        <el-form-item prop="agreed">
          <el-checkbox v-model="form.agreed">
            我已阅读并同意《用户协议》与《隐私政策》
          </el-checkbox>
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">注册</el-button>
      </el-form>

      <div class="footer">
        <router-link to="/login">已有账号？去登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { register } from '@/api/auth'
import { useSmsCode } from '@/composables/useSmsCode'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const { countdown, send: sendSms } = useSmsCode('REGISTER')

const loading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  phone: '',
  code: '',
  password: '',
  nickname: '',
  agreed: false,
})

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' },
  ],
  agreed: [
    {
      validator: (_rule, value, callback) => {
        if (!value) callback(new Error('请先阅读并同意协议'))
        else callback()
      },
      trigger: 'change',
    },
  ],
}

async function onSubmit() {
  if (!(await formRef.value?.validate())) return
  loading.value = true
  try {
    const resp = await register({
      phone: form.phone,
      code: form.code,
      password: form.password,
      nickname: form.nickname || undefined,
    })
    auth.setTokens(resp)
    router.push('/chat')
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
  background: linear-gradient(135deg, #ecf5ff 0%, #f5f7fa 100%);
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

.code-row {
  display: flex;
  gap: 8px;
  width: 100%;
}

.code-row .el-button {
  white-space: nowrap;
}

.footer {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
}
</style>
