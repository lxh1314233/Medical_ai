<template>
  <div class="landing">
    <section class="hero">
      <h1>医疗 AI 问答</h1>
      <p class="subtitle">智能健康咨询 · 症状分诊 · 用药指导 · 报告解读</p>
      <div class="cta">
        <el-button type="primary" size="large" @click="router.push('/chat')">开始咨询</el-button>
        <el-button size="large" @click="router.push('/register')">注册账号</el-button>
      </div>
      <p class="emergency">如遇胸痛、呼吸困难、意识障碍、大出血等紧急情况，请立即拨打 120 或前往急诊。</p>
    </section>

    <section class="features">
      <el-card v-for="f in features" :key="f.title" class="feature" shadow="hover">
        <div class="icon">{{ f.icon }}</div>
        <h3>{{ f.title }}</h3>
        <p>{{ f.desc }}</p>
      </el-card>
    </section>

    <DisclaimerBar class="disclaimer" />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import DisclaimerBar from '@/components/DisclaimerBar.vue'

const router = useRouter()
const auth = useAuthStore()

const features = [
  { icon: '💬', title: '智能问答', desc: '多轮对话医疗知识问答，答案可溯源，支持流式输出' },
  { icon: '🏥', title: '症状分诊', desc: '根据症状推荐科室、判断紧急程度，红旗症状即时预警' },
  { icon: '💊', title: '用药咨询', desc: '药品信息查询、用法用量、药物相互作用与特殊人群提示' },
  { icon: '📄', title: '报告解读', desc: '上传化验单，自动识别指标并标注异常与危急值' },
  { icon: '🔒', title: '隐私保护', desc: '健康档案加密存储，支持随时删除' },
  { icon: '🛡️', title: '安全合规', desc: '内容仅供参考，不替代医生面诊与诊断' },
]

onMounted(() => {
  if (auth.isLoggedIn) {
    router.replace(auth.isAdmin ? '/admin/dashboard' : '/chat')
  }
})
</script>

<style scoped>
.landing {
  max-width: 1000px;
  margin: 0 auto;
  padding: 60px 20px 40px;
}

.hero {
  text-align: center;
  margin-bottom: 48px;
}

.hero h1 {
  font-size: 40px;
  margin: 0 0 12px;
  color: #303133;
}

.subtitle {
  font-size: 18px;
  color: #909399;
  margin-bottom: 28px;
}

.cta {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.emergency {
  margin-top: 24px;
  font-size: 13px;
  color: #e6a23c;
}

.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.feature {
  text-align: center;
}

.feature .icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.feature h3 {
  margin: 0 0 8px;
  font-size: 16px;
}

.feature p {
  margin: 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}

.disclaimer {
  margin-top: 40px;
}

@media (max-width: 768px) {
  .features {
    grid-template-columns: 1fr;
  }
}
</style>
