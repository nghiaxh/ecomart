<script setup lang="ts">
definePageMeta({ middleware: 'admin', layout: 'admin' })

import type { CategoryResponse, PageResponse, Product, ProductRequest } from '~/types'
import { productSchema } from '~/schemas'

const { request } = useApi()
const { formatVND } = useFormat()
const toast = useToast()

const products = ref<Product[]>([])
const categories = ref<CategoryResponse[]>([])
const page = ref(0)
const totalPages = ref(0)
const search = ref('')
const loading = ref(false)
const busyIds = ref<Set<number>>(new Set())

const showForm = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const form = reactive({
  name: '', slug: '', description: '', price: 0, stock: 0,
  weight: 0, origin: '', categoryId: 0, active: true, imageUrl: ''
})
const errors = ref<Record<string, string>>({})

async function loadCats() {
  categories.value = await request<CategoryResponse[]>('/api/categories')
}

async function load() {
  loading.value = true
  try {
    const params = new URLSearchParams()
    params.set('showAll', 'true')
    if (search.value) params.set('q', search.value)
    params.set('page', String(page.value))
    params.set('size', '10')
    const data = await request<PageResponse<Product>>(`/api/products?${params.toString()}`)
    products.value = data.content
    totalPages.value = data.totalPages
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { name: '', slug: '', description: '', price: 0, stock: 0, weight: 0, origin: '', categoryId: 0, active: true, imageUrl: '' })
  errors.value = {}
  showForm.value = true
}

function openEdit(p: Product) {
  editingId.value = p.id
  Object.assign(form, {
    name: p.name, slug: p.slug, description: p.description || '', price: p.price, stock: p.stock,
    weight: p.weight, origin: p.origin || '', categoryId: p.categoryId, active: p.active,
    imageUrl: p.images?.[0] || ''
  })
  errors.value = {}
  showForm.value = true
}

async function submit() {
  errors.value = {}
  const result = productSchema.safeParse(form)
  if (!result.success) {
    for (const issue of result.error.issues) errors.value[String(issue.path[0])] = issue.message
    return
  }
  saving.value = true
  const payload: ProductRequest = {
    name: form.name, slug: form.slug, description: form.description, price: Number(form.price), stock: Number(form.stock),
    weight: Number(form.weight || 0), origin: form.origin,
    categoryId: Number(form.categoryId), active: form.active,
    images: form.imageUrl ? [{ url: form.imageUrl, primary: true }] : []
  }
  try {
    if (editingId.value) {
      await request(`/api/products/${editingId.value}`, { method: 'PUT', body: payload })
    } else {
      await request('/api/products', { method: 'POST', body: payload })
    }
    toast.add({ title: 'Đã lưu sản phẩm', color: 'success' })
    showForm.value = false
    await load()
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Lưu thất bại', color: 'error' })
  } finally {
    saving.value = false
  }
}

async function toggle(p: Product) {
  if (busyIds.value.has(p.id)) return
  busyIds.value.add(p.id)
  try {
    await request(`/api/products/${p.id}/toggle`, { method: 'PATCH' })
    await load()
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Không thể cập nhật trạng thái', color: 'error' })
  } finally {
    busyIds.value.delete(p.id)
  }
}

async function remove(p: Product) {
  if (busyIds.value.has(p.id)) return
  if (!confirm(`Xóa sản phẩm "${p.name}"?`)) return
  busyIds.value.add(p.id)
  try {
    await request(`/api/products/${p.id}`, { method: 'DELETE' })
    toast.add({ title: 'Đã xóa sản phẩm', color: 'success' })
    await load()
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Không thể xóa sản phẩm', color: 'error' })
  } finally {
    busyIds.value.delete(p.id)
  }
}

const debouncedLoad = useDebounceFn(load, 400)

onMounted(() => { loadCats(); load() })
watch(search, debouncedLoad)
</script>

