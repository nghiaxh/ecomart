<script setup lang="ts">
defineProps<{ title: string }>()
</script>

<template>
  <div class="mb-6 flex items-center justify-between gap-4">
    <h2 class="text-2xl font-extrabold tracking-tight text-gray-800">{{ title }}</h2>
    <UButton to="/products" color="primary" variant="ghost" label="Xem tất cả" trailing-icon="i-ph-arrow-right" />
  </div>
</template>