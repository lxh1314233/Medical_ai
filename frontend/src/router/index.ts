import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import type { Role } from '@/types'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    patientOnly?: boolean
    adminOnly?: boolean
    roles?: Role[]
    title?: string
  }
}

const routes: RouteRecordRaw[] = [
  // 认证页（无布局）
  { path: '/login', name: 'login', component: () => import('@/views/patient/Login.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'register', component: () => import('@/views/patient/Register.vue'), meta: { title: '注册' } },
  {
    path: '/forgot-password',
    name: 'forgot',
    component: () => import('@/views/patient/ForgotPassword.vue'),
    meta: { title: '找回密码' },
  },

  // 患者端（顶部导航布局）
  {
    path: '/',
    component: () => import('@/components/PatientLayout.vue'),
    children: [
      { path: '', name: 'landing', component: () => import('@/views/patient/Landing.vue'), meta: { title: '首页' } },
      {
        path: 'chat',
        name: 'chat',
        component: () => import('@/views/patient/ChatView.vue'),
        meta: { requiresAuth: true, patientOnly: true, title: '智能问答' },
      },
      {
        path: 'triage',
        name: 'triage',
        component: () => import('@/views/patient/TriageView.vue'),
        meta: { requiresAuth: true, patientOnly: true, title: '症状分诊' },
      },
      {
        path: 'medications',
        name: 'medications',
        component: () => import('@/views/patient/MedicationView.vue'),
        meta: { requiresAuth: true, patientOnly: true, title: '用药咨询' },
      },
      {
        path: 'reports',
        name: 'reports',
        component: () => import('@/views/patient/ReportView.vue'),
        meta: { requiresAuth: true, patientOnly: true, title: '报告解读' },
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/patient/ProfileView.vue'),
        meta: { requiresAuth: true, patientOnly: true, title: '健康档案' },
      },
      {
        path: 'feedback',
        name: 'feedback',
        component: () => import('@/views/patient/FeedbackView.vue'),
        meta: { requiresAuth: true, patientOnly: true, title: '我的反馈' },
      },
      {
        path: 'knowledge',
        name: 'knowledge',
        component: () => import('@/views/patient/KnowledgeView.vue'),
        meta: { requiresAuth: true, patientOnly: true, title: '个人知识库' },
      },
    ],
  },

  // 管理端
  {
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('@/views/admin/AdminLoginView.vue'),
    meta: { title: '管理端登录' },
  },
  {
    path: '/admin',
    component: () => import('@/components/AdminLayout.vue'),
    children: [
      { path: '', redirect: { name: 'admin-dashboard' } },
      {
        path: 'dashboard',
        name: 'admin-dashboard',
        component: () => import('@/views/admin/AdminDashboardView.vue'),
        meta: { requiresAuth: true, adminOnly: true, roles: ['SUPER_ADMIN', 'OPERATOR', 'AUDITOR'], title: '数据看板' },
      },
      {
        path: 'knowledge',
        name: 'admin-knowledge',
        component: () => import('@/views/admin/AdminKnowledgeView.vue'),
        meta: { requiresAuth: true, adminOnly: true, roles: ['SUPER_ADMIN', 'OPERATOR'], title: '知识库管理' },
      },
      {
        path: 'review',
        name: 'admin-review',
        component: () => import('@/views/admin/AdminReviewView.vue'),
        meta: { requiresAuth: true, adminOnly: true, roles: ['SUPER_ADMIN', 'OPERATOR', 'AUDITOR'], title: '内容审核' },
      },
      {
        path: 'users',
        name: 'admin-users',
        component: () => import('@/views/admin/AdminUsersView.vue'),
        meta: { requiresAuth: true, adminOnly: true, roles: ['SUPER_ADMIN', 'OPERATOR'], title: '用户管理' },
      },
      {
        path: 'admins',
        name: 'admin-admins',
        component: () => import('@/views/admin/AdminAdminsView.vue'),
        meta: { requiresAuth: true, adminOnly: true, roles: ['SUPER_ADMIN'], title: '管理员账号' },
      },
    ],
  },

  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFound.vue'), meta: { title: '404' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (!to.meta.requiresAuth) {
    // 已登录用户访问认证页 → 跳回各自首页
    if (auth.isLoggedIn && ['login', 'register', 'forgot', 'admin-login'].includes(to.name as string)) {
      return auth.isAdmin ? '/admin/dashboard' : '/chat'
    }
    return true
  }

  // 需要登录
  if (!auth.isLoggedIn) {
    const login = to.meta.adminOnly ? '/admin/login' : '/login'
    return { path: login, query: { redirect: to.fullPath } }
  }

  // 分区隔离：管理员 ↔ 患者
  if (to.meta.adminOnly && !auth.isAdmin) return '/chat'
  if (to.meta.patientOnly && auth.isAdmin) return '/admin/dashboard'

  // 角色细化（如 /admin/admins 仅 SUPER_ADMIN）
  if (to.meta.roles && auth.role && !to.meta.roles.includes(auth.role)) {
    ElMessage.warning('无权限访问')
    return '/admin/dashboard'
  }

  return true
})

router.afterEach((to) => {
  if (to.meta.title) document.title = `${to.meta.title} · 医疗 AI 问答`
})

export default router
