import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import OrderSummaryCard from './OrderSummaryCard.vue'

describe('OrderSummaryCard', () => {
  it('renders formatted subtotal, item count and shipping note', () => {
    const wrapper = mount(OrderSummaryCard, { props: { subtotal: 25000, itemCount: 2 } })
    expect(wrapper.text()).toContain('25.000\u00A0₫')
    expect(wrapper.text()).toContain('Tạm tính (2 món)')
    expect(wrapper.text()).toContain('Tính khi đặt hàng')
    expect(wrapper.text()).toContain('Tổng cộng')
  })

  it('omits the item count when not provided', () => {
    const wrapper = mount(OrderSummaryCard, { props: { subtotal: 50000 } })
    expect(wrapper.text()).toContain('Tạm tính')
    expect(wrapper.text()).not.toContain('Tạm tính (')
  })

  it('uses the title prop as the default heading', () => {
    const wrapper = mount(OrderSummaryCard, { props: { subtotal: 50000, title: 'Tóm tắt đơn hàng' } })
    expect(wrapper.text()).toContain('Tóm tắt đơn hàng')
  })

  it('renders items and actions slots', () => {
    const wrapper = mount(OrderSummaryCard, {
      props: { subtotal: 50000 },
      slots: {
        items: '<p>1x Bơ sáp</p>',
        actions: '<button>Thanh toán</button>'
      }
    })
    expect(wrapper.text()).toContain('1x Bơ sáp')
    expect(wrapper.text()).toContain('Thanh toán')
  })
})