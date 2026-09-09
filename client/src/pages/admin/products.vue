<script setup lang="ts">
import { ref, reactive, computed, h, onMounted, watch } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import { useToast } from '@/composables/useToast'
import type { CategoryResponse, PageResponse, Product, ProductRequest } from '@/types'
import { productSchema } from '@/schemas'
import { useApi } from '@/composables/useApi'
import { useConfirm } from '@/composables/useConfirm'
import { useFormErrors } from '@/composables/useFormErrors'
import { useFormat } from '@/composables/useFormat'
import UiImg from '@/components/UiImg.vue'
import UiIcon from '@/components/UiIcon.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import type { DataTableColumns } from 'naive-ui'
import { NButton, NCheckbox, NDataTable, NInput, NInputNumber, NModal, NSelect, NTag } from 'naive-ui'

const { request } = useApi()
const { formatVND } = useFormat()
const { errors, applyIssues, clearErrors } = useFormErrors()
const { confirm } = useConfirm()
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
  clearErrors()
  showForm.value = true
}

function openEdit(p: Product) {
  editingId.value = p.id
  Object.assign(form, {
    name: p.name, slug: p.slug, description: p.description || '', price: p.price, stock: p.stock,
    weight: p.weight, origin: p.origin || '', categoryId: p.categoryId, active: p.active,
    imageUrl: p.images?.[0] || ''
  })
  clearErrors()
  showForm.value = true
}

async function submit() {
  clearErrors()
  const result = productSchema.safeParse(form)
  if (!result.success) {
    applyIssues(result.error)
    return
  }
  saving.value = true
  const payload: ProductRequest = {
    name: form.name, slug: form.slug, description: form.description, price: Number(form.price), stock: Number(form.stock),
    weight: Number(form.weight || 0), origin: form.origin,
    categoryId: Number(form.categoryId), active: form.active,
    images: form.imageUrl ? [{ url: form.imageUrl, primary: true, displayOrder: 0 }] : []
  }
  try {
    if (editingId.value) {
      await request(`/api/products/${editingId.value}`, { method: 'PUT', body: payload })
    } else {
      await request('/api/products', { method: 'POST', body: payload })
    }
    toast.add({ severity: 'success', summary: 'Đã lưu sản phẩm', life: 4000 })
    showForm.value = false
    await load()
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Lưu thất bại', life: 4000 })
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
    toast.add({ severity: 'error', summary: e?.data?.message || 'Không thể cập nhật trạng thái', life: 4000 })
  } finally {
    busyIds.value.delete(p.id)
  }
}

async function remove(p: Product) {
  if (busyIds.value.has(p.id)) return
  if (!await confirm(`Xóa sản phẩm "${p.name}"?`, 'Xóa sản phẩm')) return
  busyIds.value.add(p.id)
  try {
    await request(`/api/products/${p.id}`, { method: 'DELETE' })
    toast.add({ severity: 'success', summary: 'Đã xóa sản phẩm', life: 4000 })
    await load()
  } catch (e: any) {
    toast.add({ severity: 'error', summary: e?.data?.message || 'Không thể xóa sản phẩm', life: 4000 })
  } finally {
    busyIds.value.delete(p.id)
  }
}

const columns = computed<DataTableColumns<Product>>(() => [
  {
    key: 'name',
    title: 'Sản phẩm',
    render: (row) => h('div', { class: 'flex min-w-0 items-center gap-3' }, [
      h(UiImg, { src: row.images?.[0], alt: row.name, imgClass: 'h-10 w-10 shrink-0 rounded-lg object-cover' }),
      h('span', { class: 'whitespace-normal font-medium text-gray-700' }, row.name)
    ])
  },
  {
    key: 'price',
    title: 'Giá',
    render: (row) => h('span', { class: 'text-gray-600 tabular-nums' }, formatVND(row.price))
  },
  {
    key: 'stock',
    title: 'Tồn kho',
    render: (row) => h('span', { class: 'text-gray-600 tabular-nums' }, row.stock)
  },
  {
    key: 'active',
    title: 'Trạng thái',
    render: (row) => h(NTag, { type: row.active ? 'success' : 'default' }, { default: () => row.active ? 'Bán' : 'Ẩn' })
  },
  {
    key: 'actions',
    title: 'Thao tác',
    render: (row) => h('div', { class: 'flex justify-end gap-1' }, [
      h(NButton, { quaternary: true, size: 'small', onClick: () => openEdit(row) }, { icon: () => h(UiIcon, { name: 'pencil', size: 16 }) }),
      h(NButton, { quaternary: true, size: 'small', loading: busyIds.value.has(row.id), onClick: () => toggle(row) }, { icon: () => h(UiIcon, { name: row.active ? 'eye-slash' : 'eye', size: 16 }) }),
      h(NButton, { quaternary: true, size: 'small', loading: busyIds.value.has(row.id), onClick: () => remove(row) }, { icon: () => h(UiIcon, { name: 'trash', size: 16 }) })
    ])
  }
])

