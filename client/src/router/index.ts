import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import DefaultLayout from '@/layouts/default.vue'
import AdminLayout from '@/layouts/admin.vue'

import HomePage from '@/pages/index.vue'
import LoginPage from '@/pages/login.vue'
import RegisterPage from '@/pages/register.vue'
import CartPage from '@/pages/cart.vue'
import CheckoutPage from '@/pages/checkout.vue'
import AccountPage from '@/pages/account.vue'
import PaymentResultPage from '@/pages/payment-result.vue'
import ProductsIndexPage from '@/pages/products/index.vue'
import ProductDetailPage from '@/pages/products/[slug].vue'
import OrdersIndexPage from '@/pages/orders/index.vue'
import OrderDetailPage from '@/pages/orders/[id].vue'
import AdminDashboardPage from '@/pages/admin/index.vue'
import AdminProductsPage from '@/pages/admin/products.vue'
import AdminCategoriesPage from '@/pages/admin/categories.vue'
import AdminOrdersPage from '@/pages/admin/orders.vue'
import AdminUsersPage from '@/pages/admin/users.vue'
import { useAuth } from '@/composables/useAuth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    component: LoginPage,
    meta: { layout: 'none' }
  },
  {
    path: '/register',
    component: RegisterPage,
    meta: { layout: 'none' }
  },
  {
    path: '/',
    component: DefaultLayout,
    children: [
      { path: '', name: 'home', component: HomePage },
      { path: 'products', name: 'products', component: ProductsIndexPage },
      { path: 'products/:slug', name: 'product-detail', component: ProductDetailPage },
      { path: 'cart', name: 'cart', component: CartPage, meta: { customerOnly: true } },
      { path: 'checkout', name: 'checkout', component: CheckoutPage, meta: { customerOnly: true } },
      { path: 'account', name: 'account', component: AccountPage, meta: { requiresAuth: true } },
      { path: 'payment-result', name: 'payment-result', component: PaymentResultPage, meta: { customerOnly: true } },
      { path: 'orders', name: 'orders', component: OrdersIndexPage, meta: { customerOnly: true } },
      { path: 'orders/:id', name: 'order-detail', component: OrderDetailPage, meta: { customerOnly: true } }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAdmin: true },
    children: [
      { path: '', name: 'admin', component: AdminDashboardPage, meta: { title: 'Tổng quan' } },
      { path: 'products', name: 'admin-products', component: AdminProductsPage, meta: { title: 'Sản phẩm' } },
      { path: 'categories', name: 'admin-categories', component: AdminCategoriesPage, meta: { title: 'Danh mục' } },
      { path: 'orders', name: 'admin-orders', component: AdminOrdersPage, meta: { title: 'Đơn hàng' } },
      { path: 'users', name: 'admin-users', component: AdminUsersPage, meta: { title: 'Người dùng' } }
    ]
  }
]

function smoothScroll(): 'auto' | 'smooth' {
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth'
}

export const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (to.hash) {
      return { el: to.hash, behavior: smoothScroll(), top: 64 }
    }
    if (savedPosition) {
      return savedPosition
    }
    if (to.name === 'home') {
      return { top: 0, behavior: smoothScroll() }
    }
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  const { isLoggedIn, isAdmin, restore } = useAuth()
  restore()

  if (to.meta.requiresAdmin) {
    if (!isLoggedIn.value) return { path: '/login' }
    if (!isAdmin.value) return { path: '/' }
    return true
  }
  if (to.meta.requiresAuth) {
    if (!isLoggedIn.value) return { path: '/login' }
    return true
  }
  if (to.meta.customerOnly) {
    if (!isLoggedIn.value) return { path: '/login' }
    if (isAdmin.value) return { path: '/admin' }
    return true
  }
  return true
})
