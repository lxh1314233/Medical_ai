import axios, {
  type AxiosError,
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios'
import { ElMessage } from 'element-plus'
import { API_BASE_URL } from './env'
import { useAuthStore } from '@/stores/auth'
import type { Result } from '@/types'

type RetriableConfig = InternalAxiosRequestConfig & { _retried?: boolean }

const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
})

// 请求拦截：挂 Bearer token
http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.set('Authorization', `Bearer ${auth.accessToken}`)
  }
  return config
})

// 响应拦截：解包 Result；业务错误 toast；401 刷新重放；403 提示
http.interceptors.response.use(
  (response: AxiosResponse<Result<unknown>>) => {
    const body = response.data
    // 非 Result 结构（如文件流 / 静态资源）直接透传
    if (!body || typeof body.code !== 'number') {
      return response as unknown as AxiosResponse
    }
    if (body.code === 0) {
      return body.data as unknown as AxiosResponse
    }
    ElMessage.error(body.message || '请求失败')
    return Promise.reject(new Error(body.message || '请求失败'))
  },
  async (error: AxiosError<Result<unknown>>) => {
    const response = error.response
    if (!response) {
      ElMessage.error('网络异常，请稍后重试')
      return Promise.reject(error)
    }

    if (response.status === 401) {
      const config = error.config as RetriableConfig | undefined
      const auth = useAuthStore()
      // 仅患者端（有 refreshToken）且未重试过时，刷新一次并重放
      if (config && !config._retried && auth.role === 'USER' && auth.refreshToken) {
        config._retried = true
        try {
          const tokens = await auth.refresh()
          config.headers.set('Authorization', `Bearer ${tokens.accessToken}`)
          return http(config)
        } catch {
          /* 刷新失败，走登出 */
        }
      }
      const target = auth.isAdmin ? '/admin/login' : '/login'
      auth.logout()
      if (window.location.pathname !== target) window.location.href = target
      return Promise.reject(error)
    }

    if (response.status === 403) {
      ElMessage.error(response.data?.message || '无权限访问')
      return Promise.reject(error)
    }

    ElMessage.error(response.data?.message || '请求失败')
    return Promise.reject(error)
  },
)

// 解包辅助：返回 Promise<data>（拦截器已剥离 Result 包裹）
export function get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return http.get(url, config) as unknown as Promise<T>
}

export function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return http.post(url, data, config) as unknown as Promise<T>
}

export function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return http.put(url, data, config) as unknown as Promise<T>
}

export function del<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return http.delete(url, config) as unknown as Promise<T>
}

export default http
