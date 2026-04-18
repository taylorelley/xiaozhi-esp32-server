import { describe, it, expect } from 'vitest'
import { formatDate, formatFileSize } from '@/utils/format'

describe('formatDate', () => {
  it('returns empty string for null/undefined input', () => {
    expect(formatDate(null)).toBe('')
    expect(formatDate(undefined)).toBe('')
    expect(formatDate('')).toBe('')
  })

  it('formats a Date object as YYYY-MM-DD HH:mm:ss in local time', () => {
    // Construct with local-time components so the expectation doesn't depend
    // on the CI runner's timezone.
    const d = new Date(2024, 0, 15, 9, 5, 7) // 2024-01-15 09:05:07 local
    expect(formatDate(d)).toBe('2024-01-15 09:05:07')
  })

  it('zero-pads single-digit components', () => {
    const d = new Date(2024, 8, 3, 1, 2, 4) // 2024-09-03 01:02:04 local
    expect(formatDate(d)).toBe('2024-09-03 01:02:04')
  })

  it('accepts an ISO string input and reformats it', () => {
    const iso = '2024-06-01T00:00:00'
    const out = formatDate(iso)
    // Don't assert the exact value because ISO parsing is timezone-dependent;
    // just assert the shape.
    expect(out).toMatch(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/)
  })
})

describe('formatFileSize', () => {
  it('returns "0 B" for zero/null/undefined', () => {
    expect(formatFileSize(0)).toBe('0 B')
    expect(formatFileSize(null)).toBe('0 B')
    expect(formatFileSize(undefined)).toBe('0 B')
  })

  it('formats small byte counts as B', () => {
    expect(formatFileSize(1)).toBe('1 B')
    expect(formatFileSize(512)).toBe('512 B')
  })

  it('formats kilobytes', () => {
    expect(formatFileSize(1024)).toBe('1 KB')
    expect(formatFileSize(1536)).toBe('1.5 KB')
  })

  it('formats megabytes', () => {
    expect(formatFileSize(1024 * 1024)).toBe('1 MB')
    expect(formatFileSize(2.5 * 1024 * 1024)).toBe('2.5 MB')
  })

  it('formats gigabytes and terabytes', () => {
    expect(formatFileSize(1024 ** 3)).toBe('1 GB')
    expect(formatFileSize(1024 ** 4)).toBe('1 TB')
  })

  it('rounds to two decimal places', () => {
    // 1234 B = 1.205078125 KB -> rounded to 1.21
    expect(formatFileSize(1234)).toBe('1.21 KB')
  })
})
