<script setup lang="ts">
import type { ProfileResponse, Address } from '@/types'
import { addressSchema, profileSchema, type AddressForm } from '@/schemas'
import { useApi } from '@/composables/useApi'
import { useAuth } from '@/composables/useAuth'
import { useFormErrors } from '@/composables/useFormErrors'
import { useConfirm } from '@/composables/useConfirm'
import { useFormat } from '@/composables/useFormat'

const { request } = useApi()
const toast = useToast()
const { session, updateSession, isAdmin, logout } = useAuth()
const { errors, applyIssues, clearErrors } = useFormErrors()
const { confirm } = useConfirm()
const { formatDate } = useFormat()

const profile = ref<ProfileResponse | null>(null)
const addresses = ref<Address[]>([])
const loading = ref(true)

const editMode = ref(false)
const saving = ref(false)
const profileForm = reactive({
  username: '', numberPhone: '', avatarUrl: '', currentPassword: '', newPassword: ''
})

const showAddressForm = ref(false)
const editingAddressId = ref<number | null>(null)
const savingAddress = ref(false)
const addressUIBusy = ref<Set<number>>(new Set())
const addressForm = ref<AddressForm>({
  label: '', street: '', ward: '', district: '', city: '', receiverName: '', receiverPhone: '', isDefault: false
})

async function load() {
  loading.value = true
  try {
    const [p, a] = await Promise.all([
      request<ProfileResponse>('/api/profile'),
      request<Address[]>('/api/addresses')
    ])
    profile.value = p
    addresses.value = a
    resetProfileForm(p)
  } finally {
    loading.value = false
  }
}

function resetProfileForm(p = profile.value) {
  if (!p) return
  Object.assign(profileForm, { username: p.username, numberPhone: p.numberPhone, avatarUrl: p.avatarUrl || '', currentPassword: '', newPassword: '' })
}

function startEdit() {
  resetProfileForm()
  clearErrors()
  editMode.value = true
}

