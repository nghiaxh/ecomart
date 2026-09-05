<script setup lang="ts">
import type { AddressForm } from '~/schemas'

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
      <UInput v-model="(form as any)[f.key]" :placeholder="f.placeholder" />
      <p v-if="props.errors[f.key]" class="mt-1 text-xs text-red-600">{{ props.errors[f.key] }}</p>
    </div>
    <UCheckbox v-model="form.isDefault" label="Đặt làm địa chỉ mặc định" />
    <div class="flex justify-end gap-2 sm:col-span-2">
      <UButton color="neutral" variant="ghost" label="Hủy" @click="emit('cancel')" />
      <UButton type="submit" color="primary" label="Lưu địa chỉ" :loading="props.saving" />
    </div>
  </form>
</template>
