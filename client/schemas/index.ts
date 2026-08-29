import { z } from 'zod'

export const loginSchema = z.object({
  email: z.string().email('Email không hợp lệ'),
  password: z.string().min(1, 'Vui lòng nhập mật khẩu')
})

export const registerSchema = z.object({
  username: z.string().min(3, 'Tên đăng nhập tối thiểu 3 ký tự').max(50),
  email: z.string().email('Email không hợp lệ'),
  numberPhone: z.string().min(10, 'Số điện thoại không hợp lệ').max(15),
  password: z.string().min(6, 'Mật khẩu tối thiểu 6 ký tự').max(100)
})

export const addressSchema = z.object({
  label: z.string().min(1, 'Vui lòng nhập nhãn địa chỉ'),
  street: z.string().min(1, 'Vui lòng nhập số nhà, đường'),
  ward: z.string().min(1, 'Vui lòng nhập phường/xã'),
  district: z.string().min(1, 'Vui lòng nhập quận/huyện'),
  city: z.string().min(1, 'Vui lòng nhập tỉnh/thành phố'),
  receiverName: z.string().min(1, 'Vui lòng nhập tên người nhận'),
  receiverPhone: z.string().min(10, 'Số điện thoại không hợp lệ'),
  isDefault: z.boolean().optional()
})

export const reviewSchema = z.object({
  rating: z.number().min(1, 'Chọn số sao').max(5),
  content: z.string().max(1000).optional().or(z.literal(''))
})

export const profileSchema = z.object({
  username: z.string().min(3).max(50),
  numberPhone: z.string().min(10).max(15),
  avatarUrl: z.string().optional(),
  currentPassword: z.string().optional(),
  newPassword: z.string().min(6).max(100).optional().or(z.literal(''))
})

export const productSchema = z.object({
  name: z.string().min(1, 'Vui lòng nhập tên sản phẩm'),
  slug: z.string().min(1, 'Vui lòng nhập slug'),
  price: z.coerce.number().positive('Giá phải lớn hơn 0'),
  stock: z.coerce.number().min(0, 'Tồn kho không âm'),
  carbonIndex: z.coerce.number().optional(),
  baselineCarbonIndex: z.coerce.number().optional(),
  ecoPointsPerUnit: z.coerce.number().optional(),
  weight: z.coerce.number().optional(),
  origin: z.string().optional(),
  categoryId: z.coerce.number().positive('Chọn danh mục'),
  active: z.boolean().optional(),
  description: z.string().optional()
})

export const categorySchema = z.object({
  name: z.string().min(1, 'Vui lòng nhập tên'),
  slug: z.string().min(1, 'Vui lòng nhập slug'),
  parentId: z.coerce.number().optional().nullable(),
  icon: z.string().optional(),
  displayOrder: z.coerce.number().optional(),
  active: z.boolean().optional()
})

export const bannerSchema = z.object({
  title: z.string().min(1, 'Vui lòng nhập tiêu đề'),
  subtitle: z.string().optional(),
  imageUrl: z.string().min(1, 'Vui lòng nhập URL hình ảnh'),
  linkUrl: z.string().optional(),
  displayOrder: z.coerce.number().optional(),
  active: z.boolean().optional()
})

export type LoginForm = z.infer<typeof loginSchema>
export type RegisterForm = z.infer<typeof registerSchema>
export type AddressForm = z.infer<typeof addressSchema>
