import { describe, it, expect } from 'vitest'
import { useStatusLabels } from './useStatusLabels'

describe('useStatusLabels', () => {
  it('maps every order status to a Vietnamese label and color', () => {
    const { orderStatus } = useStatusLabels()
    expect(orderStatus.PENDING).toEqual({ label: 'Chờ xác nhận', color: 'warning' })
    expect(orderStatus.CONFIRMED.label).toBe('Đã xác nhận')
    expect(orderStatus.SHIPPING.label).toBe('Đang giao hàng')
    expect(orderStatus.COMPLETED.color).toBe('success')
    expect(orderStatus.CANCELLED).toEqual({ label: 'Đã hủy', color: 'error' })
  })

  it('maps every payment status to a Vietnamese label and color', () => {
    const { paymentStatus } = useStatusLabels()
    expect(paymentStatus.PENDING).toEqual({ label: 'Chờ thanh toán', color: 'warning' })
    expect(paymentStatus.PAID).toEqual({ label: 'Đã thanh toán', color: 'success' })
    expect(paymentStatus.FAILED).toEqual({ label: 'Thanh toán lỗi', color: 'error' })
    expect(paymentStatus.CANCELLED.label).toBe('Đã hủy')
  })

  it('maps both payment methods to display labels', () => {
    const { paymentMethod } = useStatusLabels()
    expect(paymentMethod.PAYOS).toBe('PayOS (QR)')
    expect(paymentMethod.COD).toBe('COD')
  })

  it('translates every badge color to an NTag type', () => {
    const { badgeType } = useStatusLabels()
    expect(badgeType.primary).toBe('primary')
    expect(badgeType.secondary).toBe('default')
    expect(badgeType.neutral).toBe('default')
    expect(badgeType.success).toBe('success')
    expect(badgeType.info).toBe('info')
    expect(badgeType.warning).toBe('warning')
    expect(badgeType.error).toBe('error')
  })
})