<script setup lang="ts">
import type { Product } from '~/types'

defineProps<{ product: Product }>()
const { formatVND, formatKg } = useFormat()
</script>

<template>
  <NuxtLink
    :to="`/products/${product.slug}`"
    class="overflow-hidden rounded-2xl border border-gray-100 bg-white"
  >
    <div class="relative aspect-[4/3] overflow-hidden bg-gray-50">
      <img
        v-if="product.images && product.images.length > 0"
        :src="product.images[0]"
        :alt="product.name"
        loading="lazy"
        class="h-full w-full object-cover"
        @error="($event.target as HTMLImageElement).src = '/images/placeholder.svg'"
      />
      <div v-else class="grid h-full w-full place-items-center text-gray-200">
        <UIcon name="i-ph-image" class="h-12 w-12" />
      </div>
    </div>

    <div class="p-4">
      <p class="text-xs text-gray-400">{{ product.categoryName }}</p>
      <h3 class="mt-1 line-clamp-2 font-semibold text-gray-800">{{ product.name }}</h3>
      <p class="mt-1 text-xs text-gray-400">{{ formatKg(product.weight) }}</p>
      <div class="mt-2 flex items-end justify-between">
        <span class="text-base font-bold text-orange-600">{{ formatVND(product.price) }}</span>
      </div>
    </div>
  </NuxtLink>
</template>