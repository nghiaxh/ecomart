import { describe, it, expect } from 'vitest'
import {
  loginSchema,
  registerSchema,
  addressSchema,
  reviewSchema,
  profileSchema,
  productSchema,
  categorySchema,
  bannerSchema
} from './index'

describe('loginSchema', () => {
  it('accepts valid identifier + password', () => {
    expect(loginSchema.safeParse({ identifier: 'minh', password: '123456' }).success).toBe(true)
  })
  it('rejects empty fields', () => {
    expect(loginSchema.safeParse({ identifier: '', password: '' }).success).toBe(false)
  })
})

describe('registerSchema', () => {
  it('accepts valid Vietnamese phone', () => {
    expect(registerSchema.safeParse({ username: 'minh', email: 'minh@example.com', numberPhone: '0901234567', password: '123456' }).success).toBe(true)
    expect(registerSchema.safeParse({ username: 'minh', email: 'minh@example.com', numberPhone: '+84123456789', password: '123456' }).success).toBe(true)
  })
  it('rejects malformed phone', () => {
    expect(registerSchema.safeParse({ username: 'minh', email: 'minh@example.com', numberPhone: '12345', password: '123456' }).success).toBe(false)
    expect(registerSchema.safeParse({ username: 'minh', email: 'minh@example.com', numberPhone: 'abc1234567', password: '123456' }).success).toBe(false)
  })
  it('rejects short username/password and bad email', () => {
    expect(registerSchema.safeParse({ username: 'ab', email: 'minh@example.com', numberPhone: '0901234567', password: '123456' }).success).toBe(false)
    expect(registerSchema.safeParse({ username: 'minh', email: 'nope', numberPhone: '0901234567', password: '123456' }).success).toBe(false)
    expect(registerSchema.safeParse({ username: 'minh', email: 'minh@example.com', numberPhone: '0901234567', password: '123' }).success).toBe(false)
  })
})

describe('addressSchema', () => {
  const valid = {
    label: 'Nhà', street: '12 Nguyễn Văn Cừ', ward: 'Phường 1', district: 'Quận 5', city: 'TP.HCM',
    receiverName: 'Minh', receiverPhone: '0901234567'
  }
  it('accepts valid address', () => {
    expect(addressSchema.safeParse(valid).success).toBe(true)
  })
  it('rejects missing required fields', () => {
    expect(addressSchema.safeParse({ ...valid, street: '' }).success).toBe(false)
    expect(addressSchema.safeParse({ ...valid, receiverPhone: '1' }).success).toBe(false)
  })
})

describe('reviewSchema', () => {
  it('accepts rating 1..5', () => {
    expect(reviewSchema.safeParse({ rating: 5, content: 'tốt' }).success).toBe(true)
    expect(reviewSchema.safeParse({ rating: 1, content: '' }).success).toBe(true)
  })
  it('rejects invalid rating', () => {
    expect(reviewSchema.safeParse({ rating: 0, content: 'x' }).success).toBe(false)
    expect(reviewSchema.safeParse({ rating: 6, content: 'x' }).success).toBe(false)
  })
})

describe('profileSchema', () => {
  const base = { username: 'minh', numberPhone: '0901234567', avatarUrl: '' }
  it('accepts profile without password change', () => {
    expect(profileSchema.safeParse({ ...base, currentPassword: '', newPassword: '' }).success).toBe(true)
  })
  it('requires both current and new password together', () => {
    expect(profileSchema.safeParse({ ...base, currentPassword: 'old', newPassword: '' }).success).toBe(false)
    expect(profileSchema.safeParse({ ...base, currentPassword: '', newPassword: 'newpass' }).success).toBe(false)
    expect(profileSchema.safeParse({ ...base, currentPassword: 'old', newPassword: 'newpass' }).success).toBe(true)
  })
  it('rejects malformed phone', () => {
    expect(profileSchema.safeParse({ ...base, numberPhone: 'abc' }).success).toBe(false)
  })
})

describe('productSchema', () => {
  const valid = { name: 'Bơ', slug: 'bo', price: '25000', stock: '10', weight: '0.5', categoryId: '1', active: true, imageUrl: '' }
  it('accepts valid product with coerced numbers', () => {
    expect(productSchema.safeParse(valid).success).toBe(true)
  })
  it('rejects non-positive price and unknown category', () => {
    expect(productSchema.safeParse({ ...valid, price: '0' }).success).toBe(false)
    expect(productSchema.safeParse({ ...valid, categoryId: '0' }).success).toBe(false)
  })
  it('rejects bad image URL', () => {
    expect(productSchema.safeParse({ ...valid, imageUrl: 'not-a-url' }).success).toBe(false)
    expect(productSchema.safeParse({ ...valid, imageUrl: 'https://img.example/a.jpg' }).success).toBe(true)
  })
})

describe('categorySchema', () => {
  it('accepts valid category', () => {
    expect(categorySchema.safeParse({ name: 'Rau củ', slug: 'rau-cu', parentId: null }).success).toBe(true)
  })
  it('rejects missing name/slug', () => {
    expect(categorySchema.safeParse({ name: '', slug: 'rau-cu' }).success).toBe(false)
    expect(categorySchema.safeParse({ name: 'Rau củ', slug: '' }).success).toBe(false)
  })
})

describe('bannerSchema', () => {
  it('accepts valid banner', () => {
    expect(bannerSchema.safeParse({ title: 'Khuyến mãi', imageUrl: '/images/banner.jpg' }).success).toBe(true)
  })
  it('rejects missing title/image', () => {
    expect(bannerSchema.safeParse({ title: '', imageUrl: '/images/banner.jpg' }).success).toBe(false)
    expect(bannerSchema.safeParse({ title: 'Khuyến mãi', imageUrl: '' }).success).toBe(false)
  })
})