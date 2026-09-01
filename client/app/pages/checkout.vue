<script setup lang="ts">
import type { Address, Cart, CheckoutResult } from '~/types'
import { addressSchema } from '~/schemas'

definePageMeta({ middleware: 'customer' })

const { request } = useApi()
const { cart, fetchCart, reset } = useCart()
const { formatVND } = useFormat()
const toast = useToast()

const addresses = ref<Address[]>([])
const selectedAddressId = ref<number | null>(null)
const paymentMethod = ref<'PAYOS' | 'COD'>('COD')
const notes = ref('')
const loading = ref(false)
const showAddressForm = ref(false)

const addressForm = reactive({
  label: '', street: '', ward: '', district: '', city: '', receiverName: '', receiverPhone: '', isDefault: false
})
const addressErrors = ref<Record<string, string>>({})

async function loadAddresses() {
  addresses.value = await request<Address[]>('/api/addresses')
  const def = addresses.value.find(a => a.isDefault)
  const first = addresses.value[0]
  selectedAddressId.value = def?.id ?? first?.id ?? null
}

async function loadCart() {
  await fetchCart()
}

async function createAddress() {
  addressErrors.value = {}
  const result = addressSchema.safeParse(addressForm)
  if (!result.success) {
    for (const issue of result.error.issues) addressErrors.value[String(issue.path[0])] = issue.message
    return
  }
  const created = await request<Address>('/api/addresses', { method: 'POST', body: addressForm })
  await loadAddresses()
  if (created.isDefault) await loadCart()
  showAddressForm.value = false
  Object.assign(addressForm, { label: '', street: '', ward: '', district: '', city: '', receiverName: '', receiverPhone: '', isDefault: false })
}

