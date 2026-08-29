<script setup lang="ts">
import type { Product } from '~/types'

defineProps<{ product: Product }>()
const { formatVND, formatKg } = useFormat()
</script>

<template>
  <NuxtLink
    :to="`/products/${product.slug}`"
    class="group overflow-hidden rounded-2xl border border-green-100 bg-white transition hover:-translate-y-1 hover:shadow-lg hover:shadow-green-100/60"
  >
    <div class="relative aspect-[4/3] overflow-hidden bg-green-50">
      <img
        v-if="product.images && product.images.length > 0"
        :src="product.images[0]"
        :alt="product.name"
        loading="lazy"
        class="h-full w-full object-cover transition duration-500 group-hover:scale-105"
      />
      <div v-else class="grid h-full w-full place-items-center text-green-200">
        <UIcon name="i-heroicons-photo" class="h-12 w-12" />
      </div>
      <span v-if="product.co2Saved > 0" class="absolute left-3 top-3 rounded-full bg-green-600/90 px-2.5 py-1 text-xs font-semibold text-white shadow">
        −{{ product.co2Saved.toFixed(1) }} kg CO₂
      </span>
    </div>

    <div class="p-4">
      <p class="text-xs text-gray-400">{{ product.categoryName }}</p>
      <h3 class="mt-1 line-clamp-2 font-semibold text-gray-800 group-hover:text-green-700">
        {{ product.name }}
      </h3>
      <p class="mt-1 text-xs text-gray-400">{{ formatKg(product.weight) }}</p>
      <div class="mt-2 flex items-end justify-between">
        <span class="text-base font-bold text-green-700">{{ formatVND(product.price) }}</span>
        <span class="flex items-center gap-1 text-xs font-semibold text-green-600">
          <UIcon name="i-heroicons-star" class="h-3.5 w-3.5" />
          +{{ product.ecoPointsPerUnit }} Eco
        </span>
      </div>
    </div>
  </NuxtLink>
</template>
