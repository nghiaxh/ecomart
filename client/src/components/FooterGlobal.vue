<script setup lang="ts">
const supportPhone = import.meta.env.VITE_SUPPORT_PHONE || '0900 000 000'
const supportEmail = import.meta.env.VITE_SUPPORT_EMAIL || 'contact@ecomart.vn'
</script>

<template>
  <footer class="mt-8 border-t border-emerald-100 bg-emerald-50/40">
    <div class="mx-auto max-w-7xl px-4 py-6 sm:px-6">
      <div class="grid gap-6 md:grid-cols-4">
        <div>
          <div class="flex items-center gap-2">
            <img src="/favicon.svg" alt="EcoMart" class="h-7 w-7 rounded-lg" />
            <span class="text-base font-extrabold text-emerald-800">EcoMart</span>
          </div>
          <p class="mt-2 text-sm leading-relaxed text-gray-500">
            Siêu thị thực phẩm tiện lợi với đa dạng sản phẩm tươi sạch cho mỗi gia đình Việt.
          </p>
        </div>

        <div>
          <h4 class="text-sm font-semibold text-gray-700">Sản phẩm</h4>
          <ul class="mt-2 space-y-1.5 text-sm text-gray-500">
            <li><RouterLink to="/products" class="hover:text-emerald-700">Tất cả sản phẩm</RouterLink></li>
            <li><RouterLink to="/products?category=rau-cu-sach" class="hover:text-emerald-700">Rau củ sạch</RouterLink></li>
            <li><RouterLink to="/products?category=trai-cay-tuoi" class="hover:text-emerald-700">Trái cây tươi</RouterLink></li>
          </ul>
        </div>

        <div>
          <h4 class="text-sm font-semibold text-gray-700">Tài khoản</h4>
          <ul class="mt-2 space-y-1.5 text-sm text-gray-500">
            <li><RouterLink to="/account" class="hover:text-emerald-700">Tài khoản của tôi</RouterLink></li>
            <li><RouterLink to="/orders" class="hover:text-emerald-700">Đơn hàng</RouterLink></li>
          </ul>
        </div>

        <div>
          <h4 class="text-sm font-semibold text-gray-700">Hỗ trợ</h4>
          <ul class="mt-2 space-y-1.5 text-sm text-gray-500">
            <li><RouterLink to="/chat" class="hover:text-emerald-700">Chat trợ giúp</RouterLink></li>
            <li><span>Hotline: {{ supportPhone }}</span></li>
            <li><span>Email: {{ supportEmail }}</span></li>
          </ul>
        </div>
      </div>

      <div class="mt-5 border-t border-emerald-100 pt-4 text-center text-xs text-gray-400">
        © 2026 EcoMart. Phục vụ tận tâm mỗi ngày.
      </div>
    </div>
  </footer>
</template>
