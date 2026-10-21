import { get, put } from '@/utils/request'
import type { PageVO, UserStatusReq, UserVO } from '@/types'

export function listUsers(params: {
  page?: number
  size?: number
  phone?: string
  nickname?: string
}): Promise<PageVO<UserVO>> {
  return get<PageVO<UserVO>>('/admin/users', { params })
}

export function updateUserStatus(id: number, data: UserStatusReq): Promise<void> {
  return put<void>(`/admin/users/${id}/status`, data)
}
