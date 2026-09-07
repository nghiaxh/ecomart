<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string
    message?: string
    confirmLabel?: string
    cancelLabel?: string
  }>(),
  {
    title: 'Xác nhận',
    message: 'Bạn có chắc chắn muốn tiếp tục?',
    confirmLabel: 'Xác nhận',
    cancelLabel: 'Hủy'
  }
)

const emit = defineEmits<{ close: [value: boolean] }>()
</script>

<template>
  <UModal :title="title" :description="message">
    <template #footer>
      <div class="flex w-full justify-end gap-2">
        <UButton color="neutral" variant="ghost" @click="emit('close', false)">
          {{ cancelLabel }}
        </UButton>
        <UButton color="error" @click="emit('close', true)">
          {{ confirmLabel }}
        </UButton>
      </div>
    </template>
  </UModal>
</template>
