<script setup lang="ts">
import type { AddressForm } from '@/schemas'
import { NButton, NCheckbox, NInput } from 'naive-ui'

const props = defineProps<{
  errors: Record<string, string>
  saving?: boolean
}>()

const emit = defineEmits<{ submit: []; cancel: [] }>()

const form = defineModel<AddressForm>({ required: true })

const fields = [
  { key: 'label', placeholder: 'Nhãn (Nhà, Cơ quan...)', span: false },
  { key: 'receiverName', placeholder: 'Người nhận', span: false },
  { key: 'receiverPhone', placeholder: 'Số điện thoại', span: true },
  { key: 'street', placeholder: 'Số nhà, đường, thôn/xóm', span: true },
  { key: 'ward', placeholder: 'Phường/Xã', span: false },
  { key: 'district', placeholder: 'Quận/Huyện', span: false },
  { key: 'city', placeholder: 'Tỉnh/Thành phố', span: false }
] as const
</script>

<template>
  <form class="mt-4 grid gap-3 sm:grid-cols-2" @submit.prevent="emit('submit')">
    <div v-for="f in fields" :key="f.key" :class="f.span ? 'sm:col-span-2' : ''">
      <NInput v-model:value="(form as any)[f.key]" :placeholder="f.placeholder" class="w-full" />
      <p v-if="props.errors[f.key]" class="mt-1 text-xs text-red-600">{{ props.errors[f.key] }}</p>
    </div>
    <div class="flex items-center gap-2">
      <NCheckbox v-model:checked="form.isDefault">
        <label class="text-sm text-gray-700">Đặt làm địa chỉ mặc định</label>
      </NCheckbox>
    </div>
    <div class="flex justify-end gap-2 sm:col-span-2">
      <NButton quaternary @click="emit('cancel')">Hủy</NButton>
      <NButton attr-type="submit" :loading="props.saving">Lưu địa chỉ</NButton>
    </div>
  </form>
</template>