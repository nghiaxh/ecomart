<script setup lang="ts">
import { ref, computed, watch, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from '@/composables/useToast'
import { useHead } from '@unhead/vue'
import type { MaterialType, Product, Review } from '@/types'
import { reviewSchema } from '@/schemas'
import { useApi } from '@/composables/useApi'
import { useAuth } from '@/composables/useAuth'
import { useCart } from '@/composables/useCart'
import { useFormat } from '@/composables/useFormat'
import UiImg from '@/components/UiImg.vue'
import UiIcon from '@/components/UiIcon.vue'
import AddToCartButton from '@/components/AddToCartButton.vue'
import { NAvatar, NButton, NInput, NSkeleton } from 'naive-ui'

const { request } = useApi()
const { formatVND, formatKg, formatDate } = useFormat()
const { isLoggedIn, isAdmin } = useAuth()
const route = useRoute()
const router = useRouter()
const toast = useToast()

const slug = route.params.slug as string

const product = ref<Product | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    product.value = await request<Product>(`/api/products/slug/${slug}`)
  } catch (error: any) {
    toast.add({ severity: 'error', summary: error?.data?.message || 'Không thể tải sản phẩm', life: 4000 })
  } finally {
    loading.value = false
  }
})

useHead({ title: () => (product.value ? `${product.value.name} | EcoMart` : 'Sản phẩm | EcoMart') })

const { add: addToCartApi } = useCart()
const reviews = ref<Review[]>([])
const quantity = ref(1)
const adding = ref(false)
const submittingReview = ref(false)
const reviewLoadError = ref(false)

const selectedImage = ref('')
watch(product, (p) => {
  selectedImage.value = p?.images?.[0] || ''
  quantity.value = 1
}, { immediate: true })

const liveTotal = computed(() => (product.value?.price || 0) * quantity.value)
const avgRating = computed(() => reviews.value.length ? reviews.value.reduce((s, r) => s + r.rating, 0) / reviews.value.length : 0)
const reviewCount = computed(() => reviews.value.length)

const MATERIAL_META: Record<MaterialType, { icon: string; color: string }> = {
  ORGANIC: { icon: 'sun', color: 'bg-emerald-50 text-emerald-700 border-emerald-200' },
  RECYCLED: { icon: 'sync', color: 'bg-teal-50 text-teal-700 border-teal-200' },
  NATURAL: { icon: 'sun', color: 'bg-sky-50 text-sky-700 border-sky-200' },
  SYNTHETIC: { icon: 'bolt', color: 'bg-slate-100 text-slate-700 border-slate-200' }
}

function materialIcon(type: string) {
  return (MATERIAL_META[type as MaterialType] || { icon: 'box' }).icon
}

function materialColor(type: string) {
  return (MATERIAL_META[type as MaterialType] || { color: 'bg-gray-100 text-gray-600 border-gray-200' }).color
}

function goBack() {
  if (typeof window !== 'undefined' && window.history.length > 1) router.back()
  else router.push('/products')
}

const reviewFormOpen = ref(false)
const reviewForm = reactive({ rating: 5, content: '' })

async function loadReviews() {
  if (!product.value) return
  reviewLoadError.value = false
  try {
    reviews.value = await request<Review[]>(`/api/reviews?productId=${product.value.id}&includeHidden=false`)
  } catch {
    reviewLoadError.value = true
  }
}

async function addToCart() {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }
  if (isAdmin.value) {
    router.push('/admin/products')
    return
  }
  if (!product.value || product.value.stock <= 0) return
  adding.value = true
  try {
    await addToCartApi(product.value.id, quantity.value)
    toast.add({ severity: 'success', summary: 'Đã thêm vào giỏ hàng', life: 4000 })
  } finally {
    adding.value = false
  }
}

async function submitReview() {
  if (!product.value || submittingReview.value) return
  const parsed = reviewSchema.safeParse(reviewForm)
  if (!parsed.success) {
    const first = parsed.error.issues[0]
    toast.add({ severity: 'error', summary: first?.message || 'Đánh giá không hợp lệ', life: 4000 })
    return
  }
  submittingReview.value = true
  try {
    await request('/api/reviews', {
      method: 'POST',
      body: { productId: product.value.id, rating: reviewForm.rating, content: reviewForm.content }
    })
    toast.add({ severity: 'success', summary: 'Cảm ơn bạn đã đánh giá!', life: 4000 })
    reviewForm.content = ''
    await loadReviews()
  } catch (error: any) {
    toast.add({ severity: 'error', summary: error?.data?.message || 'Không thể gửi đánh giá', life: 4000 })
  } finally {
    submittingReview.value = false
  }
}

