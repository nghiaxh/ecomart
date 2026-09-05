<script setup lang="ts">
defineProps<{
  page: number
  totalPages: number
}>()

const emit = defineEmits<{ prev: []; next: [] }>()
</script>

<template>
  <div class="mt-6 flex items-center justify-center gap-3">
    <UButton icon="i-ph-caret-left" color="neutral" variant="ghost" :disabled="page <= 0" aria-label="Trang trước" @click="emit('prev')" />
    <span class="text-sm text-gray-500">Trang {{ page + 1 }}/{{ totalPages }}</span>
    <UButton icon="i-ph-caret-right" color="neutral" variant="ghost" :disabled="page + 1 >= totalPages" aria-label="Trang sau" @click="emit('next')" />
  </div>
</template>
