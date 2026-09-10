import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AddToCartButton from './AddToCartButton.vue'

const global = {
  stubs: { UiIcon: { template: '<span />' } }
}

describe('AddToCartButton', () => {
  it('shows the add-to-cart label and enables the button while in stock', () => {
    const wrapper = mount(AddToCartButton, { props: { stock: 5 }, global })
    expect(wrapper.text()).toContain('Thêm vào giỏ')
    expect(wrapper.find('button').attributes('disabled')).toBeUndefined()
  })

  it('shows out-of-stock label and disables the button at stock zero', () => {
    const wrapper = mount(AddToCartButton, { props: { stock: 0 }, global })
    expect(wrapper.text()).toContain('Hết hàng')
    expect(wrapper.find('button').attributes('disabled')).toBeDefined()
  })

  it('disables the button while loading', () => {
    const wrapper = mount(AddToCartButton, { props: { stock: 5, loading: true }, global })
    expect(wrapper.find('button').attributes('disabled')).toBeDefined()
  })

  it('emits the add event on click', async () => {
    const wrapper = mount(AddToCartButton, { props: { stock: 5 }, global })
    await wrapper.find('button').trigger('click')
    expect(wrapper.emitted('add')).toHaveLength(1)
  })

  it('does not emit add when out of stock', async () => {
    const wrapper = mount(AddToCartButton, { props: { stock: 0 }, global })
    await wrapper.find('button').trigger('click')
    expect(wrapper.emitted('add')).toBeUndefined()
  })
})