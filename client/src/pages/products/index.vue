<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from '@/composables/useToast'
import { useDebounceFn } from '@vueuse/core'
import type { CategoryResponse, PageResponse, Product } from '@/types'
import { useApi } from '@/composables/useApi'
import ProductCard from '@/components/ProductCard.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import UiIcon from '@/components/UiIcon.vue'
import { NButton, NInput, NSelect, NSkeleton } from 'naive-ui'

const { request } = useApi()
const route = useRoute()
const router = useRouter()
const toast = useToast()

const size = 12
let loadSeq = 0

const PRICE_RANGES = [
  { label: 'Tất cả giá', value: 'all' },
  { label: 'Dưới 20.000 ₫', value: '0-20000' },
  { label: '20.000 – 50.000 ₫', value: '20000-50000' },
  { label: '50.000 – 100.000 ₫', value: '50000-100000' },
  { label: 'Trên 100.000 ₫', value: '100000-' }
]

const SORT_OPTIONS = [
  { label: 'Mặc định', value: 'default' },
  { label: 'Giá thấp đến cao', value: 'price_asc' },
  { label: 'Giá cao đến thấp', value: 'price_desc' }
]

const filters = reactive({
  q: (route.query.q as string) || '',
  category: (route.query.category as string) || '',
  priceRange: (route.query.priceRange as string) || 'all',
  sort: (route.query.sort as string) || 'default'
})
const page = ref(Number(route.query.page) || 0)

function priceBounds(range: string): { min: string; max: string } {
  const [min, max] = range.split('-')
  return { min: min || '', max: max || '' }
}

function buildParams() {
  const params = new URLSearchParams()
  if (filters.q) params.set('q', filters.q)
  if (filters.category) {
    const cat = activeCategory.value
    // Chỉ gửi id số lên backend (nhận Long). Slug lạ hoặc categories
    // chưa về thì bỏ qua để tránh 400 MethodArgumentTypeMismatch.
    if (cat) params.set('category', String(cat.id))
  }
  if (filters.priceRange && filters.priceRange !== 'all') {
    const { min, max } = priceBounds(filters.priceRange)
    if (min) params.set('minPrice', min)
    if (max) params.set('maxPrice', max)
  }
  if (filters.sort && filters.sort !== 'default') params.set('sort', filters.sort)
  params.set('page', String(page.value))
  params.set('size', String(size))
  return params
}

function syncUrl() {
  router.replace({
    query: {
      ...(filters.q ? { q: filters.q } : {}),
      ...(filters.category ? { category: filters.category } : {}),
      ...(filters.priceRange && filters.priceRange !== 'all' ? { priceRange: filters.priceRange } : {}),
      ...(filters.sort && filters.sort !== 'default' ? { sort: filters.sort } : {}),
      ...(page.value ? { page: String(page.value) } : {})
    }
  })
}

const categories = ref<CategoryResponse[]>([])

const activeCategory = computed(() => {
  if (!filters.category) return null
  const find = (list: CategoryResponse[]): CategoryResponse | null => {
    for (const c of list) {
      if (c.slug === filters.category) return c
      const child = find(c.children)
      if (child) return child
    }
    return null
  }
  return find(categories.value)
})

const products = ref<Product[]>([])
const total = ref(0)
const totalPages = ref(0)
const loading = ref(true)

function loadCategories() {
  request<CategoryResponse[]>('/api/categories')
    .then((data) => { categories.value = data })
    .catch(() => { categories.value = [] })
}

async function load(initial = false) {
  const seq = ++loadSeq
  if (!initial) loading.value = true
  try {
    const data = await request<PageResponse<Product>>(`/api/products?${buildParams().toString()}`)
    if (seq !== loadSeq) return
    products.value = data.content
    total.value = data.totalElements
    totalPages.value = data.totalPages
    syncUrl()
  } catch (error: any) {
    if (seq !== loadSeq) return
    if (initial) {
      products.value = []
      total.value = 0
      totalPages.value = 0
    }
    toast.add({ severity: 'error', summary: error?.data?.message || 'Không thể tải sản phẩm', life: 4000 })
  } finally {
    if (seq === loadSeq) loading.value = false
  }
}

function applyFilters() {
  page.value = 0
  load()
}

function setCategory(slug?: string) {
  filters.category = slug || ''
  applyFilters()
}

function clearFilters() {
  filters.q = ''
  filters.category = ''
  filters.priceRange = 'all'
  filters.sort = 'default'
  applyFilters()
}

const debouncedApply = useDebounceFn(applyFilters, 500)

watch(() => filters.q, debouncedApply)
watch(() => [filters.priceRange, filters.sort], () => {
  page.value = 0
  load()
})

// Đồng bộ khi điều hướng bằng banner/footer/nút back-forward.
watch(() => route.query.category, (slug) => {
  const next = (slug as string) || ''
  if (next !== filters.category) {
    filters.category = next
    page.value = 0
    load()
  }
})

onMounted(() => {
  loadCategories()
  load(true)
})
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <!-- Category chips -->
    <div class="mb-6 flex flex-wrap gap-2">
      <NButton
        :type="!filters.category ? 'primary' : 'default'"
        :secondary="!!filters.category"
        size="small"
        @click="setCategory()"
      >Tất cả</NButton
      >
      <template v-for="c in categories" :key="c.id">
        <NButton
          :type="filters.category === c.slug ? 'primary' : 'default'"
          :secondary="filters.category !== c.slug"
          size="small"
          @click="setCategory(c.slug)"
        >{{ c.name }}</NButton
        >
        <NButton
          v-for="child in c.children"
          :key="child.id"
          :type="filters.category === child.slug ? 'primary' : 'default'"
          :secondary="filters.category !== child.slug"
          size="small"
          @click="setCategory(child.slug)"
        >{{ child.name }}</NButton
        >
      </template>
    </div>

    <!-- Filters -->
    <div class="mb-8 grid gap-3 rounded-2xl border border-emerald-100 bg-white p-4 md:grid-cols-5">
      <div>
        <NInput v-model:value="filters.q" placeholder="Tìm sản phẩm..." clearable>
          <template #prefix><UiIcon name="search" size="16" /></template>
        </NInput>
      </div>
      <NSelect
        v-model:value="filters.priceRange"
        :options="PRICE_RANGES"
        placeholder="Khoảng giá"
      />
      <NSelect
        v-model:value="filters.sort"
        :options="SORT_OPTIONS"
        placeholder="Sắp xếp"
      />
      <NButton type="primary" @click="applyFilters">
        <template #icon><UiIcon name="filter" size="16" /></template>
        Lọc
      </NButton>
      <NButton quaternary @click="clearFilters">Bỏ lọc</NButton>
    </div>

    <!-- Product grid -->
    <div v-if="loading" class="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
      <NSkeleton v-for="i in 8" :key="i" class="h-72 rounded-2xl" />
    </div>
    <div v-else-if="products.length" class="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
      <ProductCard v-for="p in products" :key="p.id" :product="p" />
    </div>
    <div v-else class="py-20 text-center text-gray-400">
      <UiIcon name="inbox" size="48" class="mx-auto mb-3 block" />
      <p>Không tìm thấy sản phẩm phù hợp.</p>
      <NButton class="mt-4" type="primary" secondary @click="clearFilters">Bỏ lọc</NButton>
    </div>

    <!-- Pagination -->
    <PaginationBar
      v-if="totalPages > 1"
      :page="page"
      :total-pages="totalPages"
      @prev="page--; load()"
      @next="page++; load()"
    />
  </div>
</template>
