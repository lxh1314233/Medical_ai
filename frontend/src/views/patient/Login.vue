<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2 class="title">登录</h2>

      <el-tabs v-model="tab">
        <el-tab-pane label="密码登录" name="password">
          <el-form :model="form" :rules="passwordRules" ref="passwordFormRef" label-position="top">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-button type="primary" class="submit" :loading="loading" @click="onPasswordLogin">登录</el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="短信登录" name="sms">
          <el-form :model="form" :rules="smsRules" ref="smsFormRef" label-position="top">
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
            <el-button type="primary" class="submit" :loading="loading" @click="onSmsLogin">登录</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="footer">
        <router-link to="/register">没有账号？去注册</router-link>
        <router-link to="/forgot-password">忘记密码？</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { login } from '@/api/auth'
import { useSmsCode } from '@/composables/useSmsCode'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { countdown, send: sendSms } = useSmsCode('LOGIN')

const tab = ref('password')
const loading = ref(false)
const passwordFormRef = ref<FormInstance>()
const smsFormRef = ref<FormInstance>()

const form = reactive({ phone: '', password: '', code: '' })

const phoneRule = { required: true, message: '请输入手机号', trigger: 'blur' }
const passwordRules: FormRules = {
  phone: [phoneRule, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}
const smsRules: FormRules = {
  phone: [phoneRule, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

async function onPasswordLogin() {
  if (!(await passwordFormRef.value?.validate())) return
  loading.value = true
  try {
    const resp = await login({ phone: form.phone, type: 'PASSWORD', password: form.password })
    auth.setTokens(resp)
    const redirect = route.query.redirect as string | undefined
    router.push(redirect || '/chat')
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

async function onSmsLogin() {
  if (!(await smsFormRef.value?.validate())) return
  loading.value = true
  try {
    const resp = await login({ phone: form.phone, type: 'SMS', code: form.code })
    auth.setTokens(resp)
    const redirect = route.query.redirect as string | undefined
    router.push(redirect || '/chat')
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
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  font-size: 13px;
}
</style>