async function checkout() {
  if (!selectedAddressId.value) {
    toast.add({ title: 'Vui lòng chọn địa chỉ giao hàng', color: 'warning' })
    return
  }
  loading.value = true
  try {
    const result = await request<CheckoutResult>('/api/orders/checkout', {
      method: 'POST',
      body: { addressId: selectedAddressId.value, paymentMethod: paymentMethod.value, notes: notes.value }
    })
    if (result.payosCheckoutUrl) {
      window.open(result.payosCheckoutUrl, '_blank')
    }
    await reset()
    toast.add({ title: result.message || 'Đặt hàng thành công!', color: 'success' })
    navigateTo(`/orders/${result.orderId}`)
  } catch (e: any) {
    toast.add({ title: e?.data?.message || 'Đặt hàng thất bại', color: 'error' })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAddresses()
  loadCart()
})
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <h1 class="text-3xl font-extrabold text-gray-800">Thanh toán</h1>

    <div class="mt-8 grid gap-8 lg:grid-cols-3">
      <div class="lg:col-span-2 space-y-6">
        <!-- Address -->
        <section class="rounded-2xl border border-emerald-100 bg-white p-6">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-bold text-gray-800">Địa chỉ giao hàng</h2>
            <UButton color="primary" variant="soft" size="sm" icon="i-ph-plus" label="Thêm mới" @click="showAddressForm = !showAddressForm" />
          </div>

          <form v-if="showAddressForm" class="mt-4 grid gap-3 sm:grid-cols-2" @submit.prevent="createAddress">
            <div>
              <UInput v-model="addressForm.label" placeholder="Nhãn (Nhà, Cơ quan...)" />
              <p v-if="addressErrors.label" class="mt-1 text-xs text-red-600">{{ addressErrors.label }}</p>
            </div>
            <div>
              <UInput v-model="addressForm.receiverName" placeholder="Người nhận" />
              <p v-if="addressErrors.receiverName" class="mt-1 text-xs text-red-600">{{ addressErrors.receiverName }}</p>
            </div>
            <div class="sm:col-span-2">
              <UInput v-model="addressForm.receiverPhone" placeholder="Số điện thoại" />
              <p v-if="addressErrors.receiverPhone" class="mt-1 text-xs text-red-600">{{ addressErrors.receiverPhone }}</p>
            </div>
            <div class="sm:col-span-2">
              <UInput v-model="addressForm.street" placeholder="Số nhà, đường, thôn/xóm" />
              <p v-if="addressErrors.street" class="mt-1 text-xs text-red-600">{{ addressErrors.street }}</p>
            </div>
            <div>
              <UInput v-model="addressForm.ward" placeholder="Phường/Xã" />
              <p v-if="addressErrors.ward" class="mt-1 text-xs text-red-600">{{ addressErrors.ward }}</p>
            </div>
            <div>
              <UInput v-model="addressForm.district" placeholder="Quận/Huyện" />
              <p v-if="addressErrors.district" class="mt-1 text-xs text-red-600">{{ addressErrors.district }}</p>
            </div>
            <div>
              <UInput v-model="addressForm.city" placeholder="Tỉnh/Thành phố" />
              <p v-if="addressErrors.city" class="mt-1 text-xs text-red-600">{{ addressErrors.city }}</p>
            </div>
            <UCheckbox v-model="addressForm.isDefault" label="Đặt làm địa chỉ mặc định" />
            <div class="sm:col-span-2 flex justify-end gap-2">
              <UButton color="neutral" variant="ghost" label="Hủy" @click="showAddressForm = false" />
              <UButton type="submit" color="primary" label="Lưu địa chỉ" />
            </div>
          </form>

          <div v-else class="mt-4 space-y-3">
            <label
              v-for="a in addresses"
              :key="a.id"
              class="flex cursor-pointer items-start gap-3 rounded-xl border p-4 transition"
              :class="selectedAddressId === a.id ? 'border-emerald-500 bg-emerald-50' : 'border-gray-200'"
            >
              <input type="radio" :checked="selectedAddressId === a.id" class="mt-1 accent-emerald-600" @change="selectedAddressId = a.id" />
              <div class="flex-1">
                <div class="flex items-center gap-2">
                  <span class="font-semibold text-gray-800">{{ a.receiverName }}</span>
                  <span class="text-sm text-gray-400">{{ a.receiverPhone }}</span>
                  <span v-if="a.isDefault" class="rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-semibold text-emerald-700">Mặc định</span>
                </div>
                <p class="mt-1 text-sm text-gray-500">{{ a.label }} · {{ a.street }}, {{ a.ward }}, {{ a.district }}, {{ a.city }}</p>
              </div>
            </label>
            <p v-if="!addresses.length" class="py-4 text-center text-gray-400">Chưa có địa chỉ nào. Vui lòng thêm địa chỉ giao hàng.</p>
          </div>
        </section>

        <!-- Payment -->
        <section class="rounded-2xl border border-emerald-100 bg-white p-6">
          <h2 class="text-lg font-bold text-gray-800">Phương thức thanh toán</h2>
          <div class="mt-4 grid gap-3 sm:grid-cols-2">
            <label class="flex items-center gap-3 rounded-xl border p-4 transition" :class="paymentMethod === 'COD' ? 'border-emerald-500 bg-emerald-50' : 'border-gray-200'" >
              <input type="radio" v-model="paymentMethod" value="COD" class="accent-emerald-600" />
              <span class="grid h-9 w-9 place-items-center rounded-lg bg-emerald-100 text-emerald-700"><UIcon name="i-ph-money" class="h-5 w-5" /></span>
              <div>
                <p class="font-semibold text-gray-800">COD</p>
                <p class="text-xs text-gray-400">Thanh toán khi nhận hàng</p>
              </div>
            </label>
            <label class="flex items-center gap-3 rounded-xl border p-4 transition" :class="paymentMethod === 'PAYOS' ? 'border-emerald-500 bg-emerald-50' : 'border-gray-200'" >
              <input type="radio" v-model="paymentMethod" value="PAYOS" class="accent-emerald-600" />
              <span class="grid h-9 w-9 place-items-center rounded-lg bg-emerald-100 text-emerald-700"><UIcon name="i-ph-qr-code" class="h-5 w-5" /></span>
              <div>
                <p class="font-semibold text-gray-800">PayOS QR</p>
                <p class="text-xs text-gray-400">Quét mã chuyển khoản</p>
              </div>
            </label>
          </div>
        </section>
      </div>

      <!-- Summary -->
      <div class="h-fit rounded-2xl border border-emerald-100 bg-white p-6 lg:sticky lg:top-20" v-if="cart">
        <h2 class="text-lg font-bold text-gray-800">Đơn hàng ({{ cart.itemCount }} món)</h2>
        <div class="mt-4 space-y-3 max-h-64 overflow-auto">
          <div v-for="item in cart.items" :key="item.productId" class="flex justify-between text-sm">
            <span class="text-gray-600">{{ item.productName }} <span class="text-gray-400">× {{ item.quantity }}</span></span>
            <span class="font-medium text-gray-700">{{ formatVND(item.price * item.quantity) }}</span>
          </div>
        </div>
        <div class="mt-4 space-y-2 border-t border-emerald-50 pt-4 text-sm">
          <div class="flex justify-between text-gray-500"><span>Tạm tính</span><span>{{ formatVND(cart.subtotal) }}</span></div>
          <div class="flex justify-between text-gray-500"><span>Phí giao hàng</span><span>Miễn phí</span></div>
        </div>
        <div class="mt-4 border-t border-emerald-50 pt-4">
          <div class="flex justify-between text-lg"><span class="font-semibold text-gray-700">Tổng cộng</span><span class="font-bold text-emerald-700">{{ formatVND(cart.subtotal) }}</span></div>
        </div>
        <UTextarea v-model="notes" placeholder="Ghi chú cho đơn hàng (tùy chọn)..." class="mt-4" :rows="2" />
        <UButton color="primary" size="lg" block class="mt-4" :loading="loading" label="Đặt hàng" icon="i-ph-check-circle" @click="checkout" />
      </div>
    </div>
  </div>
</template>