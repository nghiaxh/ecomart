<script setup lang="ts">
definePageMeta({ middleware: 'admin', layout: 'admin' })

import type { CategoryRequest, CategoryResponse } from '~/types'
import { categorySchema } from '~/schemas'

const { request } = useApi()
const toast = useToast()

const categories = ref<CategoryResponse[]>([])
const loading = ref(true)

const showForm = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const form = reactive({ name: '', slug: '', icon: '', parentId: null as number | null, displayOrder: 0, active: true })
const errors = ref<Record<string, string>>({})

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
  errors.value = {}
  showForm.value = true
}

function openEdit(c: CategoryResponse) {
  editingId.value = c.id
  Object.assign(form, { name: c.name, slug: c.slug, icon: c.icon || '', parentId: null, displayOrder: c.displayOrder, active: c.active })
  errors.value = {}
  showForm.value = true
}

async function submit() {
  errors.value = {}
  const result = categorySchema.safeParse(form)
  if (!result.success) {
    for (const issue of result.error.issues) errors.value[String(issue.path[0])] = issue.message
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
    toast.add({ title: 'Đã lưu danh mục', color: 'green' })
    showForm.value = false
    await load()
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Lưu thất bại', color: 'red' })
  } finally {
    saving.value = false
  }
}

async function remove(c: CategoryResponse) {
  if (!confirm(`Xóa danh mục "${c.name}"?`)) return
  await request(`/api/categories/${c.id}`, { method: 'DELETE' })
  toast.add({ title: 'Đã xóa danh mục', color: 'green' })
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="mb-6 flex flex-wrap items-center justify-between gap-3">
      <h1 class="text-xl font-bold text-gray-800">Quản lý danh mục</h1>
      <UButton color="green" icon="i-heroicons-plus" label="Thêm danh mục" @click="openCreate" />
    </div>

    <div v-if="showForm" class="mb-6 rounded-2xl border border-green-100 bg-white p-6">
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
          <label class="mb-1 block text-sm text-gray-500">Icon (tên icon heroicons)</label>
          <UInput v-model="form.icon" placeholder="ex: leaf, apple, tag" />
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Thứ tự</label>
          <UInput v-model="form.displayOrder" type="number" />
        </div>
        <div class="flex items-end pb-1">
          <UCheckbox v-model="form.active" label="Hiển thị" />
        </div>
        <div class="flex items-end justify-end gap-2 md:col-span-2">
          <UButton color="gray" variant="ghost" label="Hủy" @click="showForm = false" />
          <UButton type="submit" color="green" label="Lưu" :loading="saving" />
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
                  <UIcon v-if="c.icon" :name="`i-heroicons-${c.icon}`" class="h-4 w-4 text-green-600" />
                  <span class="font-medium text-gray-800">{{ c.name }}</span>
                </div>
              </td>
              <td class="px-4 py-3 text-gray-500">{{ c.slug }}</td>
              <td class="px-4 py-3"><UBadge color="gray" label="Cấp 1" size="sm" /></td>
              <td class="px-4 py-3"><UBadge :color="c.active ? 'success' : 'gray'" :label="c.active ? 'Hiển thị' : 'Ẩn'" size="sm" /></td>
              <td class="px-4 py-3">
                <div class="flex justify-end gap-1">
                  <UButton color="gray" variant="ghost" icon="i-heroicons-pencil-square" @click="openEdit(c)" />
                  <UButton color="gray" variant="ghost" icon="i-heroicons-trash" @click="remove(c)" />
                </div>
              </td>
            </tr>
            <tr v-for="child in c.children" :key="child.id" class="border-t border-gray-50 bg-gray-50/50">
              <td class="px-4 py-3 pl-8">
                <span class="flex items-center gap-2 text-gray-700">
                  <UIcon name="i-heroicons-arrow-turn-right-down" class="h-4 w-4 text-green-500" />
                  {{ child.name }}
                </span>
              </td>
              <td class="px-4 py-3 text-gray-500">{{ child.slug }}</td>
              <td class="px-4 py-3"><UBadge color="gray" variant="soft" label="Cấp 2" size="sm" /></td>
              <td class="px-4 py-3"><UBadge :color="child.active ? 'success' : 'gray'" :label="child.active ? 'Hiển thị' : 'Ẩn'" size="sm" /></td>
              <td class="px-4 py-3">
                <div class="flex justify-end gap-1">
                  <UButton color="gray" variant="ghost" icon="i-heroicons-pencil-square" @click="openEdit(child)" />
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>
  </div>
</template>