async function saveProfile() {
  clearErrors()
  const result = profileSchema.safeParse(profileForm)
  if (!result.success) {
    applyIssues(result.error)
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

function resetAddressForm() {
  addressForm.value = { label: '', street: '', ward: '', district: '', city: '', receiverName: '', receiverPhone: '', isDefault: false }
  editingAddressId.value = null
  clearErrors()
}

function openCreateAddress() {
  resetAddressForm()
  showAddressForm.value = true
}

function openEditAddress(a: Address) {
  editingAddressId.value = a.id
  Object.assign(addressForm.value, {
    label: a.label, street: a.street, ward: a.ward, district: a.district, city: a.city,
    receiverName: a.receiverName, receiverPhone: a.receiverPhone, isDefault: a.isDefault
  })
  clearErrors()
  showAddressForm.value = true
}

async function saveAddress() {
  clearErrors()
  const result = addressSchema.safeParse(addressForm.value)
  if (!result.success) {
    applyIssues(result.error)
    return
  }
  savingAddress.value = true
  const payload = addressForm.value
  try {
    if (editingAddressId.value) {
      await request(`/api/addresses/${editingAddressId.value}`, { method: 'PUT', body: payload })
      toast.add({ title: 'Đã cập nhật địa chỉ', color: 'success' })
    } else {
      await request('/api/addresses', { method: 'POST', body: payload })
      toast.add({ title: 'Đã thêm địa chỉ', color: 'success' })
    }
    showAddressForm.value = false
    await load()
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Không thể lưu địa chỉ', color: 'error' })
  } finally {
    savingAddress.value = false
  }
}

async function deleteAddress(a: Address) {
  if (addressUIBusy.value.has(a.id)) return
  if (!await confirm(`Xóa địa chỉ "${a.label}"?`, 'Xóa địa chỉ')) return
  addressUIBusy.value.add(a.id)
  try {
    await request(`/api/addresses/${a.id}`, { method: 'DELETE' })
    toast.add({ title: 'Đã xóa địa chỉ', color: 'success' })
    await load()
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Không thể xóa địa chỉ', color: 'error' })
  } finally {
    addressUIBusy.value.delete(a.id)
  }
}

async function setDefaultAddress(a: Address) {
  if (addressUIBusy.value.has(a.id)) return
  addressUIBusy.value.add(a.id)
  try {
    await request(`/api/addresses/${a.id}/default`, { method: 'PATCH' })
    toast.add({ title: 'Đã đặt làm mặc định', color: 'success' })
    await load()
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Không thể đặt mặc định', color: 'error' })
  } finally {
    addressUIBusy.value.delete(a.id)
  }
}

onMounted(load)
</script>

<template>
  <div class="mx-auto max-w-5xl px-4 py-8 sm:px-6">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-extrabold text-gray-700">Tài khoản</h1>
      <span v-if="profile" class="text-xs text-gray-400">Tham gia {{ formatDate(profile.createdAt) }}</span>
    </div>

    <!-- Loading skeleton -->
    <div v-if="loading" class="mt-8 grid gap-8 lg:grid-cols-3">
      <div class="lg:col-span-2 space-y-4">
        <USkeleton class="h-52 rounded-2xl" />
      </div>
      <div class="space-y-4">
        <USkeleton class="h-36 rounded-2xl" />
        <USkeleton class="h-48 rounded-2xl" />
      </div>
    </div>

    <div v-else class="mt-8 grid gap-8 lg:grid-cols-3">
      <!-- Profile -->
      <div class="lg:col-span-2 space-y-6">
        <div class="rounded-2xl border border-emerald-100 bg-white p-6">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-bold text-gray-700">Thông tin cá nhân</h2>
            <UButton v-if="!editMode" color="primary" variant="soft" size="md" label="Chỉnh sửa" icon="i-ph-pencil-simple" @click="startEdit" />
          </div>

          <form v-if="editMode" class="mt-4 space-y-3" @submit.prevent="saveProfile">
            <div class="flex items-center gap-4">
              <UAvatar :src="profileForm.avatarUrl || undefined" :alt="profileForm.username" size="xl" />
              <div class="flex-1 space-y-1">
                <UInput v-model="profileForm.avatarUrl" label="Avatar URL" placeholder="https://..." />
                <p class="text-xs text-gray-400">Dán URL ảnh đại diện để xem trước.</p>
              </div>
            </div>
            <div>
              <UInput v-model="profileForm.username" label="Tên đăng nhập" placeholder="Tên đăng nhập" />
              <p v-if="errors.username" class="text-xs text-red-600">{{ errors.username }}</p>
            </div>
            <div>
              <UInput v-model="profileForm.numberPhone" label="Số điện thoại" placeholder="Số điện thoại" />
              <p v-if="errors.numberPhone" class="text-xs text-red-600">{{ errors.numberPhone }}</p>
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

          <div v-else-if="profile" class="mt-4">
            <div class="flex items-center gap-4">
              <UAvatar :src="profile.avatarUrl" :alt="profile.username" size="xl" />
              <div>
                <div class="flex items-center gap-2">
                  <p class="text-lg font-bold text-gray-700">{{ profile.username }}</p>
                  <span
                    class="rounded-full px-2.5 py-0.5 text-xs font-semibold"
                    :class="isAdmin ? 'bg-slate-100 text-slate-700' : 'bg-emerald-100 text-emerald-700'"
                  >{{ isAdmin ? 'Quản trị' : 'Khách hàng' }}</span>
                </div>
                <div class="mt-1 space-y-0.5 text-sm text-gray-400">
                  <p class="flex items-center gap-1.5"><UIcon name="i-ph-envelope" class="h-4 w-4" /> {{ profile.email }}</p>
                  <p v-if="profile.numberPhone" class="flex items-center gap-1.5"><UIcon name="i-ph-phone" class="h-4 w-4" /> {{ profile.numberPhone }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Addresses -->
        <div class="rounded-2xl border border-emerald-100 bg-white p-6">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-bold text-gray-700">Địa chỉ của tôi</h2>
            <UButton color="primary" variant="soft" size="md" icon="i-ph-plus" label="Thêm địa chỉ" @click="openCreateAddress" />
          </div>

          <AddressForm
            v-if="showAddressForm"
            v-model="addressForm"
            :errors="errors"
            :saving="savingAddress"
            @submit="saveAddress"
            @cancel="showAddressForm = false; resetAddressForm()"
          />

          <div v-else-if="addresses.length" class="mt-4 space-y-3">
            <AddressCard
              v-for="a in addresses"
              :key="a.id"
              :address="a"
              editable
              @edit="openEditAddress"
              @delete="deleteAddress"
              @set-default="setDefaultAddress"
            />
          </div>
          <div v-else class="mt-4 rounded-xl border border-dashed border-emerald-200 bg-emerald-50/40 p-6 text-center">
            <UIcon name="i-ph-map-pin" class="mx-auto mb-2 h-8 w-8 text-emerald-300" />
            <p class="text-sm text-gray-500">Chưa có địa chỉ. Thêm địa chỉ để tiết kiệm thời gian khi đặt hàng.</p>
          </div>
        </div>
      </div>

      <!-- Sidebar -->
      <div class="space-y-6">
        <div class="rounded-2xl border border-emerald-100 bg-white p-6">
          <h3 class="font-semibold text-gray-700">Đường dẫn</h3>
          <div class="mt-3 space-y-2">
            <template v-if="!isAdmin">
              <RouterLink to="/orders" class="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-gray-600 hover:bg-emerald-50 hover:text-emerald-700">
                <UIcon name="i-ph-receipt" class="h-4 w-4" /> Đơn hàng
              </RouterLink>
              <RouterLink to="/checkout" class="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-gray-600 hover:bg-emerald-50 hover:text-emerald-700">
                <UIcon name="i-ph-shopping-cart" class="h-4 w-4" /> Giỏ hàng
              </RouterLink>
            </template>
            <RouterLink v-if="isAdmin" to="/admin" class="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-gray-600 hover:bg-emerald-50 hover:text-emerald-700">
              <UIcon name="i-ph-squares-four" class="h-4 w-4" /> Quản trị
            </RouterLink>
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
      </div>
    </div>
  </div>
</template>
