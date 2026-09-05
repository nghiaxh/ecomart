<script setup lang="ts">
import type { ProfileResponse, Address } from '~/types'
import { profileSchema } from '~/schemas'

definePageMeta({ middleware: 'auth' })

const { request } = useApi()
const toast = useToast()
const { session, updateSession, isAdmin, logout } = useAuth()

const profile = ref<ProfileResponse | null>(null)
const addresses = ref<Address[]>([])
const loading = ref(true)

const editMode = ref(false)
const saving = ref(false)
const profileForm = reactive({
  username: '', numberPhone: '', avatarUrl: '', currentPassword: '', newPassword: ''
})
const errors = ref<Record<string, string>>({})

async function load() {
  loading.value = true
  try {
    const [p, a] = await Promise.all([
      request<ProfileResponse>('/api/profile'),
      request<Address[]>('/api/addresses')
    ])
    profile.value = p
    addresses.value = a
    Object.assign(profileForm, { username: p.username, numberPhone: p.numberPhone, avatarUrl: p.avatarUrl || '', currentPassword: '', newPassword: '' })
  } finally {
    loading.value = false
  }
}

function startEdit() {
  if (profile.value) {
    Object.assign(profileForm, { username: profile.value.username, numberPhone: profile.value.numberPhone, avatarUrl: profile.value.avatarUrl || '', currentPassword: '', newPassword: '' })
  }
  editMode.value = true
}

