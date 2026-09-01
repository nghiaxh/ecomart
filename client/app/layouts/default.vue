<script setup lang="ts">
const { isLoggedIn, isAdmin, session, logout } = useAuth()
const cart = useCart()
const { formatVND } = useFormat()

const links = [
  { label: 'Trang chủ', to: '/' },
  { label: 'Sản phẩm', to: '/products' },
  { label: 'Về chúng tôi', to: '/#about' }
]

const userMenu = computed(() => {
  const items: { label: string; icon: string; to?: string; onClick?: () => void }[] = [{ label: 'Tài khoản', icon: 'i-ph-user', to: '/account' }]
  if (isAdmin.value) {
    items.push({ label: 'Quản trị', icon: 'i-ph-squares-four', to: '/admin' })
  }
  items.push({ label: 'Đăng xuất', icon: 'i-ph-sign-out', onClick: logout })
  return items
})
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <header class="sticky top-0 z-40 border-b border-emerald-100/70 bg-white/90 backdrop-blur">
      <div class="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6">
        <NuxtLink to="/" class="flex items-center gap-2">
          <span class="grid h-9 w-9 place-items-center rounded-xl bg-emerald-600 text-white">
            <UIcon name="i-ph-storefront" class="h-5 w-5" />
          </span>
          <span class="text-xl font-extrabold tracking-tight text-emerald-800">EcoMart</span>
        </NuxtLink>

        <nav class="hidden items-center gap-8 md:flex">
          <NuxtLink v-for="link in links" :key="link.to" :to="link.to" class="text-sm font-medium text-gray-600 transition hover:text-emerald-700">
            {{ link.label }}
          </NuxtLink>
        </nav>

        <div class="flex items-center gap-1">
          <UButton icon="i-ph-chats-circle" color="neutral" variant="ghost" :to="'/chat'" square aria-label="Chat trợ giúp" />

          <UButton
            color="neutral"
            variant="ghost"
            square
            aria-label="Giỏ hàng"
            :to="isLoggedIn ? '/cart' : '/login'"
            class="relative"
          >
            <UIcon name="i-ph-shopping-cart" class="h-5 w-5" />
            <span v-if="cart.itemCount > 0" class="absolute right-0 top-0 grid h-4 min-w-4 place-items-center rounded-full bg-emerald-600 px-1 text-[10px] font-bold text-white">
              {{ cart.itemCount }}
            </span>
          </UButton>

          <template v-if="isLoggedIn">
            <UDropdownMenu :items="userMenu" :content="{ side: 'bottom', align: 'end' }">
              <UButton color="neutral" variant="ghost" class="gap-2">
                <UAvatar :src="session?.avatarUrl || undefined" :alt="session?.username" size="sm" />
                <span class="hidden text-sm font-medium sm:inline">{{ session?.username }}</span>
              </UButton>
            </UDropdownMenu>
          </template>
          <template v-else>
            <UButton :to="'/login'" color="neutral" variant="soft" size="sm">Đăng nhập</UButton>
            <UButton :to="'/register'" color="primary" size="sm" class="ml-1">Đăng ký</UButton>
          </template>
        </div>
      </div>
    </header>

    <main class="flex-1">
      <slot />
    </main>

    <FooterGlobal />
  </div>
</template>
