<script setup lang="ts">
definePageMeta({ middleware: 'customer' })

const route = useRoute()
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
    toast.add({ title: error?.data?.message || 'Không thể xác nhận thanh toán', color: 'error' })
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
        <UIcon name="i-ph-circle-notch" class="h-8 w-8 animate-spin" />
      </span>
      <h1 class="mt-6 text-xl font-extrabold text-gray-800">Đang xác nhận thanh toán...</h1>
      <p class="mt-2 text-sm text-gray-500">Vui lòng đợi trong giây lát, không tắt trang này.</p>
    </div>

    <div v-else-if="state === 'success'" class="rounded-2xl border border-emerald-100 bg-white p-10 text-center">
      <span class="mx-auto grid h-16 w-16 place-items-center rounded-full bg-emerald-100 text-emerald-600">
        <UIcon name="i-ph-check-fat" class="h-8 w-8" />
      </span>
      <h1 class="mt-6 text-2xl font-extrabold text-gray-800">Thanh toán thành công!</h1>
      <p class="mt-2 text-sm text-gray-500">Cảm ơn bạn. Hóa đơn của đơn hàng #{{ orderId }} đã được xác nhận.</p>
      <div class="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
        <UButton color="primary" size="lg" to="/" icon="i-ph-house" label="Về trang chủ" />
        <UButton color="secondary" size="lg" :to="`/orders/${orderId}`" icon="i-ph-receipt" label="Xem hóa đơn" />
      </div>
    </div>

    <div v-else-if="state === 'cancelled'" class="rounded-2xl border border-amber-100 bg-white p-10 text-center">
      <span class="mx-auto grid h-16 w-16 place-items-center rounded-full bg-amber-100 text-amber-600">
        <UIcon name="i-ph-x-circle" class="h-8 w-8" />
      </span>
      <h1 class="mt-6 text-2xl font-extrabold text-gray-800">Thanh toán đã bị hủy</h1>
      <p class="mt-2 text-sm text-gray-500">Bạn chưa hoàn tất thanh toán. Đơn hàng vẫn được giữ lại trên hệ thống.</p>
      <div class="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
        <UButton color="primary" size="lg" to="/cart" icon="i-ph-shopping-cart" label="Quay lại giỏ hàng" />
        <UButton v-if="orderId" color="secondary" size="lg" :to="`/orders/${orderId}`" icon="i-ph-receipt" label="Xem đơn hàng" />
      </div>
    </div>

    <div v-else class="rounded-2xl border border-red-100 bg-white p-10 text-center">
      <span class="mx-auto grid h-16 w-16 place-items-center rounded-full bg-red-100 text-red-600">
        <UIcon name="i-ph-warning-circle" class="h-8 w-8" />
      </span>
      <h1 class="mt-6 text-2xl font-extrabold text-gray-800">Không thể xác nhận thanh toán</h1>
      <p class="mt-2 text-sm text-gray-500">
        {{ orderId ? 'Hệ thống chưa nhận được thông tin thanh toán. Bạn có thể thử lại hoặc xem đơn hàng sau.' : 'Thiếu thông tin thanh toán.' }}
      </p>
      <div class="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
        <UButton v-if="orderId" color="primary" size="lg" icon="i-ph-arrow-clockwise" label="Thử lại" @click="confirmPayment" />
        <UButton color="secondary" size="lg" :to="orderId ? `/orders/${orderId}` : '/cart'" icon="i-ph-receipt" :label="orderId ? 'Xem đơn hàng' : 'Về giỏ hàng'" />
      </div>
    </div>
  </div>
</template>