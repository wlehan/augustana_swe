const AUTH_STORAGE_KEY = 'demo_user'
const TAB_AUTH_STORAGE_KEY = 'demo_user_tab'
const ACTIVE_GAME_STORAGE_KEY = 'active_game'

/**
 * Session helpers keep a tab-scoped auth copy so multiple browser tabs can sign
 * in as different players during local testing.
 */
function hasLocalStorage() {
  return typeof window !== 'undefined' && typeof window.localStorage !== 'undefined'
}

function hasSessionStorage() {
  return typeof window !== 'undefined' && typeof window.sessionStorage !== 'undefined'
}

function parseSession(raw) {
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

export function readStoredSession() {
  if (hasSessionStorage()) {
    const tabSession = parseSession(window.sessionStorage.getItem(TAB_AUTH_STORAGE_KEY))
    if (tabSession) {
      return tabSession
    }
  }

  if (!hasLocalStorage()) {
    return null
  }

  const raw = window.localStorage.getItem(AUTH_STORAGE_KEY)
  const storedSession = parseSession(raw)
  if (storedSession && hasSessionStorage()) {
    window.sessionStorage.setItem(TAB_AUTH_STORAGE_KEY, raw)
  }
  return storedSession
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

  const serializedSession = JSON.stringify(session)
  window.localStorage.setItem(AUTH_STORAGE_KEY, serializedSession)

  if (hasSessionStorage()) {
    window.sessionStorage.setItem(TAB_AUTH_STORAGE_KEY, serializedSession)
  }
}

export function clearStoredSession() {
  if (hasSessionStorage()) {
    window.sessionStorage.removeItem(TAB_AUTH_STORAGE_KEY)
  }

  if (hasLocalStorage()) {
    window.localStorage.removeItem(AUTH_STORAGE_KEY)
    window.localStorage.removeItem(ACTIVE_GAME_STORAGE_KEY)
  }
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
