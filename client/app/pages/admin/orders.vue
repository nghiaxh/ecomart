<script setup lang="ts">
definePageMeta({ middleware: 'admin', layout: 'admin' })

import type { Order, PageResponse } from '~/types'

const { request } = useApi()
const { formatVND, formatDate } = useFormat()
const { orderStatus, paymentStatus } = useStatusLabels()
const toast = useToast()

const orders = ref<Order[]>([])
const page = ref(0)
const totalPages = ref(0)
const statusFilter = ref('ALL')
const loading = ref(false)
const loadError = ref(false)
const busyIds = ref<Set<number>>(new Set())

async function load() {
  loading.value = true
  loadError.value = false
  try {
    const params = new URLSearchParams()
    if (statusFilter.value && statusFilter.value !== 'ALL') params.set('status', statusFilter.value)
    params.set('page', String(page.value))
    params.set('size', '10')
    const data = await request<PageResponse<Order>>(`/api/orders?${params.toString()}`)
    orders.value = data.content
    totalPages.value = data.totalPages
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Không thể tải đơn hàng', color: 'error' })
    loadError.value = true
  } finally {
    loading.value = false
  }
}

async function updateStatus(o: Order, status: string) {
  if (busyIds.value.has(o.id)) return
  if (status === o.status) return
  busyIds.value.add(o.id)
  try {
    await request(`/api/orders/${o.id}/status`, { method: 'PATCH', body: { status } })
    toast.add({ title: 'Đã cập nhật trạng thái', color: 'success' })
    await load()
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Không thể cập nhật trạng thái', color: 'error' })
  } finally {
    busyIds.value.delete(o.id)
  }
}

async function confirmPayment(o: Order) {
  if (busyIds.value.has(o.id)) return
  busyIds.value.add(o.id)
  try {
    await request(`/api/orders/${o.id}/confirm-payment`, { method: 'POST' })
    toast.add({ title: 'Đã xác nhận thanh toán', color: 'success' })
    await load()
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Không thể xác nhận thanh toán', color: 'error' })
  } finally {
    busyIds.value.delete(o.id)
  }
}

onMounted(load)
watch(statusFilter, () => { page.value = 0; load() })
</script>

<template>
  <div>
    <div class="mb-6 flex flex-wrap items-center justify-between gap-3">
      <h1 class="text-xl font-bold text-gray-800">Quản lý đơn hàng</h1>
      <div class="flex items-center gap-2">
        <USelect
          v-model="statusFilter"
          :items="[{ label: 'Tất cả trạng thái', value: 'ALL' }, ...Object.entries(orderStatus).map(([k, v]) => ({ label: v.label, value: k }))]"
          label-key="label"
          value-key="value"
          class="w-48"
        />
      </div>
    </div>

    <div class="space-y-4">
      <div v-for="o in orders" :key="o.id" class="rounded-2xl border border-gray-200 bg-white p-5">
        <div class="flex flex-wrap items-center justify-between gap-4">
          <div>
            <div class="flex items-center gap-2">
              <span class="font-bold text-gray-800">Đơn #{{ o.id }}</span>
              <UBadge :color="orderStatus[o.status].color" :label="orderStatus[o.status].label" size="sm" />
              <UBadge :color="paymentStatus[o.payment.status].color" :label="paymentStatus[o.payment.status].label" size="sm" variant="soft" />
            </div>
            <p class="mt-1 text-xs text-gray-400">{{ formatDate(o.createdAt) }} · {{ o.receiverName }} · {{ o.receiverPhone }}</p>
            <p class="mt-1 text-xs text-gray-500">{{ o.address }}</p>
          </div>
          <div class="flex flex-col items-end gap-2">
            <span class="text-lg font-bold text-emerald-700">{{ formatVND(o.total) }}</span>
            <div class="flex gap-2">
              <USelect
                :model-value="o.status"
                :items="Object.entries(orderStatus).map(([k, v]) => ({ label: v.label, value: k }))"
                label-key="label"
                value-key="value"
                size="sm"
                class="w-40"
                :disabled="busyIds.has(o.id)"
                @update:model-value="(v) => updateStatus(o, v as string)"
              />
              <UButton v-if="o.payment.status === 'PENDING'" color="primary" variant="soft" size="md" label="Xác nhận thanh toán" :loading="busyIds.has(o.id)" @click="confirmPayment(o)" />
            </div>
          </div>
        </div>
      </div>
      <p v-if="(!loading && !orders.length) || loadError" class="py-16 text-center text-gray-400">Không có đơn hàng.</p>
    </div>

    <div v-if="totalPages > 1" class="mt-4 flex items-center justify-center gap-2">
      <UButton color="neutral" variant="soft" icon="i-ph-caret-left" :disabled="page === 0" @click="page--; load()" />
      <span class="px-3 text-sm">Trang {{ page + 1 }} / {{ totalPages }}</span>
      <UButton color="neutral" variant="soft" icon="i-ph-caret-right" :disabled="page >= totalPages - 1" @click="page++; load()" />
    </div>
  </div>
</template>