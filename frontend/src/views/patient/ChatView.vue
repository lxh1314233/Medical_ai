<template>
  <div class="chat-page">
    <aside class="sidebar">
      <div class="sidebar-header">
        <el-button type="primary" :icon="Plus" @click="newSession">新对话</el-button>
      </div>
      <div class="session-list">
        <div
          v-for="s in sessions"
          :key="s.id"
          class="session-item"
          :class="{ active: s.id === activeSessionId }"
          @click="openSession(s.id)"
        >
          <div class="session-body">
            <div class="session-title">{{ s.title }}</div>
            <div class="session-time">{{ formatTime(s.updateTime) }}</div>
          </div>
          <el-dropdown trigger="click" @command="(cmd: string) => onSessionCmd(cmd, s)">
            <el-icon class="more" @click.stop><MoreFilled /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="rename">重命名</el-dropdown-item>
                <el-dropdown-item command="delete">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <el-empty v-if="!sessions.length" description="暂无会话" :image-size="60" />
      </div>
    </aside>

    <section class="chat-main">
      <div class="messages" ref="listRef">
        <div v-if="!messages.length && !streaming" class="welcome">
          <div class="welcome-icon">🩺</div>
          <h2>你好，我是医疗 AI 助手</h2>
          <p>请描述你的健康问题，我会尽力为你解答。如遇紧急情况请立即拨打 120。</p>
        </div>

        <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
          <div class="avatar">{{ m.role === 'user' ? '👤' : '🩺' }}</div>
          <div class="msg-content">
            <div class="bubble">
              <MarkdownView v-if="m.role === 'assistant'" :content="m.content" />
              <span v-else>{{ m.content }}</span>
              <span v-if="m.streaming" class="cursor">▍</span>
            </div>

            <div v-if="m.role === 'assistant' && !m.streaming && m.content" class="msg-foot">
              <div v-if="m.refs && m.refs.length" class="refs">
                <span class="refs-label">参考来源</span>
                <el-tag v-for="(r, ri) in m.refs" :key="ri" size="small" class="ref" :title="r.source">{{ r.title }}</el-tag>
              </div>
              <div class="feedback">
                <el-button link size="small" :disabled="rated.has(m.id)" @click="rate(m, 'HELPFUL')">👍 有帮助</el-button>
                <el-button link size="small" :disabled="rated.has(m.id)" @click="rate(m, 'NOT_HELPFUL')">👎 无帮助</el-button>
                <el-button link size="small" @click="correct(m)">内容有误</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="quick-questions">
        <el-tag v-for="q in quickQuestions" :key="q" class="quick" @click="input = q">{{ q }}</el-tag>
      </div>

      <div class="input-bar">
        <el-input
          v-model="input"
          type="textarea"
          :rows="2"
          resize="none"
          placeholder="请输入健康问题（Enter 发送，Shift+Enter 换行）"
          @keydown.enter.exact.prevent="send"
        />
        <div class="input-actions">
          <el-button v-if="streaming" @click="stop">停止</el-button>
          <el-button type="primary" :loading="streaming" @click="send">发送</el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MoreFilled, Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import MarkdownView from '@/components/MarkdownView.vue'
import { deleteSession, listMessages, listSessions, renameSession } from '@/api/chat'
import { submitFeedback } from '@/api/feedback'
import { streamChat, type StreamHandle } from '@/utils/sse'
import { useAuthStore } from '@/stores/auth'
import type { RefVO, SessionVO } from '@/types'

interface ChatMsg {
  id: number
  role: 'user' | 'assistant'
  content: string
  refs: RefVO[]
  createTime: string
  streaming?: boolean
}

const auth = useAuthStore()

const sessions = ref<SessionVO[]>([])
const activeSessionId = ref<number | null>(null)
const messages = ref<ChatMsg[]>([])
const input = ref('')
const streaming = ref(false)
const rated = ref(new Set<number>())
const listRef = ref<HTMLElement>()

let streamHandle: StreamHandle | null = null

const quickQuestions = ['感冒了怎么办？', '发烧多少度需要就医？', '高血压日常要注意什么？', '长期失眠如何改善？']

function formatTime(t: string) {
  return dayjs(t).format('MM-DD HH:mm')
}

function scrollToBottom() {
  nextTick(() => {
    if (listRef.value) listRef.value.scrollTop = listRef.value.scrollHeight
  })
}

async function loadSessions() {
  sessions.value = await listSessions()
}

async function openSession(id: number) {
  activeSessionId.value = id
  messages.value = (await listMessages(id)) as ChatMsg[]
  scrollToBottom()
}

function newSession() {
  if (streaming.value) return
  activeSessionId.value = null
  messages.value = []
  input.value = ''
}

