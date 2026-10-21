<template>
  <div class="patient-layout">
    <header class="header">
      <div class="header-inner">
        <div class="logo" @click="router.push('/')">🏥 医疗 AI 问答</div>
        <nav v-if="auth.isLoggedIn && !auth.isAdmin" class="nav">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-link"
            active-class="active"
          >
            {{ item.label }}
          </router-link>
        </nav>
        <div class="right">
          <template v-if="auth.isLoggedIn && !auth.isAdmin">
            <el-dropdown @command="onCommand">
              <span class="user">
                <el-icon><User /></el-icon>
                {{ auth.phone }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button text @click="router.push('/login')">登录</el-button>
            <el-button type="primary" @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ArrowDown, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import * as authApi from '@/api/auth'

const router = useRouter()
const auth = useAuthStore()

const navItems = [
  { path: '/chat', label: '智能问答' },
  { path: '/triage', label: '症状分诊' },
  { path: '/medications', label: '用药咨询' },
  { path: '/reports', label: '报告解读' },
  { path: '/profile', label: '健康档案' },
  { path: '/knowledge', label: '个人知识库' },
  { path: '/feedback', label: '我的反馈' },
]

async function onCommand(cmd: string) {
  if (cmd === 'logout') {
    try {
      await authApi.logout()
    } catch {
      /* token 可能已失效，忽略 */
    }
    auth.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.patient-layout {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 0 20px;
}

.logo {
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  color: #409eff;
}

.nav {
  display: flex;
  gap: 4px;
  flex: 1;
  overflow-x: auto;
}

.nav-link {
  padding: 6px 12px;
  color: #606266;
  border-radius: 6px;
  white-space: nowrap;
  font-size: 14px;
}

.nav-link:hover {
  color: #409eff;
}

.nav-link.active {
  color: #409eff;
  background: #ecf5ff;
}

.right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #303133;
  outline: none;
}

.main {
  flex: 1;
  width: 100%;
}
</style>