onMounted(loadReviews)
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6" v-if="product">
    <button @click="goBack" class="mb-4 inline-flex items-center gap-1 text-sm text-gray-400 transition hover:text-emerald-700">
      <UiIcon name="caret-left" size="16" /> Quay lại
    </button>

    <nav class="mb-6 text-sm text-gray-400">
      <RouterLink to="/" class="hover:text-emerald-700">Trang chủ</RouterLink>
      <span class="mx-2">/</span>
      <RouterLink :to="`/products?category=${product.categorySlug}`" class="hover:text-emerald-700">{{ product.categoryName }}</RouterLink>
      <span class="mx-2">/</span>
      <span class="text-gray-600">{{ product.name }}</span>
    </nav>

    <div class="grid gap-8 lg:grid-cols-[1fr_1.1fr]">
      <!-- Gallery -->
      <div class="lg:sticky lg:top-24 lg:self-start">
        <div class="group aspect-[4/3] cursor-zoom-in overflow-hidden rounded-2xl bg-emerald-50">
          <UiImg :src="selectedImage" :alt="product.name" img-class="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105" />
        </div>
        <div v-if="product.images && product.images.length > 1" class="mt-3 flex gap-2">
          <button
            v-for="img in product.images"
            :key="img"
            class="h-14 w-14 overflow-hidden rounded-lg border-2 transition"
            :class="selectedImage === img ? 'border-emerald-600 ring-2 ring-emerald-600/20' : 'border-transparent hover:border-emerald-200'"
            @click="selectedImage = img"
          >
            <UiImg :src="img" :alt="product.name" img-class="h-full w-full object-cover" />
          </button>
        </div>
      </div>

      <!-- Info -->
      <div>
        <h1 class="text-2xl font-extrabold text-gray-700 sm:text-3xl">{{ product.name }}</h1>
        <p class="mt-2 text-sm text-gray-400">{{ formatKg(product.weight) }} · Nguồn gốc: {{ product.origin || 'Việt Nam' }}</p>

        <!-- Price & stock highlight -->
        <div class="mt-5 rounded-xl bg-emerald-50 px-5 py-4">
          <p class="text-2xl font-extrabold text-emerald-700 sm:text-3xl">{{ formatVND(product.price) }}</p>
          <div class="mt-2">
            <span
              v-if="product.stock <= 0"
              class="inline-block rounded-full bg-red-100 px-3 py-1 text-xs font-semibold text-red-700"
            >Hết hàng</span>
            <span
              v-else-if="product.stock <= 10"
              class="inline-block rounded-full bg-orange-100 px-3 py-1 text-xs font-semibold text-orange-700"
            >Chỉ còn {{ product.stock }} sản phẩm</span>
            <span
              v-else
              class="inline-block rounded-full bg-emerald-100 px-3 py-1 text-xs font-semibold text-emerald-700"
            >Còn {{ product.stock }} sản phẩm</span>
          </div>
        </div>

        <!-- Materials -->
        <div v-if="product.materials?.length" class="mt-6">
          <h3 class="text-sm font-semibold text-gray-700">Chất liệu / Bao bì</h3>
          <div class="mt-2 flex flex-wrap gap-2">
            <span
              v-for="m in product.materials"
              :key="m.id"
              class="inline-flex items-center gap-1.5 rounded-full border px-3 py-1 text-xs font-medium"
              :class="materialColor(m.type)"
            >
              <UiIcon :name="materialIcon(m.type)" size="14" />
              {{ m.name }} · {{ m.percentage }}%
            </span>
          </div>
        </div>

        <!-- Description -->
        <p v-if="product.description" class="mt-6 leading-relaxed text-gray-600">{{ product.description }}</p>

        <!-- Quantity + add + live total -->
        <div class="mt-8 flex flex-wrap items-center gap-4">
          <div class="flex items-center rounded-xl border border-emerald-200">
            <NButton quaternary :disabled="quantity <= 1" class="min-h-11 min-w-11" @click="quantity--">
              <template #icon><UiIcon name="minus" size="16" /></template>
            </NButton>
            <span class="w-10 text-center font-semibold">{{ quantity }}</span>
            <NButton quaternary :disabled="quantity >= product.stock" class="min-h-11 min-w-11" @click="quantity++">
              <template #icon><UiIcon name="plus" size="16" /></template>
            </NButton>
          </div>
          <AddToCartButton v-if="!isAdmin" :stock="product.stock" :loading="adding" @add="addToCart" />
          <span class="text-sm text-gray-500">
            Tổng: <strong class="text-emerald-700">{{ formatVND(liveTotal) }}</strong>
          </span>
        </div>
      </div>
    </div>

    <!-- Reviews -->
    <div class="mt-12 border-t border-emerald-100 pt-8">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <h2 class="text-2xl font-extrabold text-gray-700">Đánh giá</h2>
          <template v-if="reviewCount > 0">
            <div class="flex items-center gap-1.5 rounded-full bg-yellow-50 px-3 py-1">
              <UiIcon name="star-fill" size="16" class="text-yellow-400" />
              <span class="text-sm font-bold text-gray-700">{{ avgRating.toFixed(1) }}</span>
            </div>
            <span class="text-sm text-gray-400">{{ reviewCount }} đánh giá</span>
          </template>
        </div>
        <NButton
          v-if="isLoggedIn"
          type="primary"
          secondary
          size="small"
          @click="reviewFormOpen = !reviewFormOpen"
        >
          <template #icon><UiIcon :name="reviewFormOpen ? 'times' : 'pencil'" size="16" /></template>
          {{ reviewFormOpen ? 'Đóng' : 'Viết đánh giá' }}
        </NButton>
      </div>

      <div v-if="isLoggedIn && reviewFormOpen" class="mt-6 rounded-2xl border border-emerald-100 bg-white p-4">
        <div class="flex items-center gap-1">
          <button v-for="s in 5" :key="s" type="button" :aria-label="`Đánh giá ${s} sao`" :class="s <= reviewForm.rating ? 'text-yellow-400' : 'text-gray-300'" @click="reviewForm.rating = s">
            <UiIcon name="star-fill" size="22" />
          </button>
        </div>
        <div class="mt-3 flex items-end gap-3">
          <NInput v-model:value="reviewForm.content" type="textarea" placeholder="Chia sẻ trải nghiệm của bạn..." :rows="2" class="flex-1" />
          <NButton type="primary" :loading="submittingReview" @click="submitReview">
            <template #icon><UiIcon name="send" size="16" /></template>
            Gửi
          </NButton>
        </div>
      </div>

      <div class="mt-6 space-y-4">
        <div v-for="r in reviews" :key="r.id" class="rounded-2xl border border-emerald-50 bg-white p-4">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <NAvatar size="medium">{{ r.customerName?.charAt(0)?.toUpperCase() || '?' }}</NAvatar>
              <span class="font-medium text-gray-700">{{ r.customerName }}</span>
            </div>
            <div class="flex items-center gap-2 text-xs text-gray-400">
              <span class="flex gap-0.5">
                <UiIcon v-for="s in 5" :key="s" :name="s <= r.rating ? 'star-fill' : 'star'" size="16" :class="s <= r.rating ? 'text-yellow-400' : 'text-gray-300'" />
              </span>
              <span>{{ formatDate(r.createdAt) }}</span>
            </div>
          </div>
          <p v-if="r.content" class="mt-2 text-sm text-gray-600">{{ r.content }}</p>
        </div>
        <p v-if="!reviews.length && !reviewLoadError" class="py-8 text-center text-gray-400">Chưa có đánh giá nào.</p>
        <p v-if="reviewLoadError" class="py-8 text-center text-red-400">Không thể tải đánh giá.</p>
      </div>
    </div>

    <!-- Sticky mobile CTA -->
    <div
      v-if="!isAdmin"
      class="fixed bottom-0 inset-x-0 z-50 border-t border-emerald-100 bg-white px-4 py-3 shadow-lg lg:hidden"
    >
      <div class="flex items-center justify-between">
        <div>
          <p class="text-lg font-extrabold text-emerald-700">{{ formatVND(product.price) }}</p>
          <p class="text-xs text-gray-400">x{{ quantity }}</p>
        </div>
        <AddToCartButton :stock="product.stock" :loading="adding" @add="addToCart" />
      </div>
    </div>
  </div>

  <div v-else-if="loading" class="mx-auto max-w-7xl px-4 py-12 sm:px-6">
    <NSkeleton class="mb-4 h-4 w-20 rounded" />
    <div class="grid gap-8 lg:grid-cols-[1fr_1.1fr]">
      <NSkeleton class="aspect-[4/3] rounded-2xl" />
      <div class="space-y-4">
        <NSkeleton class="h-8 w-3/4 rounded" />
        <NSkeleton class="h-4 w-1/2 rounded" />
        <NSkeleton class="h-20 rounded-xl" />
        <NSkeleton class="h-10 w-40 rounded-xl" />
      </div>
    </div>
  </div>
  <div v-else class="py-24 text-center text-gray-400">Không tìm thấy sản phẩm.</div>

  <!-- Pad bottom for sticky mobile CTA -->
  <div v-if="!isAdmin && product" class="h-20 lg:hidden" />
</template>