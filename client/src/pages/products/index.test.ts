import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const { requestMock, toastMock, routerReplaceMock } = vi.hoisted(() => ({
  requestMock: vi.fn(),
  toastMock: vi.fn(),
  routerReplaceMock: vi.fn()
}))

vi.mock('@/composables/useApi', () => ({
  useApi: () => ({ request: requestMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ query: {}, params: {} }),
  useRouter: () => ({ replace: routerReplaceMock })
}))

vi.mock('@nuxt/ui/composables/useToast', () => ({
  useToast: () => ({ add: toastMock })
}))

import ProductsIndex from './index.vue'

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

const global = {
  stubs: {
    UButton: {
      props: ['label'],
      template: '<button @click="$emit(\'click\')">{{ label }}</button>'
    },
    UInput: { template: '<input />' },
    USkeleton: { template: '<div />' },
    UIcon: { template: '<div />' },
    ProductCard: {
      props: ['product'],
      template: '<div>{{ product.name }}</div>'
    },
    PaginationBar: { template: '<div />' }
  }
}

function mockCatalog(pagePayload: unknown) {
  requestMock.mockImplementation((url: string) => {
    if (url.startsWith('/api/categories')) return Promise.resolve([])
    return Promise.resolve(pagePayload)
  })
}

async function clickFilter(wrapper: Awaited<ReturnType<typeof mount>>) {
  const filter = wrapper.findAll('button').find((b: { text: () => string }) => b.text().includes('Lọc'))
  expect(filter).toBeTruthy()
  await filter!.trigger('click')
}

describe('products index', () => {
  beforeEach(() => {
    requestMock.mockReset()
    toastMock.mockReset()
    routerReplaceMock.mockReset()
    routerReplaceMock.mockResolvedValue(undefined)
  })

  it('shows empty state when initial load fails', async () => {
    requestMock.mockImplementation((url: string) => {
      if (url.startsWith('/api/categories')) return Promise.resolve([])
      return Promise.reject({ data: { message: 'Lỗi máy chủ' } })
    })
    const wrapper = mount(ProductsIndex, { global })
    await flushPromises()
    expect(wrapper.text()).toContain('Không tìm thấy sản phẩm phù hợp.')
  })

  it('keeps stale list and toasts in Vietnamese when reload fails', async () => {
    mockCatalog(productPage)
    const wrapper = mount(ProductsIndex, { global })
    await flushPromises()
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

  it('sends numeric id when clicking a parent category chip', async () => {
    const categories = [
      {
        id: 1, name: 'Rau củ sạch', slug: 'rau-cu-sach', displayOrder: 1, active: true,
        children: [
          { id: 11, name: 'Rau xanh', slug: 'rau-xanh', displayOrder: 1, active: true, children: [] }
        ]
      }
    ]
    requestMock.mockImplementation((url: string) => {
      if (url.startsWith('/api/categories')) return Promise.resolve(categories)
      return Promise.resolve(productPage)
    })
    const wrapper = mount(ProductsIndex, { global })
    await flushPromises()
    requestMock.mockClear()

    const chip = wrapper.findAll('button').find((b: { text: () => string }) => b.text().includes('Rau củ sạch'))
    expect(chip).toBeTruthy()
    await chip!.trigger('click')
    await flushPromises()
    const calledUrl = requestMock.mock.calls.map((c: unknown[]) => String(c[0])).find((u: string) => u.startsWith('/api/products'))
    expect(calledUrl).toContain('category=1')
    expect(calledUrl).not.toContain('category=rau-cu-sach')
  })
})