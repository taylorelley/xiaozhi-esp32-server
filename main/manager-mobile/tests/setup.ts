// Vitest setup for manager-mobile. The codebase is UniApp-first, so we have
// to stub out the runtime globals that UniApp would normally provide before
// any test module loads.

import { vi } from 'vitest'

// UniApp global used by uni.* calls inside src/utils and src/http.
if (!(globalThis as any).uni) {
  ;(globalThis as any).uni = {
    getStorageSync: vi.fn(() => ''),
    setStorageSync: vi.fn(),
    removeStorageSync: vi.fn(),
    getAccountInfoSync: vi.fn(() => ({ miniProgram: { envVersion: 'release' } })),
    showToast: vi.fn(),
    showLoading: vi.fn(),
    hideLoading: vi.fn(),
  }
}

if (!(globalThis as any).getCurrentPages) {
  ;(globalThis as any).getCurrentPages = vi.fn(() => [])
}

if (typeof globalThis.fetch !== 'function') {
  globalThis.fetch = vi.fn(() =>
    Promise.reject(new Error('fetch not mocked in this test'))
  )
}
