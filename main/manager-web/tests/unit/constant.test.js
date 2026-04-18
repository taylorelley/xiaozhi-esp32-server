import { describe, it, expect } from 'vitest'
import Constant from '@/utils/constant'

describe('Constant - page paths', () => {
  it('exposes the login route', () => {
    expect(Constant.PAGE.LOGIN).toBe('/login')
  })
})

describe('Constant - storage keys', () => {
  it('declares the three canonical storage keys', () => {
    expect(Constant.STORAGE_KEY).toMatchObject({
      TOKEN: 'TOKEN',
      PUBLIC_KEY: 'PUBLIC_KEY',
      USER_TYPE: 'USER_TYPE',
    })
  })
})

describe('Constant - language codes', () => {
  it.each([
    ['zh_cn'],
    ['zh_tw'],
    ['en'],
  ])('registers %s', (code) => {
    expect(Constant.Lang[code]).toBe(code)
  })
})

describe('Constant.get()', () => {
  it('returns the value when the key exists', () => {
    expect(Constant.get({ foo: 'bar' }, 'foo')).toBe('bar')
  })

  it('returns HAVE_NO_RESULT sentinel for missing keys', () => {
    expect(Constant.get({}, 'missing')).toBe('None')
    expect(Constant.get({ foo: 'bar' }, 'nope')).toBe('None')
  })

  it('returns HAVE_NO_RESULT for falsy values', () => {
    // Documented quirk: falsy values (0, '') are treated as missing because
    // of the || fallback in Constant.get.
    expect(Constant.get({ foo: 0 }, 'foo')).toBe('None')
    expect(Constant.get({ foo: '' }, 'foo')).toBe('None')
  })
})

describe('Constant - font sizes', () => {
  it('exposes big and normal', () => {
    expect(Constant.FONT_SIZE.big).toBe('big')
    expect(Constant.FONT_SIZE.normal).toBe('normal')
  })
})
