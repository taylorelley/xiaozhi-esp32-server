// Vitest setup shared across the manager-web unit tests. Adds minimal jsdom
// polyfills so individual tests can override with vi.fn() where needed.
import { vi } from 'vitest'

if (typeof globalThis.fetch !== 'function') {
  globalThis.fetch = vi.fn(() =>
    Promise.reject(new Error('fetch not mocked in this test'))
  )
}
