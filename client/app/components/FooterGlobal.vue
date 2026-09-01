<template>
  <footer class="mt-12 border-t border-emerald-100 bg-emerald-50/40">
    <div class="mx-auto max-w-7xl px-4 py-10 sm:px-6">
      <div class="grid gap-8 md:grid-cols-4">
        <div>
          <div class="flex items-center gap-2">
            <img src="/favicon.svg" alt="EcoMart" class="h-8 w-8 rounded-lg" />
            <span class="text-lg font-extrabold text-emerald-800">EcoMart</span>
          </div>
          <p class="mt-3 text-sm leading-relaxed text-gray-500">
            Siêu thị thực phẩm tiện lợi với đa dạng sản phẩm tươi sạch cho mỗi gia đình Việt.
          </p>
        </div>

        <div>
          <h4 class="text-sm font-semibold text-gray-800">Sản phẩm</h4>
          <ul class="mt-3 space-y-2 text-sm text-gray-500">
            <li><NuxtLink to="/products" class="hover:text-emerald-700">Tất cả sản phẩm</NuxtLink></li>
            <li><NuxtLink to="/products?category=rau-cu-sach" class="hover:text-emerald-700">Rau củ sạch</NuxtLink></li>
            <li><NuxtLink to="/products?category=trai-cay-tuoi" class="hover:text-emerald-700">Trái cây tươi</NuxtLink></li>
          </ul>
        </div>

        <div>
          <h4 class="text-sm font-semibold text-gray-800">Tài khoản</h4>
          <ul class="mt-3 space-y-2 text-sm text-gray-500">
            <li><NuxtLink to="/account" class="hover:text-emerald-700">Tài khoản của tôi</NuxtLink></li>
            <li><NuxtLink to="/orders" class="hover:text-emerald-700">Đơn hàng</NuxtLink></li>
          </ul>
        </div>

        <div>
          <h4 class="text-sm font-semibold text-gray-800">Hỗ trợ</h4>
          <ul class="mt-3 space-y-2 text-sm text-gray-500">
            <li><NuxtLink to="/chat" class="hover:text-emerald-700">Chat trợ giúp</NuxtLink></li>
            <li><span>Hotline: 0900 000 000</span></li>
            <li><span>Email: hi@ecomart.vn</span></li>
          </ul>
        </div>
      </div>

      <div class="mt-8 border-t border-emerald-100 pt-6 text-center text-xs text-gray-400">
        © 2026 EcoMart. Phục vụ tận tâm mỗi ngày.
      </div>
    </div>
  </footer>
</template>
