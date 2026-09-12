import { describe, it, expect, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import type { ChatMessage } from '@/types'

const mockRoutePath = vi.hoisted(() => ({ value: '/' }))
const sendMock = vi.hoisted(() => vi.fn())
const toastAddMock = vi.hoisted(() => vi.fn())

vi.mock('vue-router', () => ({ useRoute: () => ({ path: mockRoutePath.value }) }))
vi.mock('@/composables/useChat', () => ({ useChat: () => ({ send: sendMock }) }))
vi.mock('@/composables/useToast', () => ({ useToast: () => ({ add: toastAddMock }) }))

import ChatWidget from './ChatWidget.vue'

const global = {
  stubs: {
    UiIcon: { template: '<i class="ui-icon" />' },
    NButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
    NInput: { template: '<input />' }
  }
}

const mountWidget = () => mount(ChatWidget, { global })

describe('ChatWidget', () => {
  it('does not render on admin routes', () => {
    mockRoutePath.value = '/admin/products'
    const wrapper = mountWidget()
    expect(wrapper.text()).toBe('')
  })

  it('opens the panel with a greeting and suggestions', async () => {
    mockRoutePath.value = '/'
    const wrapper = mountWidget()
    expect(wrapper.text()).not.toContain('Chào bạn!')

    await wrapper.find('button').trigger('click')

    expect(wrapper.text()).toContain('Trợ lý EcoMart')
    expect(wrapper.text()).toContain('Chào bạn!')
    expect(wrapper.text()).toContain('Phí giao hàng là bao nhiêu?')
  })

  it('sends the chosen suggestion as a user message', async () => {
    mockRoutePath.value = '/'
    const wrapper = mountWidget()
    await wrapper.find('button').trigger('click')

    const suggestion = wrapper.findAll('button').find((b) => b.text().includes('Hướng dẫn thanh toán'))!
    await suggestion.trigger('click')
    await flushPromises()

    expect(sendMock).toHaveBeenCalledTimes(1)
    const [history, handlers, signal] = sendMock.mock.calls[0]
    const lastUser = [...history].reverse().find((m: ChatMessage) => m.role === 'user')
    expect(lastUser.content).toBe('Hướng dẫn thanh toán')
    expect(typeof handlers.onText).toBe('function')
    expect(typeof handlers.onSource).toBe('function')
    expect(typeof handlers.onDone).toBe('function')
    expect(signal).toBeInstanceOf(AbortSignal)
    expect(wrapper.text()).toContain('Hướng dẫn thanh toán')
  })

  it('shows a fallback and a toast when streaming fails', async () => {
    mockRoutePath.value = '/'
    sendMock.mockRejectedValueOnce(new Error('network down'))
    const wrapper = mountWidget()
    await wrapper.find('button').trigger('click')

    const suggestion = wrapper.findAll('button').find((b) => b.text().includes('Phí giao hàng'))!
    await suggestion.trigger('click')
    await flushPromises()

    expect(toastAddMock).toHaveBeenCalledWith(expect.objectContaining({ severity: 'error' }))
    expect(wrapper.text()).toContain('Xin lỗi, mình chưa thể kết nối ngay')
  })
})