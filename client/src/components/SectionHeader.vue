<script setup lang="ts">
import { useRouter } from 'vue-router'
import { NButton } from 'naive-ui'
import UiIcon from '@/components/UiIcon.vue'

defineProps<{ title: string }>()

const router = useRouter()
</script>

<template>
  <div class="mb-6 flex items-center justify-between gap-4">
    <h2 class="text-2xl font-extrabold tracking-tight text-gray-700">{{ title }}</h2>
    <NButton quaternary type="primary" @click="router.push('/products')">
      <template #icon><UiIcon name="arrow-right" size="16" /></template>
      Xem tất cả
    </NButton>
  </div>
</template>