import { describe, it, expect } from 'vitest'
import { useFormat } from './useFormat'

const { formatVND, formatDate, formatKg } = useFormat()

describe('useFormat', () => {
  describe('formatVND', () => {
    it('formats with vi-VN currency style and no decimals', () => {
      expect(formatVND(25000)).toBe(`25.000\u00A0₫`)
      expect(formatVND(0)).toBe(`0\u00A0₫`)
    })
  })

  describe('formatDate', () => {
    it('returns empty string for missing value', () => {
      expect(formatDate(undefined)).toBe('')
      expect(formatDate('')).toBe('')
    })
    it('formats a valid ISO date to vi-VN locale', () => {
      const out = formatDate('2026-09-03T10:30:00')
      expect(out).toMatch(/\d{2}\/\d{2}\/\d{4}/)
      expect(out).toContain(':')
    })
  })

  describe('formatKg', () => {
    it('returns empty for missing value', () => {
      expect(formatKg()).toBe('')
    })
    it('appends kg suffix', () => {
      expect(formatKg(0.5)).toBe('0.5 kg')
    })
  })
})