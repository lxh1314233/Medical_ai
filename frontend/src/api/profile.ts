import { del, get, put } from '@/utils/request'
import type { ProfileReq, ProfileVO } from '@/types'

export function getProfile(): Promise<ProfileVO> {
  return get<ProfileVO>('/profile')
}

export function updateProfile(data: ProfileReq): Promise<void> {
  return put<void>('/profile', data)
}

export function deleteProfile(): Promise<void> {
  return del<void>('/profile')
}
