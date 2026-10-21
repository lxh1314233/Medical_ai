import { post } from '@/utils/request'
import type { AdminLoginReq, AdminLoginResp } from '@/types'

export function adminLogin(data: AdminLoginReq): Promise<AdminLoginResp> {
  return post<AdminLoginResp>('/admin/auth/login', data)
}
