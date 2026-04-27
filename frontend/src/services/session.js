const AUTH_STORAGE_KEY = 'demo_user'
const ACTIVE_GAME_STORAGE_KEY = 'active_game'

function hasLocalStorage() {
  return typeof window !== 'undefined' && typeof window.localStorage !== 'undefined'
}

export function readStoredSession() {
  if (!hasLocalStorage()) {
    return null
  }

  const raw = window.localStorage.getItem(AUTH_STORAGE_KEY)
  if (!raw) {
    return null
  }

  try {
    const parsed = JSON.parse(raw)
    return parsed && typeof parsed === 'object' ? parsed : null
  } catch {
    return null
  }
}

export function saveStoredSession(authResponse) {
  if (!hasLocalStorage() || !authResponse) {
    return
  }

  const session = {
    userId: authResponse.userId ?? null,
    username: authResponse.username ?? '',
    token: authResponse.token ?? null,
  }

  window.localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session))
}

export function clearStoredSession() {
  if (!hasLocalStorage()) {
    return
  }

  window.localStorage.removeItem(AUTH_STORAGE_KEY)
  window.localStorage.removeItem(ACTIVE_GAME_STORAGE_KEY)
}

export function getAuthToken() {
  return readStoredSession()?.token ?? null
}

export function hasAuthenticatedSession(session = readStoredSession()) {
  return Boolean(session?.userId && session?.token)
}

export function isUnauthorizedError(error) {
  return error?.response?.status === 401
}
