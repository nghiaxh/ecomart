<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const { cart, fetchCart, updateQuantity, remove } = useCart()
const { formatVND } = useFormat()
const toast = useToast()

onMounted(() => fetchCart())
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <h1 class="text-3xl font-extrabold text-gray-800">Giỏ hàng</h1>

    <div v-if="cart && cart.items.length" class="mt-8 grid gap-8 lg:grid-cols-3">
      <div class="lg:col-span-2 space-y-4">
        <div v-for="item in cart.items" :key="item.productId" class="flex gap-4 rounded-2xl border border-green-100 bg-white p-4">
          <NuxtLink :to="`/products/${item.productSlug}`" class="h-24 w-24 shrink-0 overflow-hidden rounded-xl bg-green-50">
            <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.productName" class="h-full w-full object-cover" />
            <div v-else class="grid h-full w-full place-items-center text-green-200"><UIcon name="i-heroicons-photo" class="h-8 w-8" /></div>
          </NuxtLink>
          <div class="flex flex-1 flex-col">
            <div class="flex items-start justify-between gap-2">
              <div>
                <NuxtLink :to="`/products/${item.productSlug}`" class="font-semibold text-gray-800 hover:text-green-700">{{ item.productName }}</NuxtLink>
                <p class="mt-1 flex items-center gap-1 text-xs font-semibold text-green-600">
                  <UIcon name="i-heroicons-cloud" class="h-3.5 w-3.5" />
                  −{{ item.co2SavedPerUnit.toFixed(1) }} kg CO₂
                </p>
              </div>
              <button class="text-gray-300 hover:text-red-500" @click="remove(item.productId)">
                <UIcon name="i-heroicons-trash" class="h-5 w-5" />
              </button>
            </div>
            <div class="mt-auto flex items-center justify-between pt-3">
              <div class="flex items-center rounded-lg border border-green-200">
                <UButton color="gray" variant="ghost" icon="i-heroicons-minus" size="sm" :disabled="item.quantity <= 1" @click="updateQuantity(item.productId, item.quantity - 1)" />
                <span class="w-8 text-center text-sm font-semibold">{{ item.quantity }}</span>
                <UButton color="gray" variant="ghost" icon="i-heroicons-plus" size="sm" :disabled="item.quantity >= item.stock" @click="updateQuantity(item.productId, item.quantity + 1)" />
              </div>
              <span class="font-bold text-green-700">{{ formatVND(item.price * item.quantity) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Summary -->
      <div class="h-fit rounded-2xl border border-green-100 bg-white p-6 lg:sticky lg:top-20">
        <h2 class="text-lg font-bold text-gray-800">Tóm tắt đơn hàng</h2>
        <div class="mt-4 space-y-2 text-sm">
          <div class="flex justify-between text-gray-500"><span>Tạm tính</span><span>{{ formatVND(cart.subtotal) }}</span></div>
          <div class="flex justify-between text-green-600"><span>CO₂ tiết kiệm</span><span>{{ cart.totalCo2Saved.toFixed(2) }} kg</span></div>
        </div>
        <div class="mt-4 border-t border-green-50 pt-4">
          <div class="flex justify-between text-lg"><span class="font-semibold text-gray-700">Tổng cộng</span><span class="font-bold text-green-700">{{ formatVND(cart.subtotal) }}</span></div>
        </div>
        <UButton to="/checkout" color="green" size="lg" block class="mt-6" icon="i-heroicons-arrow-right" label="Tiến hành thanh toán" />
        <UButton to="/products" color="gray" variant="ghost" block class="mt-2" label="Tiếp tục mua sắm" />
      </div>
    </div>

    <div v-else class="py-24 text-center">
      <UIcon name="i-heroicons-shopping-cart" class="mx-auto mb-4 h-16 w-16 text-green-200" />
      <p class="text-gray-500">Giỏ hàng của bạn đang trống.</p>
      <UButton to="/products" color="green" class="mt-4" label="Mua sắm ngay" icon="i-heroicons-shopping-bag" />
    </div>
  </div>
</template>
