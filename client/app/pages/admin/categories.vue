<script setup lang="ts">
definePageMeta({ middleware: 'admin', layout: 'admin' })

import type { CategoryRequest, CategoryResponse } from '~/types'
import { categorySchema } from '~/schemas'

const { request } = useApi()
const { errors, applyIssues, clearErrors } = useFormErrors()
const { confirm } = useConfirm()
const toast = useToast()

const categories = ref<CategoryResponse[]>([])
const loading = ref(true)
const busyIds = ref<Set<number>>(new Set())

const categoryIconMap: Record<string, string> = {
  leaf: 'i-ph-leaf',
  apple: 'i-ph-apple-logo',
  box: 'i-ph-package',
  carrot: 'i-ph-carrot',
  tag: 'i-ph-tag-simple'
}

function categoryIcon(icon?: string | null) {
  if (!icon) return 'i-ph-tag-simple'
  return categoryIconMap[icon] || `i-ph-${icon}`
}

const showForm = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const form = reactive({ name: '', slug: '', icon: '', parentId: null as number | null, displayOrder: 0, active: true })

async function load() {
  loading.value = true
  try {
    categories.value = await request<CategoryResponse[]>('/api/categories')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { name: '', slug: '', icon: '', parentId: null, displayOrder: 0, active: true })
  clearErrors()
  showForm.value = true
}

function openEdit(c: CategoryResponse) {
  editingId.value = c.id
  Object.assign(form, { name: c.name, slug: c.slug, icon: c.icon || '', parentId: null, displayOrder: c.displayOrder, active: c.active })
  clearErrors()
  showForm.value = true
}

async function submit() {
  clearErrors()
  const result = categorySchema.safeParse(form)
  if (!result.success) {
    applyIssues(result.error)
    return
  }
  saving.value = true
  const payload: CategoryRequest = { name: form.name, slug: form.slug, icon: form.icon, displayOrder: Number(form.displayOrder), active: form.active }
  try {
    if (editingId.value) {
      await request(`/api/categories/${editingId.value}`, { method: 'PUT', body: payload })
    } else {
      await request('/api/categories', { method: 'POST', body: payload })
    }
    toast.add({ title: 'Đã lưu danh mục', color: 'success' })
    showForm.value = false
    await load()
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Lưu thất bại', color: 'error' })
  } finally {
    saving.value = false
  }
}

async function remove(c: CategoryResponse) {
  if (busyIds.value.has(c.id)) return
  if (!await confirm(`Xóa danh mục "${c.name}"?`, 'Xóa danh mục')) return
  busyIds.value.add(c.id)
  try {
    await request(`/api/categories/${c.id}`, { method: 'DELETE' })
    toast.add({ title: 'Đã xóa danh mục', color: 'success' })
    await load()
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Không thể xóa danh mục', color: 'error' })
  } finally {
    busyIds.value.delete(c.id)
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="mb-6 flex flex-wrap items-center justify-between gap-3">
      <h1 class="text-xl font-bold text-gray-800">Quản lý danh mục</h1>
      <UButton color="primary" icon="i-ph-plus" label="Thêm danh mục" @click="openCreate" />
    </div>

    <div v-if="showForm" class="mb-6 rounded-2xl border border-emerald-100 bg-white p-6">
      <h2 class="mb-4 font-bold text-gray-800">{{ editingId ? 'Chỉnh sửa danh mục' : 'Thêm danh mục mới' }}</h2>
      <form class="grid gap-4 md:grid-cols-3" @submit.prevent="submit">
        <div>
          <label class="mb-1 block text-sm text-gray-500">Tên</label>
          <UInput v-model="form.name" />
          <p v-if="errors.name" class="text-xs text-red-600">{{ errors.name }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Slug</label>
          <UInput v-model="form.slug" />
          <p v-if="errors.slug" class="text-xs text-red-600">{{ errors.slug }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Icon (tên icon phosphor)</label>
          <UInput v-model="form.icon" placeholder="ex: carrot, apple, tag-simple" />
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Thứ tự</label>
          <UInput v-model="form.displayOrder" type="number" />
        </div>
        <div class="flex items-end pb-1">
          <UCheckbox v-model="form.active" label="Hiển thị" />
        </div>
        <div class="flex items-end justify-end gap-2 md:col-span-2">
          <UButton color="neutral" variant="ghost" label="Hủy" @click="showForm = false" />
          <UButton type="submit" color="primary" label="Lưu" :loading="saving" />
        </div>
      </form>
    </div>

    <div class="overflow-hidden rounded-2xl border border-gray-200 bg-white">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 text-left text-xs uppercase text-gray-400">
          <tr>
            <th class="px-4 py-3">Danh mục</th>
            <th class="px-4 py-3">Slug</th>
            <th class="px-4 py-3">Cấp</th>
            <th class="px-4 py-3">Trạng thái</th>
            <th class="px-4 py-3 text-right">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <template v-for="c in categories" :key="c.id">
            <tr class="border-t border-gray-100">
              <td class="px-4 py-3">
                <div class="flex items-center gap-2">
                  <UIcon v-if="c.icon && categoryIcon(c.icon) !== 'i-ph-tag-simple'" :name="categoryIcon(c.icon)" class="h-4 w-4 text-emerald-600" />
                  <span class="font-medium text-gray-800">{{ c.name }}</span>
                </div>
              </td>
              <td class="px-4 py-3 text-gray-500">{{ c.slug }}</td>
              <td class="px-4 py-3"><UBadge color="neutral" label="Cấp 1" size="sm" /></td>
              <td class="px-4 py-3"><UBadge :color="c.active ? 'success' : 'neutral'" :label="c.active ? 'Hiển thị' : 'Ẩn'" size="sm" /></td>
              <td class="px-4 py-3">
                <div class="flex justify-end gap-1">
                  <UButton color="neutral" variant="ghost" icon="i-ph-pencil-simple" @click="openEdit(c)" />
                  <UButton color="neutral" variant="ghost" icon="i-ph-trash" :loading="busyIds.has(c.id)" @click="remove(c)" />
                </div>
              </td>
            </tr>
            <tr v-for="child in c.children" :key="child.id" class="border-t border-gray-50 bg-gray-50/50">
              <td class="px-4 py-3 pl-8">
                <span class="flex items-center gap-2 text-gray-700">
                  <UIcon name="i-ph-corner-down-right" class="h-4 w-4 text-emerald-500" />
                  {{ child.name }}
                </span>
              </td>
              <td class="px-4 py-3 text-gray-500">{{ child.slug }}</td>
              <td class="px-4 py-3"><UBadge color="neutral" variant="soft" label="Cấp 2" size="sm" /></td>
              <td class="px-4 py-3"><UBadge :color="child.active ? 'success' : 'neutral'" :label="child.active ? 'Hiển thị' : 'Ẩn'" size="sm" /></td>
              <td class="px-4 py-3">
                <div class="flex justify-end gap-1">
                  <UButton color="neutral" variant="ghost" icon="i-ph-pencil-simple" @click="openEdit(child)" />
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>
  </div>
</template>