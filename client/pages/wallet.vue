<script setup lang="ts">
import type { EcoWallet } from '~/types'

definePageMeta({ middleware: 'auth' })

const { request } = useApi()
const { formatVND, formatDate } = useFormat()
const { pointType } = useStatusLabels()

const wallet = ref<EcoWallet | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    wallet.value = await request<EcoWallet>('/api/wallet')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="mx-auto max-w-4xl px-4 py-8 sm:px-6">
    <h1 class="text-3xl font-extrabold text-gray-800">Ví Eco Points</h1>
    <p class="mt-1 text-gray-500">Tích lũy điểm từ mỗi sản phẩm xanh bạn mua</p>

    <div v-if="wallet" class="mt-8">
      <div class="grid gap-4 sm:grid-cols-3">
        <div class="rounded-2xl bg-gradient-to-br from-green-600 to-green-500 p-6 text-white sm:col-span-2">
          <p class="text-sm font-medium text-white/80">Số dư hiện tại</p>
          <p class="mt-2 text-5xl font-extrabold">{{ wallet.balance }}</p>
          <p class="mt-1 text-sm text-white/70">Eco Points</p>
        </div>
        <div class="rounded-2xl border border-green-100 bg-white p-6">
          <p class="text-sm text-gray-500">Tổng đã nhận</p>
          <p class="mt-2 text-3xl font-extrabold text-green-700">{{ wallet.totalEarned }}</p>
          <p class="mt-1 text-xs text-gray-400">điểm</p>
        </div>
      </div>

      <section class="mt-8 rounded-2xl border border-green-100 bg-white p-6">
        <h2 class="text-lg font-bold text-gray-800">Lịch sử giao dịch</h2>
        <div v-if="wallet.transactions.length" class="mt-4 space-y-3">
          <div v-for="t in wallet.transactions" :key="t.id" class="flex items-center justify-between rounded-xl border border-gray-50 p-4">
            <div class="flex items-center gap-3">
              <span class="grid h-9 w-9 place-items-center rounded-xl" :class="t.type === 'EARN' ? 'bg-green-100 text-green-700' : 'bg-orange-100 text-orange-600'">
                <UIcon :name="t.type === 'EARN' ? 'i-heroicons-arrow-down-left' : 'i-heroicons-arrow-up-right'" class="h-5 w-5" />
              </span>
              <div>
                <p class="text-sm font-medium text-gray-800">{{ t.description }}</p>
                <p class="text-xs text-gray-400">{{ formatDate(t.createdAt) }}</p>
              </div>
            </div>
            <span class="text-lg font-bold" :class="t.type === 'EARN' ? 'text-green-600' : 'text-orange-600'">
              {{ t.type === 'EARN' ? '+' : '−' }}{{ t.amount }}
            </span>
          </div>
        </div>
        <p v-else class="py-8 text-center text-gray-400">Chưa có giao dịch nào.</p>
      </section>
    </div>

    <div v-else-if="loading" class="mt-8 space-y-4">
      <USkeleton class="h-40 rounded-2xl" />
      <USkeleton class="h-48 rounded-2xl" />
    </div>
  </div>
</template>