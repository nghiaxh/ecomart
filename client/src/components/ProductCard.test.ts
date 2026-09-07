import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ProductCard from './ProductCard.vue'

const product = {
  id: 1,
  name: 'Bơ sáp',
  slug: 'bo-sap',
  price: 25000,
  stock: 10,
  weight: 0.5,
  categoryId: 1,
  categoryName: 'Trái cây',
  categorySlug: 'trai-cay',
  active: true,
  images: ['/images/bo.jpg'],
  materials: []
}

const global = {
  stubs: {
    RouterLink: { template: '<a><slot /></a>' },
    UiImg: { template: '<img />' }
  }
}

describe('ProductCard', () => {
  it('renders product name and formatted price', () => {
    const wrapper = mount(ProductCard, { props: { product }, global })
    expect(wrapper.text()).toContain('Bơ sáp')
    expect(wrapper.text()).toContain('25.000\u00A0₫')
  })

  it('shows out-of-stock badge when stock is zero', () => {
    const wrapper = mount(ProductCard, { props: { product: { ...product, stock: 0 } }, global })
    expect(wrapper.text()).toContain('Hết hàng')
  })
})
