import axios from 'axios'
import { defineStore } from 'pinia'
import { API_BASE_URL } from '@/utils/env'
import type { AdminLoginResp, Result, Role, TokenResp } from '@/types'

interface AuthState {
  accessToken: string
  refreshToken: string
  userId: number | null
  phone: string
  role: Role | null
}

const STORAGE_KEY = 'medical_ai_auth'

function loadState(): AuthState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return { ...defaultState(), ...JSON.parse(raw) }
  } catch {
    /* ignore */
  }
  return defaultState()
}

function defaultState(): AuthState {
  return { accessToken: '', refreshToken: '', userId: null, phone: '', role: null }
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => loadState(),

  getters: {
    isLoggedIn: (s) => !!s.accessToken,
    isAdmin: (s) => s.role === 'SUPER_ADMIN' || s.role === 'OPERATOR' || s.role === 'AUDITOR',
    isSuperAdmin: (s) => s.role === 'SUPER_ADMIN',
    /** 知识库 / 用户 / 审核处理 权限（SUPER_ADMIN + OPERATOR） */
    canManage: (s) => s.role === 'SUPER_ADMIN' || s.role === 'OPERATOR',
  },

  actions: {
    persist() {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          accessToken: this.accessToken,
          refreshToken: this.refreshToken,
          userId: this.userId,
          phone: this.phone,
          role: this.role,
        }),
      )
    },

    /** 患者端登录/注册/刷新成功后写入 */
    setTokens(resp: TokenResp) {
      this.accessToken = resp.accessToken
      this.refreshToken = resp.refreshToken
      this.userId = resp.userId
      this.phone = resp.phone
      this.role = 'USER'
      this.persist()
    },

    /** 管理员登录后写入 */
    setAdmin(resp: AdminLoginResp) {
      this.accessToken = resp.accessToken
      this.refreshToken = ''
      this.userId = null
      this.phone = ''
      this.role = resp.role
      this.persist()
    },

    /** 刷新患者 accessToken（用裸 axios，避免触发 request 拦截器递归） */
    async refresh(): Promise<TokenResp> {
      if (!this.refreshToken) throw new Error('无 refreshToken')
      const { data } = await axios.post<Result<TokenResp>>(`${API_BASE_URL}/auth/refresh`, {
        refreshToken: this.refreshToken,
      })
      if (data.code !== 0) throw new Error(data.message || '刷新失败')
      this.setTokens(data.data)
      return data.data
    },

    logout() {
      Object.assign(this, defaultState())
      localStorage.removeItem(STORAGE_KEY)
    },
  },
})
