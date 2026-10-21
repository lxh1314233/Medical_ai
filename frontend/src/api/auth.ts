import { post } from '@/utils/request'
import type { LoginReq, RegisterReq, ResetPasswordReq, SendCodeReq, TokenResp } from '@/types'

export function sendCode(data: SendCodeReq): Promise<void> {
  return post<void>('/auth/sms-code', data)
}

export function register(data: RegisterReq): Promise<TokenResp> {
  return post<TokenResp>('/auth/register', data)
}

export function login(data: LoginReq): Promise<TokenResp> {
  return post<TokenResp>('/auth/login', data)
}

export function resetPassword(data: ResetPasswordReq): Promise<void> {
  return post<void>('/auth/reset-password', data)
}

export function logout(): Promise<void> {
  return post<void>('/auth/logout')
}
