<template>
  <div class="page-wrap">
    <el-card>
      <el-tabs v-model="tab">
        <el-tab-pane label="药品查询" name="query">
          <div class="search-row">
            <el-input v-model="drugName" placeholder="输入药品通用名或商品名，如：阿莫西林" @keyup.enter="searchDrug" />
            <el-button type="primary" :loading="searching" @click="searchDrug">查询</el-button>
          </div>

          <el-empty v-if="!drug && !searching" description="输入药品名查询适应症、用法用量、禁忌等信息" />

          <div v-if="drug">
            <el-alert v-if="!drug.found" :title="drug.message || '未查询到该药品，请核对名称或咨询医生/药师'" type="warning" :closable="false" show-icon />

            <template v-if="drug.found">
              <h3 class="drug-name">{{ drug.genericName }}</h3>
              <p v-if="drug.brandNames" class="brand">商品名：{{ drug.brandNames }}</p>

              <el-descriptions :column="1" border class="desc">
                <el-descriptions-item label="适应症">{{ drug.indications }}</el-descriptions-item>
                <el-descriptions-item label="用法用量">{{ drug.dosage }}</el-descriptions-item>
                <el-descriptions-item label="禁忌">{{ drug.contraindications }}</el-descriptions-item>
                <el-descriptions-item label="不良反应">{{ drug.adverseReactions }}</el-descriptions-item>
                <el-descriptions-item label="贮藏">{{ drug.storage }}</el-descriptions-item>
                <el-descriptions-item label="特殊人群提示">{{ drug.specialPopulation }}</el-descriptions-item>
              </el-descriptions>

              <div v-if="drug.warnings && drug.warnings.length" class="warnings">
                <el-alert v-for="(w, i) in drug.warnings" :key="i" :title="w" type="warning" :closable="false" show-icon class="warn-item" />
              </div>

              <DisclaimerBar v-if="drug.disclaimer" :text="drug.disclaimer" />
            </template>
          </div>
        </el-tab-pane>

        <el-tab-pane label="药物相互作用" name="interaction">
          <p class="hint">填写正在服用的药品（含 OTC、保健品），检查是否存在相互作用风险。</p>
          <el-select
            v-model="drugs"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入药品名后回车添加，可添加多种"
            style="width: 100%"
            class="drug-select"
          />
          <el-button type="primary" :loading="checking" @click="onCheck">检查相互作用</el-button>

          <div v-if="interactionResult">
            <template v-if="interactionResult.interactions.length">
              <el-card v-for="(it, i) in interactionResult.interactions" :key="i" class="interaction-card">
                <div class="interaction-head">
                  <span class="pair">{{ it.drugA }} + {{ it.drugB }}</span>
                  <el-tag :type="riskType(it.riskLevel)" size="small">{{ it.riskLevel }}风险</el-tag>
                </div>
                <p class="interaction-desc">{{ it.description }}</p>
              </el-card>
            </template>
            <el-alert v-else title="暂未发现已知相互作用，但仍建议遵医嘱用药" type="success" :closable="false" show-icon />
            <DisclaimerBar v-if="interactionResult.note" :text="interactionResult.note" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { checkInteractions, getDrugInfo } from '@/api/medication'
import DisclaimerBar from '@/components/DisclaimerBar.vue'
import type { DrugInfoVO, InteractionResultVO } from '@/types'

const tab = ref('query')

const drugName = ref('')
const searching = ref(false)
const drug = ref<DrugInfoVO | null>(null)

const drugs = ref<string[]>([])
const checking = ref(false)
const interactionResult = ref<InteractionResultVO | null>(null)

async function searchDrug() {
  const name = drugName.value.trim()
  if (!name) {
    ElMessage.warning('请输入药品名')
    return
  }
  searching.value = true
  drug.value = null
  try {
    drug.value = await getDrugInfo(name)
  } catch {
    /* 拦截器已提示 */
  } finally {
    searching.value = false
  }
}

async function onCheck() {
  if (drugs.value.length === 0) {
    ElMessage.warning('请至少添加一种药品')
    return
  }
  checking.value = true
  try {
    interactionResult.value = await checkInteractions({ drugs: drugs.value })
  } catch {
    /* 拦截器已提示 */
  } finally {
    checking.value = false
  }
}

function riskType(level: string) {
  if (level === '高') return 'danger'
  if (level === '中') return 'warning'
  return 'info'
}
</script>

<style scoped>
.page-wrap {
  max-width: 760px;
  margin: 24px auto;
  padding: 0 16px;
}

.search-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.drug-name {
  margin: 8px 0 4px;
}

.brand {
  color: #909399;
  font-size: 13px;
  margin: 0 0 16px;
}

.desc {
  margin-bottom: 16px;
}

.warnings {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hint {
  color: #909399;
  font-size: 13px;
  margin-bottom: 12px;
}

.drug-select {
  margin-bottom: 12px;
}

.interaction-card {
  margin-top: 12px;
}

.interaction-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.pair {
  font-weight: 600;
}

.interaction-desc {
  color: #606266;
  line-height: 1.7;
  margin: 0;
}
</style>
