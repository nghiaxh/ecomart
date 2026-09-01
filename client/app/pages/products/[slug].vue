<script setup lang="ts">
import type { Product, Review } from '~/types'

const { request } = useApi()
const { formatVND, formatKg, formatDate } = useFormat()
const { isLoggedIn } = useAuth()
const route = useRoute()
const toast = useToast()

const product = ref<Product | null>(null)
const reviews = ref<Review[]>([])
const quantity = ref(1)
const loading = ref(true)
const adding = ref(false)

const selectedImage = ref('')

const reviewForm = reactive({
  rating: 5,
  content: ''
})

async function load() {
  loading.value = true
  try {
    const p = await request<Product>(`/api/products/slug/${route.params.slug}`)
    product.value = p
    selectedImage.value = p.images?.[0] || ''
  } finally {
    loading.value = false
  }
}

async function loadReviews() {
  reviews.value = await request<Review[]>(`/api/reviews?productId=${product.value?.id}&includeHidden=false`)
}

async function addToCart() {
  if (!isLoggedIn.value) {
    navigateTo('/login')
    return
  }
  adding.value = true
  try {
    await useCart().add(product.value!.id, quantity.value)
    toast.add({ title: 'Đã thêm vào giỏ hàng', icon: 'i-ph-check-circle', color: 'success' })
  } finally {
    adding.value = false
  }
}

async function submitReview() {
  if (!product.value) return
  await request('/api/reviews', {
    method: 'POST',
    body: { productId: product.value.id, rating: reviewForm.rating, content: reviewForm.content }
  })
  toast.add({ title: 'Cảm ơn bạn đã đánh giá!', icon: 'i-ph-check-circle', color: 'success' })
  reviewForm.content = ''
  await loadReviews()
}

