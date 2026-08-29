<script setup lang="ts">
definePageMeta({
  layout: 'admin'
})

const { logout } = useAuth()

const nav = [
  { label: 'Tổng quan', icon: 'i-heroicons-squares-2x2', to: '/admin' },
  { label: 'Sản phẩm', icon: 'i-heroicons-shopping-bag', to: '/admin/products' },
  { label: 'Danh mục', icon: 'i-heroicons-tag', to: '/admin/categories' },
  { label: 'Đơn hàng', icon: 'i-heroicons-receipt-percent', to: '/admin/orders' },
  { label: 'Banner', icon: 'i-heroicons-photo', to: '/admin/banners' },
]
</script>

<template>
  <div class="flex min-h-screen bg-gray-50">
    <aside class="sticky top-0 hidden h-screen w-64 flex-col border-r border-gray-200 bg-white lg:flex">
      <NuxtLink to="/" class="flex h-16 items-center gap-2 border-b border-gray-100 px-6">
        <span class="grid h-8 w-8 place-items-center rounded-lg bg-green-600 text-white">
          <UIcon name="i-heroicons-leaf" class="h-4 w-4" />
        </span>
        <div class="leading-tight">
          <p class="text-sm font-extrabold text-green-800">EcoMart</p>
          <p class="text-xs text-gray-400">Quản trị</p>
        </div>
      </NuxtLink>

      <nav class="flex-1 space-y-1 p-3">
        <NuxtLink
          v-for="item in nav"
          :key="item.to"
          :to="item.to"
          class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-gray-600 transition hover:bg-green-50 hover:text-green-700"
          active-class="!bg-green-600 !text-white"
        >
          <UIcon :name="item.icon" class="h-5 w-5" />
          {{ item.label }}
        </NuxtLink>
      </nav>

      <div class="border-t border-gray-100 p-3">
        <UButton color="gray" variant="ghost" block icon="i-heroicons-arrow-left-on-rectangle" label="Về trang chủ" :to="'/' " />
        <UButton class="mt-1" color="gray" variant="soft" block icon="i-heroicons-arrow-right-on-rectangle" label="Đăng xuất" @click="logout" />
      </div>
    </aside>

    <div class="flex min-h-screen flex-1 flex-col">
      <header class="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-gray-200 bg-white px-6">
        <h1 class="text-lg font-bold text-gray-800">Bảng điều khiển</h1>
        <UButton icon="i-heroicons-shopping-cart" color="gray" variant="ghost" :to="'/' " square />
      </header>

      <main class="flex-1 p-6">
        <slot />
      </main>
    </div>
  </div>
</template>
