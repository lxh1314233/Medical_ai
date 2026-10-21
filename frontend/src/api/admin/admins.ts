import { get, post } from '@/utils/request'
import type { AdminCreateReq, AdminVO } from '@/types'

export function listAdmins(): Promise<AdminVO[]> {
  return get<AdminVO[]>('/admin/admins')
}

export function createAdmin(data: AdminCreateReq): Promise<AdminVO> {
  return post<AdminVO>('/admin/admins', data)
}