onMounted(() => {
  load().then(loadReviews)
})
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6" v-if="product">
    <!-- Breadcrumb -->
    <nav class="mb-6 text-sm text-gray-400">
      <NuxtLink to="/" class="hover:text-emerald-700">Trang chủ</NuxtLink>
      <span class="mx-2">/</span>
      <NuxtLink :to="`/products?category=${product.categorySlug}`" class="hover:text-emerald-700">{{ product.categoryName }}</NuxtLink>
      <span class="mx-2">/</span>
      <span class="text-gray-600">{{ product.name }}</span>
    </nav>

    <div class="grid gap-8 lg:grid-cols-2">
      <!-- Gallery -->
      <div>
        <div class="aspect-square overflow-hidden rounded-2xl bg-emerald-50">
          <img v-if="selectedImage" :src="selectedImage" :alt="product.name" class="h-full w-full object-cover" />
          <div v-else class="grid h-full w-full place-items-center text-emerald-200">
            <UIcon name="i-ph-image" class="h-16 w-16" />
          </div>
        </div>
        <div v-if="product.images && product.images.length > 1" class="mt-3 flex gap-2">
          <button
            v-for="img in product.images"
            :key="img"
            class="h-20 w-20 overflow-hidden rounded-lg border-2 transition"
            :class="selectedImage === img ? 'border-emerald-600' : 'border-transparent'"
            @click="selectedImage = img"
          >
            <img :src="img" :alt="product.name" class="h-full w-full object-cover" />
          </button>
        </div>
      </div>

      <!-- Info -->
      <div>
        <h1 class="mt-3 text-3xl font-extrabold text-gray-800">{{ product.name }}</h1>
        <p class="mt-2 text-sm text-gray-400">{{ formatKg(product.weight) }} · Nguồn gốc: {{ product.origin || 'Việt Nam' }}</p>

        <p class="mt-4 text-3xl font-extrabold text-emerald-700">{{ formatVND(product.price) }}</p>

        <!-- Materials -->
        <div v-if="product.materials && product.materials.length" class="mt-6">
          <h3 class="text-sm font-semibold text-gray-700">Chất liệu / Bao bì</h3>
          <div class="mt-2 flex flex-wrap gap-2">
            <span v-for="m in product.materials" :key="m.id" class="rounded-full border border-emerald-200 bg-white px-3 py-1 text-xs text-gray-600">
              {{ m.name }} · {{ m.percentage }}%
            </span>
          </div>
        </div>

        <!-- Description -->
        <p v-if="product.description" class="mt-6 leading-relaxed text-gray-600">{{ product.description }}</p>

        <!-- Quantity + add -->
        <div class="mt-8 flex flex-wrap items-center gap-4">
          <div class="flex items-center rounded-xl border border-emerald-200">
            <UButton color="neutral" variant="ghost" icon="i-ph-minus" :disabled="quantity <= 1" @click="quantity--" />
            <span class="w-10 text-center font-semibold">{{ quantity }}</span>
            <UButton color="neutral" variant="ghost" icon="i-ph-plus" :disabled="quantity >= product.stock" @click="quantity++" />
          </div>
          <UButton
            color="primary"
            size="lg"
            icon="i-ph-shopping-cart"
            :label="product.stock > 0 ? 'Thêm vào giỏ' : 'Hết hàng'"
            :disabled="product.stock <= 0"
            :loading="adding"
            @click="addToCart"
          />
        </div>
        <p v-if="product.stock > 0 && product.stock <= 10" class="mt-2 text-xs text-orange-600">Chỉ còn {{ product.stock }} sản phẩm</p>
        <p v-else-if="product.stock > 0" class="mt-2 text-xs text-gray-400">Còn {{ product.stock }} sản phẩm trong kho</p>
      </div>
    </div>

    <!-- Reviews -->
    <div class="mt-12 border-t border-emerald-100 pt-8">
      <h2 class="text-2xl font-extrabold text-gray-800">Đánh giá</h2>

      <div v-if="isLoggedIn" class="mt-6 rounded-2xl border border-emerald-100 bg-white p-4">
        <h3 class="font-semibold text-gray-700">Viết đánh giá</h3>
        <div class="mt-2 flex items-center gap-1">
          <button v-for="s in 5" :key="s" :class="s <= reviewForm.rating ? 'text-yellow-400' : 'text-gray-300'" @click="reviewForm.rating = s">
            <UIcon name="i-ph-star-fill" class="h-6 w-6" />
          </button>
        </div>
        <div class="mt-3 flex items-end gap-3">
          <UTextarea v-model="reviewForm.content" placeholder="Chia sẻ trải nghiệm của bạn..." class="flex-1" :rows="2" />
          <UButton color="primary" label="Gửi" icon="i-ph-paper-plane-tilt" @click="submitReview" />
        </div>
      </div>

      <div class="mt-6 space-y-4">
        <div v-for="r in reviews" :key="r.id" class="rounded-2xl border border-emerald-50 bg-white p-4">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <UAvatar :alt="r.customerName" size="sm" />
              <span class="font-medium text-gray-700">{{ r.customerName }}</span>
            </div>
            <div class="flex items-center gap-2 text-xs text-gray-400">
              <span class="flex gap-0.5">
                <UIcon v-for="s in 5" :key="s" :name="s <= r.rating ? 'i-ph-star-fill' : 'i-ph-star'" :class="s <= r.rating ? 'text-yellow-400' : 'text-gray-300'" class="h-4 w-4" />
              </span>
              <span>{{ formatDate(r.createdAt) }}</span>
            </div>
          </div>
          <p v-if="r.content" class="mt-2 text-sm text-gray-600">{{ r.content }}</p>
        </div>
        <p v-if="!reviews.length" class="py-8 text-center text-gray-400">Chưa có đánh giá nào.</p>
      </div>
    </div>
  </div>

  <div v-else-if="loading" class="mx-auto max-w-7xl px-4 py-12 sm:px-6">
    <USkeleton class="h-96 rounded-2xl" />
  </div>
  <div v-else class="py-24 text-center text-gray-400">Không tìm thấy sản phẩm.</div>
</template>
