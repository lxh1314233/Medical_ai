<template>
  <div class="markdown-body" v-html="html"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'

const props = defineProps<{ content: string }>()

// html:false 防 XSS；答案由后端 LLM/知识库生成 + 固定免责声明，安全可控
const md = new MarkdownIt({ html: false, linkify: true, breaks: true })

const html = computed(() => md.render(props.content || ''))
</script>
