<script setup lang="ts">
import type { Order, PageResponse } from '@/types'
import { useApi } from '@/composables/useApi'
import { useFormat } from '@/composables/useFormat'
import { useStatusLabels } from '@/composables/useStatusLabels'

const { request } = useApi()
const { formatVND, formatDate } = useFormat()
const { orderStatus, paymentStatus } = useStatusLabels()

const orders = ref<Order[]>([])
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await request<PageResponse<Order>>(`/api/orders/mine?page=${page.value}&size=8`)
    orders.value = data.content
    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="mx-auto max-w-5xl px-4 py-8 sm:px-6">
    <div class="flex flex-wrap items-end justify-between gap-2">
      <h1 class="text-3xl font-extrabold text-gray-700">Đơn hàng của tôi</h1>
      <p v-if="!loading && totalElements > 0" class="text-sm text-gray-400">Tổng cộng {{ totalElements }} đơn</p>
    </div>

    <div v-if="loading" class="mt-8 space-y-4">
      <USkeleton v-for="i in 3" :key="i" class="h-36 rounded-2xl" />
    </div>

    <div v-else-if="orders.length" class="mt-8 space-y-4">
      <RouterLink
        v-for="o in orders"
        :key="o.id"
        :to="`/orders/${o.id}`"
        class="group block rounded-2xl border border-emerald-100 bg-white p-5 transition hover:border-emerald-300 hover:shadow-md"
      >
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div class="flex items-center gap-3">
            <span class="grid h-11 w-11 place-items-center rounded-xl bg-emerald-50 text-emerald-700">
              <UIcon name="i-ph-receipt" class="h-5 w-5" />
            </span>
            <div>
              <p class="font-bold text-gray-700">Đơn #{{ o.id }}</p>
              <p class="text-xs text-gray-400">{{ formatDate(o.createdAt) }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <div class="text-right">
              <p class="font-bold text-emerald-700">{{ formatVND(o.total) }}</p>
              <div class="mt-1 flex items-center justify-end gap-2">
                <UBadge :color="orderStatus[o.status].color" :label="orderStatus[o.status].label" size="sm" />
                <UBadge :color="paymentStatus[o.payment.status].color" :label="paymentStatus[o.payment.status].label" size="sm" variant="soft" />
              </div>
            </div>
            <UIcon name="i-ph-caret-right" class="h-5 w-5 text-gray-300 transition group-hover:text-emerald-500" />
          </div>
        </div>

        <div class="mt-3 flex items-center gap-2">
          <div class="flex gap-2 overflow-hidden">
            <UiImg v-for="item in o.items.slice(0, 4)" :key="item.productId" :src="item.imageUrl" :alt="item.productName" img-class="h-12 w-12 rounded-lg object-cover" />
            <span v-if="o.items.length > 4" class="grid h-12 w-12 place-items-center rounded-lg bg-emerald-50 text-xs font-semibold text-emerald-700">+{{ o.items.length - 4 }}</span>
          </div>
          <p class="ml-2 text-xs text-gray-400">{{ o.items.length }} món · {{ o.payment.method === 'PAYOS' ? 'PayOS QR' : 'COD' }}</p>
        </div>
      </RouterLink>
    </div>

    <div v-else class="py-24 text-center">
      <span class="mx-auto mb-4 grid h-20 w-20 place-items-center rounded-full bg-emerald-50">
        <UIcon name="i-ph-receipt" class="h-10 w-10 text-emerald-300" />
      </span>
      <p class="text-lg font-semibold text-gray-600">Bạn chưa có đơn hàng nào</p>
      <p class="mt-1 text-sm text-gray-400">Hãy bắt đầu mua sắm các sản phẩm tươi ngon nhé!</p>
      <UButton to="/products" color="primary" class="mt-6" label="Mua sắm ngay" icon="i-ph-shopping-bag" />
    </div>

    <PaginationBar
      v-if="totalPages > 1"
      :page="page"
      :total-pages="totalPages"
      @prev="page--; load()"
      @next="page++; load()"
    />
  </div>
</template>
