<template>
  <div>
    <el-card>
      <div class="filter-bar">
        <el-select v-model="filters.status" placeholder="状态" clearable style="width: 140px" @change="search">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="已下架" value="OFFLINE" />
        </el-select>
        <el-input v-model="filters.category" placeholder="分类" clearable style="width: 160px" @keyup.enter="search" />
        <el-input v-model="filters.keyword" placeholder="标题/内容关键词" clearable style="width: 220px" @keyup.enter="search" />
        <el-button type="primary" :icon="Search" @click="search">查询</el-button>
        <el-button :icon="Plus" type="success" @click="openCreate">新建</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="source" label="来源" width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="70" />
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status !== 'PUBLISHED'" link type="success" @click="publish(row)">上架</el-button>
            <el-button v-else link type="warning" @click="offline(row)">下架</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pager"
        @current-change="load"
        @size-change="search"
      />
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑知识条目' : '新建知识条目'" width="640px">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="标题 *" prop="title">
          <el-input v-model="form.title" placeholder="知识条目标题" />
        </el-form-item>
        <el-form-item label="内容 *" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="知识内容" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类">
              <el-input v-model="form.category" placeholder="如：内科 / 用药 / 检验" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源">
              <el-input v-model="form.source" placeholder="如：国家药典 / 指南" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="知识条目详情" width="640px">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusName(detail.status) }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ detail.category || '—' }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ detail.source || '—' }}</el-descriptions-item>
          <el-descriptions-item label="版本">v{{ detail.version }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-content">{{ detail.content }}</div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  createKnowledge,
  deleteKnowledge,
  listKnowledge,
  offlineKnowledge,
  publishKnowledge,
  updateKnowledge,
} from '@/api/admin/knowledge'
import type { KnowledgeVO } from '@/types'

const list = ref<KnowledgeVO[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const filters = reactive({ status: '', category: '', keyword: '' })

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({ title: '', content: '', source: '', category: '' })

const detailVisible = ref(false)
const detail = ref<KnowledgeVO | null>(null)

const rules: FormRules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
}

function statusName(s: string) {
  if (s === 'PUBLISHED') return '已发布'
  if (s === 'OFFLINE') return '已下架'
  return '待审核'
}

function statusType(s: string) {
  if (s === 'PUBLISHED') return 'success'
  if (s === 'OFFLINE') return 'info'
  return 'warning'
}

function formatTime(t: string) {
  return dayjs(t).format('YYYY-MM-DD HH:mm')
}

async function load() {
  loading.value = true
  try {
    const res = await listKnowledge({
      page: page.value,
      size: size.value,
      status: filters.status || undefined,
      category: filters.category || undefined,
      keyword: filters.keyword || undefined,
    })
    list.value = res.records
    total.value = res.total
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { title: '', content: '', source: '', category: '' })
  dialogVisible.value = true
}

function openEdit(row: KnowledgeVO) {
  editingId.value = row.id
  Object.assign(form, { title: row.title, content: row.content, source: row.source, category: row.category })
  dialogVisible.value = true
}

function openDetail(row: KnowledgeVO) {
  detail.value = row
  detailVisible.value = true
}

async function save() {
  if (!(await formRef.value?.validate())) return
  saving.value = true
  try {
    if (editingId.value) await updateKnowledge(editingId.value, { ...form })
    else await createKnowledge({ ...form })
    ElMessage.success('已保存')
    dialogVisible.value = false
    load()
  } catch {
    /* 拦截器已提示 */
  } finally {
    saving.value = false
  }
}

async function publish(row: KnowledgeVO) {
  await publishKnowledge(row.id)
  ElMessage.success('已上架')
  load()
}

async function offline(row: KnowledgeVO) {
  await offlineKnowledge(row.id)
  ElMessage.success('已下架')
  load()
}

async function remove(row: KnowledgeVO) {
  await ElMessageBox.confirm(`确定删除「${row.title}」？删除后不可恢复。`, '删除', { type: 'warning' })
  await deleteKnowledge(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.pager {
  margin-top: 16px;
  justify-content: flex-end;
}

.detail-content {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  white-space: pre-wrap;
  line-height: 1.7;
  max-height: 360px;
  overflow-y: auto;
  font-size: 14px;
}
</style>
