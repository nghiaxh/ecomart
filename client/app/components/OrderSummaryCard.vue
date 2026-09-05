<script setup lang="ts">
const props = defineProps<{
  subtotal: number
  itemCount?: number
  title?: string
}>()

const { formatVND } = useFormat()
</script>

<template>
  <div class="h-fit rounded-2xl border border-emerald-100 bg-white p-6 lg:sticky lg:top-20">
    <h2 class="text-lg font-bold text-gray-800">
      <slot name="title">{{ title ?? 'Tóm tắt đơn hàng' }}</slot>
    </h2>

    <div class="mt-4">
      <slot name="items" />
    </div>

    <div class="mt-4 space-y-2 border-t border-emerald-50 pt-4 text-sm">
      <div class="flex justify-between text-gray-500">
        <span>Tạm tính{{ itemCount ? ` (${itemCount} món)` : '' }}</span>
        <span>{{ formatVND(props.subtotal) }}</span>
      </div>
      <div class="flex justify-between text-gray-500">
        <span>Phí giao hàng</span>
        <span>Tính khi đặt hàng</span>
      </div>
    </div>
    <div class="mt-4 border-t border-emerald-50 pt-4">
      <div class="flex justify-between text-lg">
        <span class="font-semibold text-gray-700">Tổng cộng</span>
        <span class="font-bold text-emerald-700">{{ formatVND(props.subtotal) }}</span>
      </div>
    </div>

    <div class="mt-6">
      <slot name="actions" />
    </div>
  </div>
</template>