async function onSessionCmd(cmd: string, s: SessionVO) {
  if (cmd === 'rename') {
    const { value } = await ElMessageBox.prompt('请输入新标题', '重命名会话', {
      inputValue: s.title,
      inputValidator: (v) => (v && v.trim() ? true : '标题不能为空'),
    })
    await renameSession(s.id, { title: value.trim() })
    ElMessage.success('已重命名')
    loadSessions()
  } else if (cmd === 'delete') {
    await ElMessageBox.confirm('删除后不可恢复，确定删除该会话？', '删除会话', { type: 'warning' })
    await deleteSession(s.id)
    if (activeSessionId.value === s.id) newSession()
    loadSessions()
  }
}

function send() {
  const text = input.value.trim()
  if (!text || streaming.value) return
  input.value = ''

  // 用户消息（本地占位）
  messages.value.push({ id: 0, role: 'user', content: text, refs: [], createTime: '' })
  // 助手占位
  messages.value.push({ id: 0, role: 'assistant', content: '', refs: [], createTime: '', streaming: true })
  const assistantIdx = messages.value.length - 1
  scrollToBottom()

  streaming.value = true
  const wasNew = activeSessionId.value == null

  streamHandle = streamChat(
    auth.accessToken,
    { sessionId: activeSessionId.value ?? undefined, message: text },
    {
      onDelta: (content) => {
        messages.value[assistantIdx].content += content
        scrollToBottom()
      },
      onDone: (e) => {
        messages.value[assistantIdx].id = e.messageId
        messages.value[assistantIdx].refs = e.refs
        messages.value[assistantIdx].streaming = false
        streaming.value = false
        if (wasNew) {
          activeSessionId.value = e.sessionId
          loadSessions()
        }
        scrollToBottom()
      },
      onError: (msg) => {
        messages.value[assistantIdx].streaming = false
        if (!messages.value[assistantIdx].content) messages.value[assistantIdx].content = msg || '服务异常，请稍后重试'
        streaming.value = false
        scrollToBottom()
      },
    },
  )
}

function stop() {
  streamHandle?.close()
  streamHandle = null
  streaming.value = false
  const last = messages.value[messages.value.length - 1]
  if (last && last.streaming) last.streaming = false
}

async function rate(m: ChatMsg, type: 'HELPFUL' | 'NOT_HELPFUL') {
  if (!m.id || rated.value.has(m.id)) return
  await submitFeedback({ type, messageId: m.id })
  rated.value.add(m.id)
  ElMessage.success('感谢你的反馈')
}

async function correct(m: ChatMsg) {
  if (!m.id) return
  try {
    const { value } = await ElMessageBox.prompt('请说明内容错误之处', '内容纠错', {
      inputType: 'textarea',
      inputValidator: (v) => (v && v.trim() ? true : '请填写纠错说明'),
    })
    await submitFeedback({ type: 'CORRECTION', messageId: m.id, content: value.trim() })
    ElMessage.success('已提交纠错，将进入审核队列')
  } catch {
    /* 用户取消 */
  }
}

onMounted(loadSessions)
</script>

<style scoped>
.chat-page {
  display: flex;
  height: calc(100vh - 60px);
}

.sidebar {
  width: 260px;
  border-right: 1px solid #ebeef5;
  background: #fff;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 12px;
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
  border-radius: 6px;
  margin: 2px 8px;
}

.session-item:hover {
  background: #f5f7fa;
}

.session-item.active {
  background: #ecf5ff;
}

.session-body {
  flex: 1;
  min-width: 0;
}

.session-title {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 12px;
  color: #c0c4cc;
}

.more {
  color: #c0c4cc;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.welcome {
  text-align: center;
  margin-top: 80px;
  color: #909399;
}

.welcome-icon {
  font-size: 48px;
}

.welcome h2 {
  color: #303133;
  margin: 12px 0 8px;
}

.msg {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
}

.msg.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;
}

.msg-content {
  max-width: 75%;
  min-width: 0;
}

.msg.user .msg-content {
  display: flex;
  justify-content: flex-end;
}

.bubble {
  display: inline-block;
  padding: 10px 14px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid #ebeef5;
  line-height: 1.6;
  font-size: 14px;
  white-space: pre-wrap;
  word-break: break-word;
  text-align: left;
}

.msg.user .bubble {
  background: #409eff;
  color: #fff;
  border: none;
}

.cursor {
  color: #409eff;
  animation: blink 1s infinite;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

.msg-foot {
  margin-top: 6px;
}

.refs {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 4px;
}

.refs-label {
  font-size: 12px;
  color: #909399;
}

.feedback {
  display: flex;
  gap: 4px;
}

.quick-questions {
  display: flex;
  gap: 8px;
  padding: 8px 20px;
  flex-wrap: wrap;
}

.quick {
  cursor: pointer;
}

.input-bar {
  display: flex;
  gap: 12px;
  padding: 12px 20px 20px;
  border-top: 1px solid #ebeef5;
  background: #fff;
  align-items: flex-end;
}

.input-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }

  .msg-content {
    max-width: 85%;
  }
}
</style>
