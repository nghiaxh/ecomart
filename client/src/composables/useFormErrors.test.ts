import { describe, it, expect, beforeEach } from 'vitest'
import { z } from 'zod'
import { registerSchema, profileSchema } from '@/schemas'
import { useFormErrors } from './useFormErrors'

describe('useFormErrors', () => {
  beforeEach(() => {
    useFormErrors().clearErrors()
  })

  it('maps each invalid field to its first message', () => {
    const result = registerSchema.safeParse({
      username: 'ab', email: 'nope', numberPhone: '123', password: '123'
    })
    const { errors, applyIssues } = useFormErrors()
    applyIssues(result.error!)
    expect(errors.value).toEqual({
      username: 'Tên đăng nhập tối thiểu 3 ký tự',
      email: 'Email không hợp lệ',
      numberPhone: 'Số điện thoại không hợp lệ',
      password: 'Mật khẩu tối thiểu 6 ký tự'
    })
  })

  it('re-applying issues replaces previous errors', () => {
    const bad1 = registerSchema.safeParse({ username: 'ab', email: 'nope', numberPhone: '123', password: '123' })
    const bad2 = registerSchema.safeParse({ username: 'minh', email: 'nope2', numberPhone: '000', password: '1234567' })
    const { errors, applyIssues } = useFormErrors()
    applyIssues(bad1.error!)
    expect(errors.value).toHaveProperty('username')
    applyIssues(bad2.error!)
    expect(errors.value).not.toHaveProperty('username')
    expect(errors.value.email).toBe('Email không hợp lệ')
    expect(errors.value.numberPhone).toBe('Số điện thoại không hợp lệ')
  })

  it('keeps the payload refine message on its path', () => {
    const result = profileSchema.safeParse({
      username: 'minh', numberPhone: '0901234567', currentPassword: 'old', newPassword: ''
    })
    const { errors, applyIssues } = useFormErrors()
    applyIssues(result.error!)
    expect(errors.value.newPassword).toBe('Vui lòng nhập đầy đủ mật khẩu hiện tại và mật khẩu mới')
  })

  it('defaults an empty issue path to the form key', () => {
    const zErr = z.string().safeParse(123).error!
    const { errors, applyIssues } = useFormErrors()
    applyIssues(zErr)
    expect(errors.value.form).toBeTruthy()
  })

  it('clearErrors empties the map', () => {
    const result = registerSchema.safeParse({ username: 'ab' })
    const { errors, applyIssues, clearErrors } = useFormErrors()
    applyIssues(result.error!)
    clearErrors()
    expect(errors.value).toEqual({})
  })
})