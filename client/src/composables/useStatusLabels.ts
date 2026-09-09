import type { OrderStatus, PaymentStatus, PaymentMethod } from '@/types'

type BadgeColor = 'primary' | 'secondary' | 'success' | 'info' | 'warning' | 'error' | 'neutral'
type BadgeType = 'primary' | 'info' | 'success' | 'warning' | 'error' | 'default'

export const useStatusLabels = () => {
  const orderStatus: Record<OrderStatus, { label: string; color: BadgeColor }> = {
    PENDING: { label: 'Chờ xác nhận', color: 'warning' },
    CONFIRMED: { label: 'Đã xác nhận', color: 'primary' },
    SHIPPING: { label: 'Đang giao hàng', color: 'info' },
    COMPLETED: { label: 'Hoàn thành', color: 'success' },
    CANCELLED: { label: 'Đã hủy', color: 'error' }
  }

  const paymentStatus: Record<PaymentStatus, { label: string; color: BadgeColor }> = {
    PENDING: { label: 'Chờ thanh toán', color: 'warning' },
    PAID: { label: 'Đã thanh toán', color: 'success' },
    FAILED: { label: 'Thanh toán lỗi', color: 'error' },
    CANCELLED: { label: 'Đã hủy', color: 'neutral' }
  }

  const paymentMethod: Record<PaymentMethod, string> = {
    PAYOS: 'PayOS (QR)',
    COD: 'COD'
  }

  const badgeType: Record<BadgeColor, BadgeType> = {
    primary: 'primary',
    secondary: 'default',
    success: 'success',
    info: 'info',
    warning: 'warning',
    error: 'error',
    neutral: 'default'
  }

  return {
    orderStatus,
    paymentStatus,
    paymentMethod,
    badgeType
  }
}