const debouncedLoad = useDebounceFn(load, 400)

onMounted(() => { loadCats(); load() })
watch(search, debouncedLoad)
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <div class="mb-5 flex flex-wrap items-center justify-between gap-3">
      <div class="relative max-w-md flex-1">
        <NInput v-model:value="search" placeholder="Tìm sản phẩm..." clearable>
          <template #prefix><UiIcon name="search" size="16" /></template>
        </NInput>
      </div>
      <NButton type="primary" @click="openCreate">
        <template #icon><UiIcon name="plus" size="16" /></template>
        Thêm sản phẩm
      </NButton>
    </div>

    <div class="overflow-hidden rounded-2xl border border-gray-200 bg-white">
      <NDataTable :data="products" :columns="columns" :loading="loading" striped>
        <template #empty>
          <div v-if="!loading" class="flex flex-col items-center py-8 text-gray-400">
            <UiIcon name="inbox" size="32" class="mb-2 text-gray-300" />
            <p class="text-sm">Không có sản phẩm.</p>
          </div>
        </template>
      </NDataTable>
    </div>

    <PaginationBar
      v-if="totalPages > 1"
      :page="page"
      :total-pages="totalPages"
      @prev="page--; load()"
      @next="page++; load()"
    />

    <NModal
      v-model:show="showForm"
      preset="card"
      :title="editingId ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới'"
      :style="{ width: '920px', maxWidth: '95vw' }"
    >
      <form class="grid gap-4 md:grid-cols-3" @submit.prevent="submit">
        <div>
          <label class="mb-1 block text-sm text-gray-500">Tên</label>
          <NInput v-model:value="form.name" placeholder="vd: Rau muống sạch" />
          <p v-if="errors.name" class="text-xs text-red-600">{{ errors.name }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Slug</label>
          <NInput v-model:value="form.slug" placeholder="vd: rau-muong-sach" />
          <p v-if="errors.slug" class="text-xs text-red-600">{{ errors.slug }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Danh mục</label>
          <NSelect v-model:value="form.categoryId" :options="categories.map(c => ({ label: c.name, value: c.id }))" placeholder="Chọn danh mục" />
          <p v-if="errors.categoryId" class="text-xs text-red-600">{{ errors.categoryId }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Giá (₫)</label>
          <NInputNumber v-model:value="form.price" :min="0" :show-button="false" placeholder="vd: 15000" />
          <p v-if="errors.price" class="text-xs text-red-600">{{ errors.price }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Tồn kho</label>
          <NInputNumber v-model:value="form.stock" :min="0" :show-button="false" placeholder="vd: 100" />
          <p v-if="errors.stock" class="text-xs text-red-600">{{ errors.stock }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Khối lượng (kg)</label>
          <NInputNumber v-model:value="form.weight" :min="0" :show-button="false" placeholder="vd: 1" />
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Xuất xứ</label>
          <NInput v-model:value="form.origin" placeholder="vd: Lâm Đồng" />
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Hình ảnh URL</label>
          <NInput v-model:value="form.imageUrl" placeholder="https://..." />
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-500">Mô tả</label>
          <NInput v-model:value="form.description" type="textarea" :rows="3" placeholder="Mô tả ngắn về sản phẩm" />
        </div>
        <div class="flex items-center gap-2 md:col-span-3">
          <NCheckbox v-model:checked="form.active">Đang bán</NCheckbox>
        </div>
      </form>
      <template #footer>
        <div class="flex justify-end gap-2">
          <NButton quaternary @click="showForm = false">Hủy</NButton>
          <NButton type="primary" :loading="saving" @click="submit">Lưu</NButton>
        </div>
      </template>
    </NModal>
  </div>
</template>