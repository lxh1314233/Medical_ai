<template>
  <div>
    <el-card>
      <div class="filter-bar">
        <el-button type="success" :icon="Plus" @click="openCreate">新建管理员</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="160" />
        <el-table-column label="角色" width="140">
          <template #default="{ row }">
            <el-tag :type="roleType(row.role)" size="small">{{ roleName(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新建管理员" width="480px">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名 *" prop="username">
          <el-input v-model="form.username" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="密码 *" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="角色 *" prop="role">
          <el-select v-model="form.role" placeholder="选择角色" style="width: 100%">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="运营" value="OPERATOR" />
            <el-option label="审核员" value="AUDITOR" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { createAdmin, listAdmins } from '@/api/admin/admins'
import type { AdminRole, AdminVO } from '@/types'

const list = ref<AdminVO[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({ username: '', password: '', role: 'OPERATOR' as AdminRole })

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

function roleName(role: AdminRole) {
  if (role === 'SUPER_ADMIN') return '超级管理员'
  if (role === 'OPERATOR') return '运营'
  return '审核员'
}

function roleType(role: AdminRole) {
  if (role === 'SUPER_ADMIN') return 'danger'
  if (role === 'OPERATOR') return 'primary'
  return 'warning'
}

function formatTime(t: string) {
  return dayjs(t).format('YYYY-MM-DD HH:mm')
}

async function load() {
  loading.value = true
  try {
    list.value = await listAdmins()
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { username: '', password: '', role: 'OPERATOR' })
  dialogVisible.value = true
}

async function save() {
  if (!(await formRef.value?.validate())) return
  saving.value = true
  try {
    await createAdmin({ ...form })
    ElMessage.success('已创建')
    dialogVisible.value = false
    load()
  } catch {
    /* 拦截器已提示 */
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.filter-bar {
  margin-bottom: 16px;
}
</style>
