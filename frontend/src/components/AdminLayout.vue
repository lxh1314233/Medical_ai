<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <div class="brand">医疗 AI · 管理端</div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="title">{{ route.meta.title }}</span>
        <div class="right">
          <span class="role">{{ roleName[auth.role as string] || auth.role }}</span>
          <span class="user">{{ auth.phone || '管理员' }}</span>
          <el-button text @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataAnalysis, Document, Lock, Postcard, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const roleName: Record<string, string> = {
  SUPER_ADMIN: '超级管理员',
  OPERATOR: '运营管理员',
  AUDITOR: '审核员',
}

const allMenu = [
  { path: '/admin/dashboard', label: '数据看板', icon: DataAnalysis, roles: ['SUPER_ADMIN', 'OPERATOR', 'AUDITOR'] },
  { path: '/admin/knowledge', label: '知识库管理', icon: Document, roles: ['SUPER_ADMIN', 'OPERATOR'] },
  { path: '/admin/review', label: '内容审核', icon: Postcard, roles: ['SUPER_ADMIN', 'OPERATOR', 'AUDITOR'] },
  { path: '/admin/users', label: '用户管理', icon: User, roles: ['SUPER_ADMIN', 'OPERATOR'] },
  { path: '/admin/admins', label: '管理员账号', icon: Lock, roles: ['SUPER_ADMIN'] },
]

const menuItems = computed(() => allMenu.filter((m) => auth.role && m.roles.includes(auth.role)))

function logout() {
  auth.logout()
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100%;
}

.aside {
  background: #001529;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.brand {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.menu {
  border-right: none;
  background: transparent;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: rgba(255, 255, 255, 0.75);
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.08);
  --el-menu-active-color: #fff;
}

.header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  gap: 16px;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.role {
  font-size: 13px;
  color: #909399;
}

.user {
  font-size: 14px;
}

.main {
  background: #f5f7fa;
}
</style>
