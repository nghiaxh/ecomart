<script setup lang="ts">
import { NButton } from 'naive-ui'
import UiIcon from '@/components/UiIcon.vue'

defineProps<{
  page: number
  totalPages: number
}>()

const emit = defineEmits<{ prev: []; next: [] }>()
</script>

<template>
  <div class="mt-6 flex items-center justify-center gap-3">
    <NButton quaternary :disabled="page <= 0" aria-label="Trang trước" @click="emit('prev')">
      <template #icon><UiIcon name="caret-left" /></template>
    </NButton>
    <span class="text-sm text-gray-500">Trang {{ page + 1 }}/{{ totalPages }}</span>
    <NButton quaternary :disabled="page + 1 >= totalPages" aria-label="Trang sau" @click="emit('next')">
      <template #icon><UiIcon name="caret-right" /></template>
    </NButton>
  </div>
</template>