import type { OrderStatus, PaymentStatus, PaymentMethod, NotificationType } from '~/types'

type BadgeColor = 'primary' | 'secondary' | 'success' | 'info' | 'warning' | 'error' | 'neutral'

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

  const notificationType: Record<NotificationType, string> = {
    ORDER: 'Đơn hàng',
    PROMO: 'Khuyến mãi',
    SYSTEM: 'Hệ thống'
  }

  return {
    orderStatus,
    paymentStatus,
    paymentMethod,
    notificationType
  }
}
