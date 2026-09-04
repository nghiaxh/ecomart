<script setup lang="ts">
definePageMeta({ middleware: 'admin', layout: 'admin' })

import type { Banner, BannerRequest } from '~/types'
import { bannerSchema } from '~/schemas'

const { request } = useApi()
const toast = useToast()

const banners = ref<Banner[]>([])
const loading = ref(true)
const busyIds = ref<Set<number>>(new Set())

const showForm = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const form = reactive({ title: '', subtitle: '', imageUrl: '', linkUrl: '', displayOrder: 0, active: true })
const errors = ref<Record<string, string>>({})

async function load() {
  loading.value = true
  try {
    banners.value = await request<Banner[]>('/api/banners')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { title: '', subtitle: '', imageUrl: '', linkUrl: '', displayOrder: 0, active: true })
  errors.value = {}
  showForm.value = true
}

function openEdit(b: Banner) {
  editingId.value = b.id
  Object.assign(form, { title: b.title, subtitle: b.subtitle || '', imageUrl: b.imageUrl, linkUrl: b.linkUrl || '', displayOrder: b.displayOrder, active: b.active })
  errors.value = {}
  showForm.value = true
}

async function submit() {
  errors.value = {}
  const result = bannerSchema.safeParse(form)
  if (!result.success) {
    for (const issue of result.error.issues) errors.value[String(issue.path[0])] = issue.message
    return
  }
  saving.value = true
  const payload: BannerRequest = { title: form.title, subtitle: form.subtitle, imageUrl: form.imageUrl, linkUrl: form.linkUrl, displayOrder: Number(form.displayOrder || 0), active: form.active }
  try {
    if (editingId.value) {
      await request(`/api/banners/${editingId.value}`, { method: 'PUT', body: payload })
    } else {
      await request('/api/banners', { method: 'POST', body: payload })
    }
    toast.add({ title: 'Đã lưu banner', color: 'success' })
    showForm.value = false
    await load()
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Lưu thất bại', color: 'error' })
  } finally {
    saving.value = false
  }
}

async function remove(b: Banner) {
  if (busyIds.value.has(b.id)) return
  if (!confirm(`Xóa banner "${b.title}"?`)) return
  busyIds.value.add(b.id)
  try {
    await request(`/api/banners/${b.id}`, { method: 'DELETE' })
    toast.add({ title: 'Đã xóa banner', color: 'success' })
    await load()
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Không thể xóa banner', color: 'error' })
  } finally {
    busyIds.value.delete(b.id)
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="mb-6 flex flex-wrap items-center justify-between gap-3">
      <h1 class="text-xl font-bold text-gray-800">Quản lý banner</h1>
      <UButton color="primary" icon="i-ph-plus" label="Thêm banner" @click="openCreate" />
    </div>

    <div v-if="showForm" class="mb-6 rounded-2xl border border-emerald-100 bg-white p-6">
      <h2 class="mb-4 font-bold text-gray-800">{{ editingId ? 'Chỉnh sửa banner' : 'Thêm banner mới' }}</h2>
      <form class="grid gap-4 md:grid-cols-2" @submit.prevent="submit">
        <div>
          <label class="mb-1 block text-sm text-gray-500">Tiêu đề</label>
          <UInput v-model="form.title" />
          <p v-if="errors.title" class="text-xs text-red-600">{{ errors.title }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Phụ đề</label>
          <UInput v-model="form.subtitle" />
        </div>
        <div class="md:col-span-2">
          <label class="mb-1 block text-sm text-gray-500">URL hình ảnh</label>
          <UInput v-model="form.imageUrl" placeholder="https://..." />
          <p v-if="errors.imageUrl" class="text-xs text-red-600">{{ errors.imageUrl }}</p>
        </div>
        <div class="md:col-span-2">
          <label class="mb-1 block text-sm text-gray-500">Liên kết (tùy chọn)</label>
          <UInput v-model="form.linkUrl" placeholder="/products" />
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Thứ tự</label>
          <UInput v-model="form.displayOrder" type="number" />
        </div>
        <div class="flex items-end pb-1">
          <UCheckbox v-model="form.active" label="Hiển thị" />
        </div>
        <div v-if="form.imageUrl" class="md:col-span-2">
          <img :src="form.imageUrl" alt="preview" class="h-32 w-full rounded-xl object-cover" />
        </div>
        <div class="flex justify-end gap-2 md:col-span-2">
          <UButton color="neutral" variant="ghost" label="Hủy" @click="showForm = false" />
          <UButton type="submit" color="primary" label="Lưu" :loading="saving" />
        </div>
      </form>
    </div>

    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <div v-for="b in banners" :key="b.id" class="overflow-hidden rounded-2xl border border-gray-200 bg-white">
        <img :src="b.imageUrl" :alt="b.title" class="h-40 w-full object-cover" @error="($event.target as HTMLImageElement).src = '/images/placeholder.svg'" />
        <div class="p-4">
          <div class="flex items-center justify-between">
            <h3 class="font-bold text-gray-800">{{ b.title }}</h3>
            <UBadge :color="b.active ? 'success' : 'neutral'" :label="b.active ? 'Hiển thị' : 'Ẩn'" size="sm" />
          </div>
          <p v-if="b.subtitle" class="mt-1 text-sm text-gray-400">{{ b.subtitle }}</p>
          <div class="mt-3 flex justify-end gap-1">
            <UButton color="neutral" variant="ghost" icon="i-ph-pencil-simple" @click="openEdit(b)" />
            <UButton color="neutral" variant="ghost" icon="i-ph-trash" :loading="busyIds.has(b.id)" @click="remove(b)" />
          </div>
        </div>
      </div>
      <p v-if="!loading && !banners.length" class="col-span-full py-16 text-center text-gray-400">Chưa có banner.</p>
    </div>
  </div>
</template>