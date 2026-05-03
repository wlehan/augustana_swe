import '@testing-library/jest-dom'
import { vi } from 'vitest'

/**
 * Creates the browser storage API surface used by session/profile tests.
 */
const createMockStorage = () => {
  let storage = {}
  return {
    getItem: vi.fn((key) => storage[key] || null),
    setItem: vi.fn((key, value) => {
      storage[key] = value.toString()
    }),
    removeItem: vi.fn((key) => {
      delete storage[key]
    }),
    clear: vi.fn(() => {
      storage = {}
    }),
    get length() {
      return Object.keys(storage).length
    },
    key: vi.fn((index) => {
      const keys = Object.keys(storage)
      return keys[index] || null
    }),
  }
}

Object.defineProperty(window, 'localStorage', {
  value: createMockStorage(),
  writable: true,
})

Object.defineProperty(window, 'sessionStorage', {
  value: createMockStorage(),
  writable: true,
})

