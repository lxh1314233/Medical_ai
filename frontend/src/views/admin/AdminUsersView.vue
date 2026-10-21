<template>
  <div>
    <el-card>
      <div class="filter-bar">
        <el-input v-model="phone" placeholder="手机号" clearable style="width: 180px" @keyup.enter="search" />
        <el-input v-model="nickname" placeholder="昵称" clearable style="width: 180px" @keyup.enter="search" />
        <el-button type="primary" :icon="Search" @click="search">查询</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="nickname" label="昵称" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registerType" label="注册方式" width="100" />
        <el-table-column prop="createTime" label="注册时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button v-if="auth.canManage" link :type="row.status === 1 ? 'danger' : 'success'" @click="toggle(row)">
              {{ row.status === 1 ? '禁用' : '解禁' }}
            </el-button>
            <span v-else>—</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pager"
        @current-change="load"
        @size-change="search"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { listUsers, updateUserStatus } from '@/api/admin/users'
import { useAuthStore } from '@/stores/auth'
import type { UserVO } from '@/types'

const auth = useAuthStore()

const list = ref<UserVO[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const phone = ref('')
const nickname = ref('')

function formatTime(t: string) {
  return dayjs(t).format('YYYY-MM-DD HH:mm')
}

async function load() {
  loading.value = true
  try {
    const res = await listUsers({
      page: page.value,
      size: size.value,
      phone: phone.value || undefined,
      nickname: nickname.value || undefined,
    })
    list.value = res.records
    total.value = res.total
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

async function toggle(row: UserVO) {
  const next = row.status === 1 ? 0 : 1
  const label = next === 0 ? '禁用' : '解禁'
  await ElMessageBox.confirm(`确定${label}用户「${row.phone}」？`, label, { type: 'warning' })
  await updateUserStatus(row.id, { status: next })
  ElMessage.success(`已${label}`)
  load()
}

onMounted(load)
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
