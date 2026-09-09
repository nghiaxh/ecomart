<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { Order, PageResponse } from '@/types'
import { useApi } from '@/composables/useApi'
import { useFormat } from '@/composables/useFormat'
import { useStatusLabels } from '@/composables/useStatusLabels'
import UiImg from '@/components/UiImg.vue'
import UiIcon from '@/components/UiIcon.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import { NButton, NSkeleton, NTag } from 'naive-ui'

const { request } = useApi()
const { formatVND, formatDate } = useFormat()
const { orderStatus, paymentStatus, badgeType } = useStatusLabels()

const router = useRouter()
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
      <NSkeleton v-for="i in 3" :key="i" class="h-36 rounded-2xl" />
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
              <UiIcon name="receipt" size="18" />
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
                <NTag :type="badgeType[orderStatus[o.status].color]">{{ orderStatus[o.status].label }}</NTag>
                <NTag :type="badgeType[paymentStatus[o.payment.status].color]">{{ paymentStatus[o.payment.status].label }}</NTag>
              </div>
            </div>
            <UiIcon name="caret-right" size="18" class="text-gray-300 transition group-hover:text-emerald-500" />
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
        <UiIcon name="receipt" size="40" class="text-emerald-300" />
      </span>
      <p class="text-lg font-semibold text-gray-600">Bạn chưa có đơn hàng nào</p>
      <p class="mt-1 text-sm text-gray-400">Hãy bắt đầu mua sắm các sản phẩm tươi ngon nhé!</p>
      <NButton type="primary" size="large" class="mt-6" @click="router.push('/products')">
        <template #icon><UiIcon name="shopping-bag" size="18" /></template>
        Mua sắm ngay
      </NButton>
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
