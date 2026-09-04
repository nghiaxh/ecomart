import { describe, it, expect } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
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

describe('ProductCard', () => {
  it('renders product name and formatted price', async () => {
    const wrapper = await mountSuspended(ProductCard, { props: { product } })
    expect(wrapper.text()).toContain('Bơ sáp')
    expect(wrapper.text()).toContain(`25.000\u00A0₫`)
  })
})