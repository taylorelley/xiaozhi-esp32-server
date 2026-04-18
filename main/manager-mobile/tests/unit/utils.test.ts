// Unit tests for the pure helpers exported from src/utils/index.ts.
//
// The module has a top-level import of '@/pages.json' and './platform', both
// of which assume the UniApp build environment. We intercept those imports
// with vi.mock so the rest of the module (deepClone, debounce, getUrlObj,
// sm2Encrypt/Decrypt) can be imported and exercised in a plain node + jsdom
// runtime.

import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/pages.json', () => ({
  pages: [],
  subPackages: [],
  default: { pages: [], subPackages: [] },
}))

vi.mock('@/utils/platform', () => ({
  isMpWeixin: false,
  isH5: true,
  isApp: false,
  isMp: false,
  platform: 'h5',
  default: { platform: 'h5', isH5: true, isApp: false, isMp: false, isMpWeixin: false },
}))

const utils = await import('@/utils/index')

describe('deepClone', () => {
  it('returns primitives unchanged', () => {
    expect(utils.deepClone(1)).toBe(1)
    expect(utils.deepClone('x')).toBe('x')
    expect(utils.deepClone(true)).toBe(true)
    expect(utils.deepClone(null)).toBeNull()
    expect(utils.deepClone(undefined)).toBeUndefined()
  })

  it('clones plain objects deeply', () => {
    const src: any = { a: 1, nested: { b: 2, list: [1, 2, 3] } }
    const copy: any = utils.deepClone(src)
    expect(copy).toEqual(src)
    expect(copy).not.toBe(src)
    expect(copy.nested).not.toBe(src.nested)
    expect(copy.nested.list).not.toBe(src.nested.list)
  })

  it('clones arrays with nested objects', () => {
    const src: any = [{ id: 1 }, { id: 2, nested: { x: 9 } }]
    const copy: any = utils.deepClone(src)
    expect(copy).toEqual(src)
    expect(copy[1]).not.toBe(src[1])
    expect(copy[1].nested).not.toBe(src[1].nested)
  })

  it('clones Date instances to a new Date with the same time', () => {
    const d = new Date('2024-01-15T12:34:56Z')
    const copy = utils.deepClone(d) as Date
    expect(copy).toBeInstanceOf(Date)
    expect(copy).not.toBe(d)
    expect(copy.getTime()).toBe(d.getTime())
  })

  it('mutating the clone does not affect the source', () => {
    const src: any = { list: [1, 2, 3] }
    const copy: any = utils.deepClone(src)
    copy.list.push(4)
    expect(src.list).toEqual([1, 2, 3])
  })
})

describe('debounce', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  it('delays invocation until the timer elapses', () => {
    const fn = vi.fn()
    const debounced = utils.debounce(fn, 50)
    debounced()
    debounced()
    debounced()
    expect(fn).not.toHaveBeenCalled()
    vi.advanceTimersByTime(50)
    expect(fn).toHaveBeenCalledTimes(1)
  })

  it('forwards the latest arguments', () => {
    const fn = vi.fn()
    const debounced = utils.debounce(fn, 25)
    debounced(1)
    debounced(2)
    debounced(3)
    vi.advanceTimersByTime(25)
    expect(fn).toHaveBeenCalledWith(3)
  })

  it('fires immediately when immediate=true, then ignores subsequent calls in the window', () => {
    const fn = vi.fn()
    const debounced = utils.debounce(fn, 50, true)
    debounced('a')
    debounced('b')
    expect(fn).toHaveBeenCalledTimes(1)
    expect(fn).toHaveBeenLastCalledWith('a')
    vi.advanceTimersByTime(50)
    // No trailing fire for immediate=true.
    expect(fn).toHaveBeenCalledTimes(1)
  })

  it('cancel() prevents the pending call from firing', () => {
    const fn = vi.fn()
    const debounced = utils.debounce(fn, 50)
    debounced()
    debounced.cancel()
    vi.advanceTimersByTime(100)
    expect(fn).not.toHaveBeenCalled()
  })
})

describe('getUrlObj', () => {
  it('returns path + empty query for a url with no ?', () => {
    expect(utils.getUrlObj('/pages/login/index')).toEqual({
      path: '/pages/login/index',
      query: {},
    })
  })

  it('parses simple key=value query parameters', () => {
    const out = utils.getUrlObj('/pages/login/index?name=xiaozhi&age=3')
    expect(out.path).toBe('/pages/login/index')
    expect(out.query).toEqual({ name: 'xiaozhi', age: '3' })
  })

  it('recursively decodes percent-encoded values', () => {
    const doubleEncoded = encodeURIComponent(encodeURIComponent('/pages/demo/base/route-interceptor'))
    const url = `/pages/login/index?redirect=${doubleEncoded}`
    const out = utils.getUrlObj(url)
    expect(out.query.redirect).toBe('/pages/demo/base/route-interceptor')
  })

  it('handles an already-decoded redirect parameter', () => {
    const url = '/pages/login/index?redirect=%2Fpages%2Fdemo%2Fbase%2Froute-interceptor'
    const out = utils.getUrlObj(url)
    expect(out.query.redirect).toBe('/pages/demo/base/route-interceptor')
  })
})

describe('sm2Encrypt / sm2Decrypt', () => {
  it('encrypts with the 04 prefix expected by the backend and round-trips', async () => {
    const { generateSm2KeyPairHex, sm2Encrypt, sm2Decrypt } = utils
    const { publicKey, privateKey } = generateSm2KeyPairHex()

    const plain = 'xiaozhi-secret'
    const cipher = sm2Encrypt(publicKey, plain)
    expect(cipher.startsWith('04')).toBe(true)
    expect(cipher.length).toBeGreaterThan(10)

    expect(sm2Decrypt(privateKey, cipher)).toBe(plain)
  })

  it('sm2Encrypt rejects empty plaintext', () => {
    expect(() => utils.sm2Encrypt('deadbeef', '')).toThrow(/empty/i)
  })

  it('sm2Encrypt rejects null/undefined public key', () => {
    // @ts-expect-error deliberately passing invalid input
    expect(() => utils.sm2Encrypt(null, 'x')).toThrow(/null|undefined/i)
    // @ts-expect-error deliberately passing invalid input
    expect(() => utils.sm2Encrypt(undefined, 'x')).toThrow(/null|undefined/i)
  })
})
