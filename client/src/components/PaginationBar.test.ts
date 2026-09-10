import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PaginationBar from './PaginationBar.vue'

const global = {
  stubs: {
    NButton: {
      name: 'NButtonStub',
      props: ['disabled'],
      emits: ['click'],
      template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>'
    },
    UiIcon: { template: '<span />' }
  }
}

describe('PaginationBar', () => {
  it('renders the one-based current page and total pages', () => {
    const wrapper = mount(PaginationBar, { props: { page: 1, totalPages: 5 }, global })
    expect(wrapper.text()).toContain('Trang 2/5')
  })

  it('disables prev on the first page and next on the last page', () => {
    const first = mount(PaginationBar, { props: { page: 0, totalPages: 3 }, global })
    expect(first.get('[aria-label="Trang trước"]').attributes('disabled')).toBeDefined()
    expect(first.get('[aria-label="Trang sau"]').attributes('disabled')).toBeUndefined()

    const last = mount(PaginationBar, { props: { page: 2, totalPages: 3 }, global })
    expect(last.get('[aria-label="Trang sau"]').attributes('disabled')).toBeDefined()
    expect(last.get('[aria-label="Trang trước"]').attributes('disabled')).toBeUndefined()
  })

  it('enables both buttons on an interior page', () => {
    const wrapper = mount(PaginationBar, { props: { page: 1, totalPages: 3 }, global })
    expect(wrapper.get('[aria-label="Trang trước"]').attributes('disabled')).toBeUndefined()
    expect(wrapper.get('[aria-label="Trang sau"]').attributes('disabled')).toBeUndefined()
  })

  it('emits prev and next events', async () => {
    const wrapper = mount(PaginationBar, { props: { page: 1, totalPages: 3 }, global })
    await wrapper.get('[aria-label="Trang trước"]').trigger('click')
    await wrapper.get('[aria-label="Trang sau"]').trigger('click')
    expect(wrapper.emitted('prev')).toHaveLength(1)
    expect(wrapper.emitted('next')).toHaveLength(1)
  })
})