<script setup lang="ts">
import { registerSchema } from '~/schemas'

definePageMeta({
  layout: false
})

const { register } = useAuth()
const toast = useToast()

const form = reactive({ username: '', email: '', numberPhone: '', password: '' })
const loading = ref(false)

async function submit() {
  const result = registerSchema.safeParse(form)
  if (!result.success) {
    for (const issue of result.error.issues) {
      toast.add({ title: issue.message, color: 'error', icon: 'i-ph-warning' })
    }
    return
  }
  loading.value = true
  try {
    const data = await register(form, { remember: true })
    if (data.role === 'ADMIN') {
      navigateTo('/admin')
    } else {
      navigateTo('/')
    }
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Đăng ký thất bại', color: 'error', icon: 'i-ph-warning' })
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthShell title="Tạo tài khoản" subtitle="Bắt đầu mua sắm tiện lợi cùng EcoMart">
    <form class="w-full space-y-4" novalidate @submit.prevent="submit">
      <div>
        <label for="register-username" class="mb-1.5 block text-sm font-medium text-gray-700">Tên đăng nhập</label>
        <UInput
          id="register-username"
          v-model="form.username"
          autocomplete="username"
          placeholder="Nhập tên đăng nhập"
          icon="i-ph-user"
          size="lg"
          class="w-full"
        />
      </div>
      <div>
        <label for="register-email" class="mb-1.5 block text-sm font-medium text-gray-700">Email</label>
        <UInput
          id="register-email"
          v-model="form.email"
          type="email"
          autocomplete="email"
          placeholder="Nhập email"
          icon="i-ph-envelope"
          size="lg"
          class="w-full"
        />
      </div>
      <div>
        <label for="register-phone" class="mb-1.5 block text-sm font-medium text-gray-700">Số điện thoại</label>
        <UInput
          id="register-phone"
          v-model="form.numberPhone"
          type="tel"
          autocomplete="tel"
          placeholder="Nhập số điện thoại"
          icon="i-ph-phone"
          size="lg"
          class="w-full"
        />
      </div>
      <div>
        <label for="register-password" class="mb-1.5 block text-sm font-medium text-gray-700">Mật khẩu</label>
        <PasswordInput
          id="register-password"
          v-model="form.password"
          autocomplete="new-password"
          placeholder="Nhập mật khẩu"
          icon="i-ph-lock"
          size="lg"
        />
      </div>
      <UButton type="submit" color="primary" size="xl" block :loading="loading" label="Đăng ký" class="mt-2" />
    </form>

    <p class="mt-6 text-center text-sm text-gray-500">
      Đã có tài khoản?
      <NuxtLink to="/login" class="font-semibold text-emerald-700 hover:underline">Đăng nhập</NuxtLink>
    </p>
  </AuthShell>
</template>