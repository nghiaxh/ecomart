import type { OrderStatus, PaymentStatus, PaymentMethod, NotificationType, PointTransactionType } from '~/types'

export const useStatusLabels = () => {
  const orderStatus: Record<OrderStatus, { label: string; color: string }> = {
    PENDING: { label: 'Chờ xác nhận', color: 'warning' },
    CONFIRMED: { label: 'Đã xác nhận', color: 'primary' },
    SHIPPING: { label: 'Đang giao hàng', color: 'info' },
    COMPLETED: { label: 'Hoàn thành', color: 'success' },
    CANCELLED: { label: 'Đã hủy', color: 'error' }
  }

  const paymentStatus: Record<PaymentStatus, { label: string; color: string }> = {
    PENDING: { label: 'Chờ thanh toán', color: 'warning' },
    PAID: { label: 'Đã thanh toán', color: 'success' },
    FAILED: { label: 'Thanh toán lỗi', color: 'error' },
    CANCELLED: { label: 'Đã hủy', color: 'gray' }
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

  const pointType: Record<PointTransactionType, string> = {
    EARN: 'Nhận điểm',
    REDEEM: 'Dùng điểm'
  }

  return {
    orderStatus,
    paymentStatus,
    paymentMethod,
    notificationType,
    pointType
  }
}
