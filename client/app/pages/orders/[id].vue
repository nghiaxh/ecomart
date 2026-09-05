<script setup lang="ts">
import type { Order } from '~/types'

definePageMeta({ middleware: 'customer' })

const { request } = useApi()
const { formatVND, formatDate } = useFormat()
const { orderStatus, paymentStatus, paymentMethod } = useStatusLabels()
const route = useRoute()

const order = ref<Order | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    order.value = await request<Order>(`/api/orders/${route.params.id}`)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="mx-auto max-w-3xl px-4 py-8 sm:px-6">
    <div v-if="order">
      <div class="flex items-center gap-3">
        <UButton icon="i-ph-arrow-left" color="neutral" variant="ghost" to="/orders" />
        <div>
          <h1 class="text-2xl font-extrabold text-gray-800">Đơn hàng #{{ order.id }}</h1>
          <p class="text-sm text-gray-400">{{ formatDate(order.createdAt) }}</p>
        </div>
      </div>

      <div class="mt-8 grid gap-6 md:grid-cols-2">
        <section class="rounded-2xl border border-emerald-100 bg-white p-6">
          <h2 class="font-semibold text-gray-800">Trạng thái</h2>
          <div class="mt-3 space-y-3">
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">Đơn hàng:</span>
              <UBadge :color="orderStatus[order.status].color" :label="orderStatus[order.status].label" />
            </div>
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">Thanh toán:</span>
              <span class="text-sm font-medium">{{ paymentStatus[order.payment.status].label }}</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">Phương thức:</span>
              <span class="text-sm font-medium">{{ paymentMethod[order.payment.method] }}</span>
            </div>
          </div>
        </section>

        <section class="rounded-2xl border border-emerald-100 bg-white p-6">
          <h2 class="font-semibold text-gray-800">Giao hàng đến</h2>
          <div class="mt-3 space-y-1 text-sm">
            <p class="font-medium text-gray-700">{{ order.receiverName }} · {{ order.receiverPhone }}</p>
            <p class="text-gray-500">{{ order.address }}</p>
          </div>
        </section>
      </div>

      <section class="mt-6 rounded-2xl border border-emerald-100 bg-white p-6">
        <h2 class="font-semibold text-gray-800">Sản phẩm</h2>
        <div class="mt-4 space-y-4">
          <div v-for="item in order.items" :key="item.productId" class="flex items-center gap-4">
            <img :src="item.imageUrl" :alt="item.productName" class="h-16 w-16 rounded-xl object-cover" />
            <div class="flex-1">
              <p class="font-medium text-gray-800">{{ item.productName }}</p>
              <p class="text-sm text-gray-400">{{ formatVND(item.unitPrice) }} × {{ item.quantity }}</p>
            </div>
            <span class="font-semibold text-gray-700">{{ formatVND(item.unitPrice * item.quantity) }}</span>
          </div>
        </div>
        <div class="mt-6 space-y-2 border-t border-emerald-50 pt-4 text-sm">
          <div class="flex justify-between text-gray-500"><span>Tạm tính</span><span>{{ formatVND(order.subtotal) }}</span></div>
          <div class="flex justify-between text-gray-500"><span>Phí giao hàng</span><span>{{ formatVND(order.shippingFee) }}</span></div>
          <div class="flex justify-between text-lg pt-2"><span class="font-semibold text-gray-700">Tổng cộng</span><span class="font-bold text-emerald-700">{{ formatVND(order.total) }}</span></div>
        </div>
        <p v-if="order.notes" class="mt-4 rounded-xl bg-emerald-50 p-3 text-sm text-gray-600">
          <span class="font-medium">Ghi chú:</span> {{ order.notes }}
        </p>
      </section>
    </div>

    <div v-else-if="loading" class="py-12">
      <USkeleton class="h-64 rounded-2xl" />
    </div>
    <div v-else class="py-24 text-center text-gray-400">Không tìm thấy đơn hàng.</div>
  </div>
</template>