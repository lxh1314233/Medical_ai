# 医疗 AI 问答系统 — 前端

患者端 + 管理端单页应用（SPA），按角色分路由。

## 技术栈

- **Vue 3**（`<script setup>` + TypeScript）
- **Vite 6**、**TypeScript 5.7**、**Element Plus 2.9**、**Pinia 3**、**Vue Router 4**
- **Axios**（请求封装 + 统一响应解包 + 401 刷新）、**@microsoft/fetch-event-source**（POST SSE 流式问答）
- **markdown-it**（答案 Markdown 渲染）、**echarts**（管理端统计图表）、**dayjs**（时间格式化）

> 关于「版本 20+」：Vue 生态不存在 v20，最新大版本是 **Vue 3**。此处按 **Node.js ≥ 20** 落地（Vite 7 要求 Node ≥ 20.19），实际使用 **Node 20/22 LTS + Vue 3 + Vite**。

## 运行

```bash
# 1. 确保本机 Node ≥ 20
node -v

# 2. 安装依赖
npm install

# 3. 启动开发服务器（默认 http://localhost:5173，/api 代理到 http://localhost:8080）
npm run dev

# 4. 生产构建（vue-tsc 类型检查 + vite 打包）
npm run build
```

后端需先启动（MySQL/Redis 就绪后 `./mvnw spring-boot:run`，默认 `http://localhost:8080`）。

## 目录结构

```
src/
├── api/            # 按域拆分的接口封装（patient + admin/）
├── types/          # 全部后端 DTO 的 TypeScript 类型
├── utils/          # request.ts（axios + 拦截器）、sse.ts（流式问答）
├── stores/         # Pinia：auth.ts（token/角色持久化）
├── router/         # 路由表 + 全局守卫（requiresAuth / roles）
├── composables/    # useSmsCode.ts（验证码倒计时）
├── components/     # PatientLayout / AdminLayout / MarkdownView / DisclaimerBar / EmergencyBanner
└── views/
    ├── patient/    # 落地页、登录、注册、找回密码、智能问答、分诊、用药、报告、档案、反馈、知识库
    └── admin/      # 管理登录、数据看板、知识库、内容审核、用户管理、管理员账号
```

## 关键约定

- **统一响应** `Result<T> = { code, message, data }`，`code === 0` 成功；业务错误返回 HTTP 200 + 非零 code（拦截器统一 `ElMessage.error`）。
- **鉴权**：`Authorization: Bearer <accessToken>`；患者与管理员是两套独立登录。患者 token 过期自动用 `refreshToken` 静默续期一次；管理员无刷新接口，过期需重登。
- **流式问答**：`POST /api/chat/messages` 返回 `text/event-stream`，事件为 **data-only**（无 `event:` 字段），payload 按 `type` 区分 `delta` / `done` / `error`；因走 POST，使用 `@microsoft/fetch-event-source` 而非 `EventSource`。
- **分页**：患者端接口全量返回 `List`；管理端返回 `PageVO<T> = { records, total, page, size }`。

## 测试账号

- 管理端：`admin / admin123`（SUPER_ADMIN；另可建 OPERATOR / AUDITOR 验证菜单显隐与 403）
- 患者端：任意手机号注册（短信验证码为 mock，打印在后端日志 `[MOCK SMS] ... code=xxxxxx`）
