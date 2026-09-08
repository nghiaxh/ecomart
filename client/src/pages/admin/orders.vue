<script setup lang="ts">

import type { Order, PageResponse } from '@/types'
import { useApi } from '@/composables/useApi'
import { useFormat } from '@/composables/useFormat'
import { useStatusLabels } from '@/composables/useStatusLabels'

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
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <div class="mb-5 flex items-center justify-between gap-3">
      <div v-if="!loading && orders.length" class="text-sm text-gray-500">Hiển thị <span class="font-semibold tabular-nums">{{ orders.length }}</span> đơn gần nhất</div>
      <div class="ml-auto">
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
      <div v-for="o in orders" :key="o.id" class="rounded-2xl border border-gray-200 bg-white p-5 transition hover:border-gray-300">
        <div class="flex flex-wrap items-center justify-between gap-4">
          <div>
            <div class="flex items-center gap-2">
              <span class="font-bold text-gray-700 tabular-nums">Đơn #{{ o.id }}</span>
              <UBadge :color="orderStatus[o.status].color" :label="orderStatus[o.status].label" size="sm" />
              <UBadge :color="paymentStatus[o.payment.status].color" :label="paymentStatus[o.payment.status].label" size="sm" variant="soft" />
            </div>
            <p class="mt-1 text-xs text-gray-400">{{ formatDate(o.createdAt) }} · {{ o.receiverName }} · {{ o.receiverPhone }}</p>
            <p class="mt-1 text-xs text-gray-500">{{ o.address }}</p>
          </div>
          <div class="flex flex-col items-end gap-2">
            <span class="text-lg font-bold text-emerald-700 tabular-nums">{{ formatVND(o.total) }}</span>
            <div class="flex gap-2">
              <USelect
                :model-value="o.status"
                :items="Object.entries(orderStatus).map(([k, v]) => ({ label: v.label, value: k }))"
                label-key="label"
                value-key="value"
                size="sm"
                class="w-40"
                :disabled="busyIds.has(o.id)"
                @update:model-value="(v: string | null) => updateStatus(o, v ?? '')"
              />
              <UButton v-if="o.payment.status === 'PENDING'" color="primary" variant="soft" size="md" label="Xác nhận thanh toán" :loading="busyIds.has(o.id)" @click="confirmPayment(o)" />
            </div>
          </div>
        </div>
      </div>
      <div v-if="!loading && !orders.length" class="rounded-2xl border border-dashed border-gray-200 py-16 text-center">
        <UIcon name="i-ph-tray" class="mx-auto h-8 w-8 text-gray-300" />
        <p class="mt-3 text-sm text-gray-400">Không có đơn hàng.</p>
      </div>
      <div v-if="loadError" class="rounded-2xl border border-dashed border-gray-200 py-16 text-center">
        <UIcon name="i-ph-warning-circle" class="mx-auto h-8 w-8 text-gray-300" />
        <p class="mt-3 text-sm text-gray-400">Không thể tải đơn hàng. Vui lòng thử lại.</p>
      </div>
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