import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'

const { requestMock, toastMock } = vi.hoisted(() => ({
  requestMock: vi.fn(),
  toastMock: vi.fn()
}))

vi.mock('~/composables/useApi', () => ({
  useApi: () => ({ request: requestMock })
}))

vi.mock('@nuxt/ui/composables/useToast', () => ({
  useToast: () => ({ add: toastMock })
}))

import ProductsIndex from './index.vue'
import { clearNuxtData } from '#app'

const product = {
  id: 1,
  name: 'Rau ngót',
  slug: 'rau-ngot',
  price: 15000,
  stock: 200,
  weight: 1,
  categoryId: 1,
  categoryName: 'Rau xanh',
  categorySlug: 'rau-xanh',
  active: true,
  images: [],
  materials: []
}

const productPage = {
  content: [product],
  page: 0,
  size: 12,
  totalElements: 1,
  totalPages: 1
}

function mockCatalog(pagePayload: unknown) {
  requestMock.mockImplementation((url: string) => {
    if (url.startsWith('/api/categories')) return Promise.resolve([])
    return Promise.resolve(pagePayload)
  })
}

async function clickFilter(wrapper: Awaited<ReturnType<typeof mountSuspended>>) {
  const filter = wrapper.findAll('button').find((b: { text: () => string }) => b.text().includes('Lọc'))
  expect(filter).toBeTruthy()
  await filter!.trigger('click')
}

describe('products index', () => {
  beforeEach(() => {
    clearNuxtData(['product-list-initial', 'product-categories'])
    requestMock.mockReset()
    toastMock.mockReset()
  })

  it('shows empty state when initial load fails', async () => {
    requestMock.mockImplementation((url: string) => {
      if (url.startsWith('/api/categories')) return Promise.resolve([])
      return Promise.reject({ data: { message: 'Lỗi máy chủ' } })
    })
    const wrapper = await mountSuspended(ProductsIndex)
    expect(wrapper.text()).toContain('Không tìm thấy sản phẩm phù hợp.')
  })

  it('keeps stale list and toasts in Vietnamese when reload fails', async () => {
    mockCatalog(productPage)
    const wrapper = await mountSuspended(ProductsIndex)
    expect(wrapper.text()).toContain('Rau ngót')

    requestMock.mockRejectedValue({ data: { message: 'Lỗi máy chủ' } })
    await clickFilter(wrapper)

    await vi.waitFor(() => {
      expect(toastMock).toHaveBeenCalledWith(expect.objectContaining({ color: 'error' }))
    })
    expect(toastMock).toHaveBeenCalledWith(
      expect.objectContaining({ title: expect.stringMatching(/Không thể tải sản phẩm|Lỗi máy chủ/) })
    )
    expect(wrapper.text()).toContain('Rau ngót')
    expect(wrapper.text()).not.toContain('Không tìm thấy sản phẩm phù hợp.')
  })
})
