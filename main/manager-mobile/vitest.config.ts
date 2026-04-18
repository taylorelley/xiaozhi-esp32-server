import { defineConfig } from 'vitest/config'
import path from 'node:path'

export default defineConfig({
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  // The UniApp build injects __UNI_PLATFORM__ at compile time. For unit tests
  // we just replace it with a string literal so modules that read it (notably
  // src/utils/platform.ts) don't blow up at import time.
  define: {
    __UNI_PLATFORM__: JSON.stringify('h5'),
  },
  test: {
    globals: true,
    environment: 'jsdom',
    include: ['tests/unit/**/*.{test,spec}.{js,mjs,ts}'],
    setupFiles: ['./tests/setup.ts'],
    coverage: {
      provider: 'v8',
      include: ['src/utils/**/*.ts'],
      exclude: ['src/utils/uploadFile.ts', 'src/utils/toast.ts'],
      reporter: ['text', 'lcov'],
    },
  },
})
