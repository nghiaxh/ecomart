<script setup lang="ts">
import type { Address } from '@/types'

withDefaults(defineProps<{
  address: Address
  selected?: boolean
  selectable?: boolean
  editable?: boolean
}>(), {
  selected: false,
  selectable: false,
  editable: false
})

const emit = defineEmits<{
  select: [id: number]
  edit: [address: Address]
  delete: [address: Address]
  setDefault: [address: Address]
}>()
</script>

<template>
  <div
    class="flex items-start gap-3 rounded-xl border p-4 transition"
    :class="[selected ? 'border-emerald-500 bg-emerald-50' : 'border-gray-200', selectable ? 'cursor-pointer' : '']"
    @click="selectable && emit('select', address.id)"
  >
    <input
      v-if="selectable"
      type="radio"
      :checked="selected"
      class="mt-1 accent-emerald-600"
      @change="emit('select', address.id)"
      @click.stop
    />
    <div class="flex-1">
      <div class="flex items-center gap-2">
        <span class="font-semibold text-gray-700">{{ address.receiverName }}</span>
        <span class="text-sm text-gray-400">{{ address.receiverPhone }}</span>
        <span v-if="address.isDefault" class="rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-semibold text-emerald-700">Mặc định</span>
      </div>
      <p class="mt-1 text-sm text-gray-500">{{ address.label }} · {{ address.street }}, {{ address.ward }}, {{ address.district }}, {{ address.city }}</p>
    </div>

    <div v-if="editable" class="flex shrink-0 items-center gap-1">
      <button
        v-if="!address.isDefault"
        type="button"
        class="rounded-lg p-1.5 text-gray-400 transition hover:bg-emerald-50 hover:text-emerald-700"
        title="Đặt làm mặc định"
        @click="emit('setDefault', address)"
      >
        <UIcon name="i-ph-check" class="h-4 w-4" />
      </button>
      <button
        type="button"
        class="rounded-lg p-1.5 text-gray-400 transition hover:bg-emerald-50 hover:text-emerald-700"
        title="Chỉnh sửa"
        @click="emit('edit', address)"
      >
        <UIcon name="i-ph-pencil-simple" class="h-4 w-4" />
      </button>
      <button
        type="button"
        class="rounded-lg p-1.5 text-gray-400 transition hover:bg-red-50 hover:text-red-600"
        title="Xóa"
        @click="emit('delete', address)"
      >
        <UIcon name="i-ph-trash" class="h-4 w-4" />
      </button>
    </div>
  </div>
</template>
