<script setup lang="ts">
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
  <UButton
    color="primary"
    size="lg"
    :block="block"
    icon="i-ph-shopping-cart"
    :disabled="disabled"
    :loading="loading"
    @click="emit('add')"
  >
    {{ label }}
  </UButton>
</template>
