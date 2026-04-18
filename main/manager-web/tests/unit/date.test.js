import { describe, it, expect } from 'vitest'
import { isDate, isDateObject, toDate, formatDate } from '@/utils/date'

describe('isDate', () => {
  it('returns false for null/undefined', () => {
    expect(isDate(null)).toBe(false)
    expect(isDate(undefined)).toBe(false)
  })

  it('returns true for a parseable date string', () => {
    expect(isDate('2024-01-15')).toBe(true)
  })

  it('returns false for an unparseable string', () => {
    expect(isDate('not-a-date')).toBe(false)
  })

  it('returns true for a real Date object', () => {
    expect(isDate(new Date())).toBe(true)
  })

  it('returns true for epoch-millisecond numbers', () => {
    expect(isDate(1705318200000)).toBe(true)
  })
})

describe('isDateObject', () => {
  it('distinguishes Date instances from strings/numbers', () => {
    expect(isDateObject(new Date())).toBe(true)
    expect(isDateObject('2024-01-15')).toBe(false)
    expect(isDateObject(1705318200000)).toBe(false)
    expect(isDateObject(null)).toBe(false)
  })
})

describe('toDate', () => {
  it('returns null for unparseable input', () => {
    expect(toDate('bogus')).toBeNull()
    expect(toDate(null)).toBeNull()
    expect(toDate(undefined)).toBeNull()
  })

  it('returns a Date for valid input', () => {
    const d = toDate('2024-01-15')
    expect(d).toBeInstanceOf(Date)
    expect(Number.isNaN(d.getTime())).toBe(false)
  })
})

describe('formatDate (from utils/date)', () => {
  it('returns empty string for falsy input', () => {
    expect(formatDate(null)).toBe('')
    expect(formatDate(undefined)).toBe('')
  })

  it('honours the yyyy-MM-dd format', () => {
    const d = new Date(2024, 0, 15, 0, 0, 0)
    expect(formatDate(d, 'yyyy-MM-dd')).toBe('2024-01-15')
  })

  it('pads two-digit year specifiers', () => {
    const d = new Date(2024, 0, 15)
    // 'yy-MM-dd' should take the last two digits of the year.
    expect(formatDate(d, 'yy-MM-dd')).toBe('24-01-15')
  })

  it('supports lowercase time fields (h/m/s)', () => {
    const d = new Date(2024, 0, 15, 9, 5, 7)
    // Note: the underlying formatter only matches lowercase hours (h+).
    expect(formatDate(d, 'yyyy-MM-dd hh:mm:ss')).toBe('2024-01-15 09:05:07')
  })

  it('falls back to the default format when none is given', () => {
    const d = new Date(2024, 0, 15, 9, 5, 7)
    const out = formatDate(d)
    expect(out).toMatch(/^\d{4}-\d{2}-\d{2} /)
  })
})
