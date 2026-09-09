<script setup lang="ts">
import { computed } from 'vue'
import { NButton } from 'naive-ui'
import UiIcon from '@/components/UiIcon.vue'

const props = defineProps<{
  stock: number
  loading?: boolean
  block?: boolean
}>()

const emit = defineEmits<{ add: [] }>()

const disabled = computed(() => props.stock <= 0 || props.loading)
const label = computed(() => (props.stock <= 0 ? 'Hết hàng' : 'Thêm vào giỏ'))
</script>

<template>
  <NButton type="primary" size="large" :block="block" :disabled="disabled" :loading="loading" @click="emit('add')">
    <template #icon><UiIcon name="shopping-cart" /></template>
    {{ label }}
  </NButton>
</template>