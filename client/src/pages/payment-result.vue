<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useToast } from '@/composables/useToast'
import { useRoute, useRouter } from 'vue-router'
import { useApi } from '@/composables/useApi'
import UiIcon from '@/components/UiIcon.vue'
import { NButton } from 'naive-ui'

const route = useRoute()
const router = useRouter()
const { request } = useApi()
const toast = useToast()

const orderId = computed(() => Number(route.query.orderCode) || Number(route.query.id) || 0)
const cancelled = computed(() => route.query.cancel === 'true' || String(route.query.status).toUpperCase() === 'CANCELLED')

const state = ref<'processing' | 'success' | 'cancelled' | 'failed'>('processing')

let confirmed = false

async function confirmPayment() {
  if (!orderId.value) {
    state.value = 'failed'
    return
  }
  if (confirmed) return
  confirmed = true
  state.value = 'processing'
  try {
    await request(`/api/payments/payos/return?orderId=${orderId.value}`, { method: 'POST' })
    state.value = 'success'
  } catch (error: any) {
    state.value = 'failed'
    toast.add({ severity: 'error', summary: error?.data?.message || 'Không thể xác nhận thanh toán', life: 4000 })
  }
}

onMounted(() => {
  if (cancelled.value) {
    state.value = 'cancelled'
  } else {
    confirmPayment()
  }
})
</script>

<template>
  <div class="mx-auto max-w-xl px-4 py-20 sm:px-6">
    <div v-if="state === 'processing'" class="rounded-2xl border border-emerald-100 bg-white p-10 text-center">
      <span class="mx-auto grid h-16 w-16 place-items-center rounded-full bg-emerald-100 text-emerald-600">
        <UiIcon name="spinner" size="40" class="animate-spin" />
      </span>
      <h1 class="mt-6 text-xl font-extrabold text-gray-700">Đang xác nhận thanh toán...</h1>
      <p class="mt-2 text-sm text-gray-500">Vui lòng đợi trong giây lát, không tắt trang này.</p>
    </div>

    <div v-else-if="state === 'success'" class="rounded-2xl border border-emerald-100 bg-white p-10 text-center">
      <span class="mx-auto grid h-16 w-16 place-items-center rounded-full bg-emerald-100 text-emerald-600">
        <UiIcon name="check-circle" size="40" />
      </span>
      <h1 class="mt-6 text-2xl font-extrabold text-gray-700">Thanh toán thành công!</h1>
      <p class="mt-2 text-sm text-gray-500">Cảm ơn bạn. Hóa đơn của đơn hàng #{{ orderId }} đã được xác nhận.</p>
      <div class="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
        <NButton type="primary" size="large" @click="router.push('/')">
          <template #icon><UiIcon name="home" size="18" /></template>
          Về trang chủ
        </NButton>
        <NButton secondary size="large" @click="router.push(`/orders/${orderId}`)">
          <template #icon><UiIcon name="receipt" size="18" /></template>
          Xem hóa đơn
        </NButton>
      </div>
    </div>

    <div v-else-if="state === 'cancelled'" class="rounded-2xl border border-amber-100 bg-white p-10 text-center">
      <span class="mx-auto grid h-16 w-16 place-items-center rounded-full bg-amber-100 text-amber-600">
        <UiIcon name="times-circle" size="40" />
      </span>
      <h1 class="mt-6 text-2xl font-extrabold text-gray-700">Thanh toán đã bị hủy</h1>
      <p class="mt-2 text-sm text-gray-500">Bạn chưa hoàn tất thanh toán. Đơn hàng vẫn được giữ lại trên hệ thống.</p>
      <div class="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
        <NButton type="primary" size="large" @click="router.push('/cart')">
          <template #icon><UiIcon name="shopping-cart" size="18" /></template>
          Quay lại giỏ hàng
        </NButton>
        <NButton v-if="orderId" secondary size="large" @click="router.push(`/orders/${orderId}`)">
          <template #icon><UiIcon name="receipt" size="18" /></template>
          Xem đơn hàng
        </NButton>
      </div>
    </div>

    <div v-else class="rounded-2xl border border-red-100 bg-white p-10 text-center">
      <span class="mx-auto grid h-16 w-16 place-items-center rounded-full bg-red-100 text-red-600">
        <UiIcon name="exclamation-circle" size="40" />
      </span>
      <h1 class="mt-6 text-2xl font-extrabold text-gray-700">Không thể xác nhận thanh toán</h1>
      <p class="mt-2 text-sm text-gray-500">
        {{ orderId ? 'Hệ thống chưa nhận được thông tin thanh toán. Bạn có thể thử lại hoặc xem đơn hàng sau.' : 'Thiếu thông tin thanh toán.' }}
      </p>
      <div class="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
        <NButton v-if="orderId" type="primary" size="large" @click="confirmPayment">
          <template #icon><UiIcon name="refresh" size="18" /></template>
          Thử lại
        </NButton>
        <NButton secondary size="large" @click="router.push(orderId ? `/orders/${orderId}` : '/cart')">
          <template #icon><UiIcon name="receipt" size="18" /></template>
          {{ orderId ? 'Xem đơn hàng' : 'Về giỏ hàng' }}
        </NButton>
      </div>
    </div>
  </div>
</template>
