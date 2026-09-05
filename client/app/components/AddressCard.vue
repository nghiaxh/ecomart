<script setup lang="ts">
import type { Address } from '~/types'

defineProps<{
  address: Address
  selected?: boolean
  selectable?: boolean
}>()

const emit = defineEmits<{ select: [id: number] }>()
</script>

<template>
  <label
    class="flex cursor-pointer items-start gap-3 rounded-xl border p-4 transition"
    :class="selected ? 'border-emerald-500 bg-emerald-50' : 'border-gray-200'"
  >
    <input
      v-if="selectable"
      type="radio"
      :checked="selected"
      class="mt-1 accent-emerald-600"
      @change="emit('select', address.id)"
    />
    <div class="flex-1">
      <div class="flex items-center gap-2">
        <span class="font-semibold text-gray-800">{{ address.receiverName }}</span>
        <span class="text-sm text-gray-400">{{ address.receiverPhone }}</span>
        <span v-if="address.isDefault" class="rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-semibold text-emerald-700">Mặc định</span>
      </div>
      <p class="mt-1 text-sm text-gray-500">{{ address.label }} · {{ address.street }}, {{ address.ward }}, {{ address.district }}, {{ address.city }}</p>
    </div>
  </label>
</template>
