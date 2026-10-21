import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { sendCode } from '@/api/auth'
import type { SmsScene } from '@/types'

/**
 * 短信验证码发送 + 60s 倒计时（登录/注册/找回密码共用）。
 */
export function useSmsCode(scene: SmsScene) {
  const countdown = ref(0)
  let timer: number | undefined

  async function send(phone: string) {
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      ElMessage.warning('请输入正确的手机号')
      return
    }
    if (countdown.value > 0) return
    try {
      await sendCode({ phone, scene })
      ElMessage.success('验证码已发送（开发环境请查看后端日志）')
      countdown.value = 60
      timer = window.setInterval(() => {
        countdown.value -= 1
        if (countdown.value <= 0) window.clearInterval(timer)
      }, 1000)
    } catch {
      /* 拦截器已提示错误 */
    }
  }

  return { countdown, send }
}
