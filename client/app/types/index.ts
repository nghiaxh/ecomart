export type UserRole = 'CUSTOMER' | 'ADMIN'
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'SHIPPING' | 'COMPLETED' | 'CANCELLED'
export type PaymentMethod = 'PAYOS' | 'COD'
export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'CANCELLED'
export type NotificationType = 'ORDER' | 'PROMO' | 'SYSTEM'
export type ChatRole = 'USER' | 'BOT' | 'SYSTEM'
export type MaterialType = 'ORGANIC' | 'RECYCLED' | 'NATURAL' | 'SYNTHETIC'

export interface AuthResponse {
  token: string
  id: number
  username: string
  email: string
  numberPhone: string
  avatarUrl?: string
  role: UserRole
}

export interface ProfileResponse {
  id: number
  username: string
  email: string
  numberPhone: string
  avatarUrl?: string
  role: UserRole
  createdAt: string
}

export interface CategoryResponse {
  id: number
  name: string
  slug: string
  icon?: string
  displayOrder: number
  active: boolean
  children: CategoryResponse[]
}

export interface ProductMaterial {
  id: number
  name: string
  percentage: number
  type: string
}

export interface Product {
  id: number
  name: string
  slug: string
  description?: string
  price: number
  stock: number
  weight: number
  origin?: string
  categoryId: number
  categoryName: string
  categorySlug: string
  active: boolean
  images: string[]
  materials: ProductMaterial[]
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface CartItem {
  productId: number
  productName: string
  productSlug: string
  imageUrl: string
  price: number
  quantity: number
  stock: number
}

export interface Cart {
  items: CartItem[]
  subtotal: number
  itemCount: number
}

export interface Banner {
  id: number
  title: string
  subtitle?: string
  imageUrl: string
  linkUrl?: string
  displayOrder: number
  active: boolean
}

export interface Address {
  id: number
  label: string
  street: string
  ward: string
  district: string
  city: string
  receiverName: string
  receiverPhone: string
  isDefault: boolean
}

export interface OrderPayment {
  method: PaymentMethod
  status: PaymentStatus
  amount: number
  payosOrderCode?: string
  paidAt?: string
}

export interface OrderItem {
  productId: number
  productName: string
  imageUrl: string
  unitPrice: number
  quantity: number
}

export interface Order {
  id: number
  receiverName: string
  receiverPhone: string
  address: string
  status: OrderStatus
  subtotal: number
  shippingFee: number
  total: number
  notes?: string
  createdAt: string
  payment: OrderPayment
  items: OrderItem[]
}

export interface CheckoutResult {
  orderId: number
  status: string
  payosCheckoutUrl?: string
  message: string
}

export interface NotificationItem {
  id: number
  title: string
  message: string
  type: NotificationType
  read: boolean
  createdAt: string
}

export interface Review {
  id: number
  customerId: number
  customerName: string
  rating: number
  content?: string
  hidden: boolean
  createdAt: string
}

export interface ChatMessage {
  id: number
  role: ChatRole
  content: string
  createdAt: string
}

export interface ChatSession {
  id: number
  title: string
  createdAt: string
  messages: ChatMessage[]
}

export interface ChatResponse {
  botMessage: string
  sessionId: number
  messages: ChatMessage[]
}

export interface AdminDashboard {
  productCount: number
  customerCount: number
  orderCount: number
  revenue: number
}

export interface ProductRequest {
  name: string
  slug: string
  description?: string
  price: number
  stock: number
  weight?: number
  origin?: string
  categoryId: number
  active: boolean
  images?: { url: string; primary: boolean; displayOrder?: number }[]
  materials?: { materialId: number; percentage: number }[]
}

export interface CategoryRequest {
  parentId?: number
  name: string
  slug: string
  icon?: string
  displayOrder?: number
  active: boolean
}

export interface BannerRequest {
  title: string
  subtitle?: string
  imageUrl: string
  linkUrl?: string
  displayOrder?: number
  active: boolean
}

export interface AddressRequest {
  label: string
  street: string
  ward: string
  district: string
  city: string
  receiverName: string
  receiverPhone: string
  isDefault: boolean
}
