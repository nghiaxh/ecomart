<script setup lang="ts">
import type { Product } from '~/types'

defineProps<{ product: Product }>()
const { formatVND, formatKg } = useFormat()
</script>

<template>
  <NuxtLink
    :to="`/products/${product.slug}`"
    class="group block overflow-hidden rounded-2xl border border-gray-100 bg-white transition-all duration-300 hover:-translate-y-1 hover:border-emerald-200 hover:shadow-lg hover:shadow-emerald-900/5"
  >
    <div class="relative aspect-[4/3] overflow-hidden bg-gray-50">
      <UiImg
        :src="product.images && product.images.length > 0 ? product.images[0] : null"
        :alt="product.name"
        img-class="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
        :class="{ 'opacity-60 grayscale': product.stock === 0 }"
      />
      <span
        v-if="product.stock === 0"
        class="absolute left-2 top-2 rounded-full bg-red-500 px-2.5 py-1 text-xs font-semibold text-white"
      >
        Hết hàng
      </span>
      <span
        v-else-if="product.stock <= 10"
        class="absolute left-2 top-2 rounded-full bg-orange-500 px-2.5 py-1 text-xs font-semibold text-white"
      >
        Chỉ còn {{ product.stock }}
      </span>
    </div>

    <div class="p-4">
      <p class="text-xs text-gray-400">{{ product.categoryName }}</p>
      <h3 class="mt-1 line-clamp-2 font-semibold text-gray-800 group-hover:text-emerald-700">{{ product.name }}</h3>
      <p class="mt-1 text-xs text-gray-400">{{ formatKg(product.weight) }}</p>
      <div class="mt-2 flex items-end justify-between">
        <span class="text-base font-bold text-orange-600">{{ formatVND(product.price) }}</span>
      </div>
    </div>
  </NuxtLink>
</template>