<template>
  <div>
    <div class="mb-6 flex flex-wrap items-center justify-between gap-3">
      <h1 class="text-xl font-bold text-gray-800">Quản lý sản phẩm</h1>
      <UButton color="primary" icon="i-ph-plus" label="Thêm sản phẩm" @click="openCreate" />
    </div>

    <div v-if="showForm" class="mb-6 rounded-2xl border border-emerald-100 bg-white p-6">
      <h2 class="mb-4 font-bold text-gray-800">{{ editingId ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới' }}</h2>
      <form class="grid gap-4 md:grid-cols-3" @submit.prevent="submit">
        <div class="md:col-span-1">
          <label class="mb-1 block text-sm text-gray-500">Tên</label>
          <UInput v-model="form.name" />
          <p v-if="errors.name" class="text-xs text-red-600">{{ errors.name }}</p>
        </div>
        <div class="md:col-span-1">
          <label class="mb-1 block text-sm text-gray-500">Slug</label>
          <UInput v-model="form.slug" />
          <p v-if="errors.slug" class="text-xs text-red-600">{{ errors.slug }}</p>
        </div>
        <div class="md:col-span-1">
          <label class="mb-1 block text-sm text-gray-500">Danh mục</label>
          <USelect v-model="form.categoryId" :items="categories.map(c => ({ label: c.name, value: c.id }))" label-key="label" value-key="value" />
          <p v-if="errors.categoryId" class="text-xs text-red-600">{{ errors.categoryId }}</p>
        </div>
        <div class="md:col-span-1">
          <label class="mb-1 block text-sm text-gray-500">Giá (₫)</label>
          <UInput v-model="form.price" type="number" />
          <p v-if="errors.price" class="text-xs text-red-600">{{ errors.price }}</p>
        </div>
        <div class="md:col-span-1">
          <label class="mb-1 block text-sm text-gray-500">Tồn kho</label>
          <UInput v-model="form.stock" type="number" />
          <p v-if="errors.stock" class="text-xs text-red-600">{{ errors.stock }}</p>
        </div>
        <div class="md:col-span-1">
          <label class="mb-1 block text-sm text-gray-500">Khối lượng (kg)</label>
          <UInput v-model="form.weight" type="number" step="0.1" />
        </div>
        <div class="md:col-span-1">
          <label class="mb-1 block text-sm text-gray-500">Xuất xứ</label>
          <UInput v-model="form.origin" />
        </div>
        <div class="md:col-span-1">
          <label class="mb-1 block text-sm text-gray-500">Hình ảnh URL</label>
          <UInput v-model="form.imageUrl" placeholder="https://..." />
        </div>
        <div class="md:col-span-3">
          <label class="mb-1 block text-sm text-gray-500">Mô tả</label>
          <UTextarea v-model="form.description" :rows="3" />
        </div>
        <div class="md:col-span-3">
          <UCheckbox v-model="form.active" label="Đang bán" />
        </div>
        <div class="md:col-span-3 flex justify-end gap-2">
          <UButton color="neutral" variant="ghost" label="Hủy" @click="showForm = false" />
          <UButton type="submit" color="primary" label="Lưu" :loading="saving" />
        </div>
      </form>
    </div>

    <div class="mb-4 flex items-center gap-3">
      <UInput v-model="search" icon="i-ph-magnifying-glass" placeholder="Tìm sản phẩm..." class="max-w-md" />
    </div>

    <div class="overflow-hidden rounded-2xl border border-gray-200 bg-white">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 text-left text-xs uppercase text-gray-400">
          <tr>
            <th class="px-4 py-3">Sản phẩm</th>
            <th class="px-4 py-3">Giá</th>
            <th class="px-4 py-3">Tồn kho</th>
            <th class="px-4 py-3">Trạng thái</th>
            <th class="px-4 py-3 text-right">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in products" :key="p.id" class="border-t border-gray-100">
            <td class="px-4 py-3">
              <div class="flex items-center gap-3">
                <img :src="p.images?.[0]" :alt="p.name" class="h-10 w-10 rounded-lg object-cover" @error="($event.target as HTMLImageElement).src = '/images/placeholder.svg'" />
                <span class="font-medium text-gray-800">{{ p.name }}</span>
              </div>
            </td>
            <td class="px-4 py-3 text-gray-600">{{ formatVND(p.price) }}</td>
            <td class="px-4 py-3 text-gray-600">{{ p.stock }}</td>
            <td class="px-4 py-3">
              <UBadge :color="p.active ? 'success' : 'neutral'" :label="p.active ? 'Bán' : 'Ẩn'" size="sm" />
            </td>
            <td class="px-4 py-3">
              <div class="flex justify-end gap-1">
                <UButton color="neutral" variant="ghost" icon="i-ph-pencil-simple" @click="openEdit(p)" />
                <UButton color="neutral" variant="ghost" :icon="p.active ? 'i-ph-eye-slash' : 'i-ph-eye'" :loading="busyIds.has(p.id)" @click="toggle(p)" />
                <UButton color="neutral" variant="ghost" icon="i-ph-trash" :loading="busyIds.has(p.id)" @click="remove(p)" />
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="totalPages > 1" class="mt-4 flex items-center justify-center gap-2">
      <UButton color="neutral" variant="soft" icon="i-ph-caret-left" :disabled="page === 0" @click="page--; load()" />
      <span class="px-3 text-sm">Trang {{ page + 1 }} / {{ totalPages }}</span>
      <UButton color="neutral" variant="soft" icon="i-ph-caret-right" :disabled="page >= totalPages - 1" @click="page++; load()" />
    </div>
  </div>
</template>