async function saveProfile() {
  errors.value = {}
  const result = profileSchema.safeParse(profileForm)
  if (!result.success) {
    for (const issue of result.error.issues) errors.value[String(issue.path[0])] = issue.message
    return
  }
  saving.value = true
  try {
    const updated = await request<ProfileResponse>('/api/profile', { method: 'PUT', body: profileForm })
    profile.value = updated
    updateSession({ username: updated.username, avatarUrl: updated.avatarUrl })
    editMode.value = false
    toast.add({ title: 'Cập nhật thành công', color: 'success' })
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Cập nhật thất bại', color: 'error' })
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="mx-auto max-w-5xl px-4 py-8 sm:px-6">
    <h1 class="text-2xl font-extrabold text-gray-800">Tài khoản</h1>

    <!-- Loading skeleton -->
    <div v-if="loading" class="mt-8 grid gap-8 lg:grid-cols-3">
      <div class="lg:col-span-2 space-y-4">
        <USkeleton class="h-48 rounded-2xl" />
      </div>
      <div class="space-y-4">
        <USkeleton class="h-36 rounded-2xl" />
        <USkeleton class="h-36 rounded-2xl" />
      </div>
    </div>

    <div v-else class="mt-8 grid gap-8 lg:grid-cols-3">
      <!-- Profile -->
      <div class="lg:col-span-2">
        <div class="rounded-2xl border border-emerald-100 bg-white p-6">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-bold text-gray-800">Thông tin cá nhân</h2>
            <UButton v-if="!editMode" color="primary" variant="soft" size="md" label="Chỉnh sửa" icon="i-ph-pencil-simple" @click="startEdit" />
          </div>

          <form v-if="editMode" class="mt-4 space-y-3" @submit.prevent="saveProfile">
            <div>
              <UInput v-model="profileForm.username" label="Tên đăng nhập" placeholder="Tên đăng nhập" />
              <p v-if="errors.username" class="text-xs text-red-600">{{ errors.username }}</p>
            </div>
            <div>
              <UInput v-model="profileForm.numberPhone" label="Số điện thoại" placeholder="Số điện thoại" />
              <p v-if="errors.numberPhone" class="text-xs text-red-600">{{ errors.numberPhone }}</p>
            </div>
            <div>
              <UInput v-model="profileForm.avatarUrl" label="Avatar URL" placeholder="https://..." />
            </div>
            <hr class="border-emerald-100" />
            <p class="text-sm font-semibold text-gray-500">Đổi mật khẩu</p>
            <div>
              <UInput v-model="profileForm.currentPassword" type="password" label="Mật khẩu hiện tại" placeholder="(để trống nếu không đổi)" />
            </div>
            <div>
              <UInput v-model="profileForm.newPassword" type="password" label="Mật khẩu mới" placeholder="(để trống nếu không đổi)" />
              <p v-if="errors.newPassword" class="text-xs text-red-600">{{ errors.newPassword }}</p>
            </div>
            <div class="flex justify-end gap-2 pt-2">
              <UButton color="neutral" variant="ghost" label="Hủy" @click="editMode = false" />
              <UButton type="submit" color="primary" label="Lưu" :loading="saving" />
            </div>
          </form>

          <div v-else-if="profile" class="mt-4 space-y-4">
            <div class="flex items-center gap-4">
              <UAvatar :src="profile.avatarUrl" :alt="profile.username" size="lg" />
              <div>
                <div class="flex items-center gap-2">
                  <p class="text-lg font-bold text-gray-800">{{ profile.username }}</p>
                  <span
                    class="rounded-full px-2.5 py-0.5 text-xs font-semibold"
                    :class="isAdmin ? 'bg-slate-100 text-slate-700' : 'bg-emerald-100 text-emerald-700'"
                  >{{ isAdmin ? 'Quản trị' : 'Khách hàng' }}</span>
                </div>
                <p class="text-sm text-gray-400">{{ profile.email }}</p>
                <p v-if="profile.numberPhone" class="text-sm text-gray-400">{{ profile.numberPhone }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Sidebar -->
      <div class="space-y-6">
        <div class="rounded-2xl border border-emerald-100 bg-white p-6">
          <h3 class="font-semibold text-gray-800">Đường dẫn</h3>
          <div class="mt-3 space-y-2">
            <template v-if="!isAdmin">
              <NuxtLink to="/orders" class="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-gray-600 hover:bg-emerald-50 hover:text-emerald-700">
                <UIcon name="i-ph-receipt" class="h-4 w-4" /> Đơn hàng
              </NuxtLink>
              <NuxtLink to="/chat" class="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-gray-600 hover:bg-emerald-50 hover:text-emerald-700">
                <UIcon name="i-ph-chats-circle" class="h-4 w-4" /> Chat hỗ trợ
              </NuxtLink>
            </template>
            <NuxtLink v-if="isAdmin" to="/admin" class="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-gray-600 hover:bg-emerald-50 hover:text-emerald-700">
              <UIcon name="i-ph-squares-four" class="h-4 w-4" /> Quản trị
            </NuxtLink>
            <UButton
              color="error"
              variant="ghost"
              icon="i-ph-sign-out"
              label="Đăng xuất"
              class="w-full justify-start"
              @click="logout"
            />
          </div>
        </div>

        <div class="rounded-2xl border border-emerald-100 bg-white p-6">
          <h3 class="font-semibold text-gray-800">Địa chỉ của tôi</h3>
          <div v-if="addresses.length" class="mt-3 space-y-2">
            <div v-for="a in addresses" :key="a.id" class="rounded-lg border border-gray-100 p-3 text-sm" :class="a.isDefault ? 'border-l-2 border-l-emerald-500' : ''">
              <div class="flex items-center gap-2">
                <span class="font-medium text-gray-700">{{ a.label }}</span>
                <span v-if="a.isDefault" class="rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-semibold text-emerald-700">Mặc định</span>
              </div>
              <p class="mt-1 text-xs text-gray-400">{{ a.receiverName }} · {{ a.receiverPhone }}</p>
              <p class="mt-0.5 text-xs text-gray-500">{{ a.street }}, {{ a.ward }}, {{ a.district }}, {{ a.city }}</p>
            </div>
          </div>
          <p v-else class="mt-3 text-sm text-gray-400">Chưa có địa chỉ. Thêm khi đặt hàng.</p>
        </div>
      </div>
    </div>
  </div>
</template>