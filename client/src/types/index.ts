export type UserRole = 'CUSTOMER' | 'STAFF' | 'ADMIN'
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'SHIPPING' | 'COMPLETED' | 'CANCELLED'
export type PaymentMethod = 'PAYOS' | 'COD'
export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'CANCELLED'
export type MaterialType = 'ORGANIC' | 'RECYCLED' | 'NATURAL' | 'SYNTHETIC'
export type ChatRole = 'user' | 'assistant'

export interface ChatMessage {
  role: ChatRole
  content: string
}

export interface AuthResponse {
  token: string
  refreshToken: string
  expiresIn: number
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
  type: MaterialType
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
  status: OrderStatus
  payosCheckoutUrl?: string
  message: string
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

export interface AdminDashboard {
  productCount: number
  customerCount: number
  orderCount: number
  revenue: number
}

export interface AdminDailyStat {
  date: string
  revenue: number
  orderCount: number
}

export interface AdminTopProduct {
  productId: number
  name: string
  quantity: number
  revenue: number
}

export interface AdminStatistics {
  revenueSeries: AdminDailyStat[]
  ordersByStatus: Record<OrderStatus, number>
  topProducts: AdminTopProduct[]
}

export interface UserSummary {
  id: number
  username: string
  email: string
  numberPhone: string
  avatarUrl?: string
  role: UserRole
  isActive: boolean
  createdAt: string
  hireDate?: string
}

export interface ActivityLog {
  id: number
  userId?: number
  username?: string
  role?: string
  action: string
  entityType?: string
  entityId?: number
  entityName?: string
  detail?: string
  createdAt: string
}

export interface CreateUserRequest {
  username: string
  email: string
  numberPhone: string
  password: string
  role: UserRole
  hireDate?: string
}

export interface UpdateUserRequest {
  username: string
  email: string
  numberPhone: string
  role: UserRole
  password?: string
  hireDate?: string
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
  slug?: string
  icon?: string
  displayOrder?: number
  active: boolean
}
