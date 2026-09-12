import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import DefaultLayout from '@/layouts/default.vue'

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
import AdminStatisticsPage from '@/pages/admin/statistic.vue'
import AdminProductsPage from '@/pages/admin/products.vue'
import AdminCategoriesPage from '@/pages/admin/categories.vue'
import AdminOrdersPage from '@/pages/admin/orders.vue'
import AdminUsersPage from '@/pages/admin/users.vue'
import AdminActivityLogPage from '@/pages/admin/activity-log.vue'
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
      { path: 'orders/:id', name: 'order-detail', component: OrderDetailPage, meta: { customerOnly: true } },

      { path: 'admin/products', name: 'admin-products', component: AdminProductsPage, meta: { requiresStaffOrAdmin: true } },
      { path: 'admin/categories', name: 'admin-categories', component: AdminCategoriesPage, meta: { requiresStaffOrAdmin: true } },
      { path: 'admin/orders', name: 'admin-orders', component: AdminOrdersPage, meta: { requiresStaffOrAdmin: true } },
      { path: 'admin/users', name: 'admin-users', component: AdminUsersPage, meta: { requiresAdmin: true } },
      { path: 'admin/statistic', name: 'admin-statistic', component: AdminStatisticsPage, meta: { requiresStaffOrAdmin: true } },
      { path: 'admin/activity-log', name: 'admin-activity-log', component: AdminActivityLogPage, meta: { requiresAdmin: true } }
    ]
  }
]

function smoothScroll(): 'auto' | 'smooth' {
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth'
}

const HEADER_HEIGHT = 64

function scrollToHash(hash: string): Promise<false> {
  return new Promise(resolve => {
    let attempts = 0
    const maxAttempts = 60
    const tryScroll = () => {
      const el = document.querySelector(hash)
      if (el) {
        const top = el.getBoundingClientRect().top + window.scrollY - HEADER_HEIGHT
        window.scrollTo({ top, behavior: smoothScroll() })
        resolve(false)
      } else if (++attempts < maxAttempts) {
        requestAnimationFrame(tryScroll)
      } else {
        resolve(false)
      }
    }
    requestAnimationFrame(tryScroll)
  })
}

export const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (to.hash) {
      if (from.name !== to.name) {
        return scrollToHash(to.hash)
      }
      const el = document.querySelector(to.hash)
      if (el) {
        const top = el.getBoundingClientRect().top + window.scrollY - HEADER_HEIGHT
        window.scrollTo({ top, behavior: smoothScroll() })
      }
      return false
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
  const { isLoggedIn, isAdmin, isStaff, restore } = useAuth()
  restore()

  if (!isLoggedIn.value) {
    if (to.meta.requiresAdmin || to.meta.requiresStaffOrAdmin || to.meta.requiresAuth || to.meta.customerOnly) {
      return { path: '/login' }
    }
    return true
  }

  if (to.meta.requiresAdmin) {
    if (!isAdmin.value) return { path: '/' }
    return true
  }
  if (to.meta.requiresStaffOrAdmin) {
    if (!isAdmin.value && !isStaff.value) return { path: '/' }
    return true
  }
  if (to.meta.requiresAuth) {
    return true
  }
  if (to.meta.customerOnly) {
    if (isStaff.value || isAdmin.value) return { path: '/admin/products' }
    return true
  }
  return true
})
