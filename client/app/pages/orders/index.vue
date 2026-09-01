<script setup lang="ts">
import type { Order, PageResponse } from '~/types'

definePageMeta({ middleware: 'auth' })

const { request } = useApi()
const { formatVND, formatDate } = useFormat()
const { orderStatus, paymentStatus } = useStatusLabels()

const orders = ref<Order[]>([])
const page = ref(0)
const totalPages = ref(0)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await request<PageResponse<Order>>(`/api/orders/mine?page=${page.value}&size=8`)
    orders.value = data.content
    totalPages.value = data.totalPages
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="mx-auto max-w-5xl px-4 py-8 sm:px-6">
    <h1 class="text-3xl font-extrabold text-gray-800">Đơn hàng của tôi</h1>

    <div v-if="loading" class="mt-8 space-y-4">
      <USkeleton v-for="i in 3" :key="i" class="h-32 rounded-2xl" />
    </div>

    <div v-else-if="orders.length" class="mt-8 space-y-4">
      <NuxtLink
        v-for="o in orders"
        :key="o.id"
        :to="`/orders/${o.id}`"
        class="block rounded-2xl border border-emerald-100 bg-white p-5 transition hover:border-emerald-300 hover:shadow-md"
      >
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p class="font-bold text-gray-800">Đơn #{{ o.id }}</p>
            <p class="text-sm text-gray-400">{{ formatDate(o.createdAt) }}</p>
          </div>
          <div class="text-right">
            <p class="font-bold text-emerald-700">{{ formatVND(o.total) }}</p>
            <div class="mt-1 flex items-center gap-2">
              <UBadge :color="orderStatus[o.status].color" :label="orderStatus[o.status].label" size="sm" />
              <span class="text-xs text-gray-400">{{ paymentStatus[o.payment.status].label }}</span>
            </div>
          </div>
        </div>
        <div class="mt-3 flex gap-2 overflow-hidden">
          <img v-for="item in o.items.slice(0, 4)" :key="item.productId" :src="item.imageUrl" :alt="item.productName" class="h-12 w-12 rounded-lg object-cover" />
          <span v-if="o.items.length > 4" class="grid h-12 w-12 place-items-center rounded-lg bg-emerald-50 text-xs font-semibold text-emerald-700">+{{ o.items.length - 4 }}</span>
        </div>
      </NuxtLink>
    </div>

    <div v-else class="py-24 text-center">
      <UIcon name="i-ph-receipt" class="mx-auto mb-4 h-16 w-16 text-emerald-200" />
      <p class="text-gray-500">Bạn chưa có đơn hàng nào.</p>
      <UButton to="/products" color="primary" class="mt-4" label="Mua sắm ngay" />
    </div>

    <div v-if="totalPages > 1" class="mt-8 flex items-center justify-center gap-2">
      <UButton color="neutral" variant="soft" icon="i-ph-caret-left" :disabled="page === 0" @click="page--; load()" />
      <span class="px-3 text-sm text-gray-600">Trang {{ page + 1 }} / {{ totalPages }}</span>
      <UButton color="neutral" variant="soft" icon="i-ph-caret-right" :disabled="page >= totalPages - 1" @click="page++; load()" />
    </div>
  </div>
</template>