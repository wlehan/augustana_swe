const PROFILE_STORAGE_KEY = 'user_profiles'

const DEFAULT_STATS = {
  gamesPlayed: 0,
  wins: 0,
}

function hasLocalStorage() {
  return typeof window !== 'undefined' && typeof window.localStorage !== 'undefined'
}

function normalizeId(value) {
  return value === null || value === undefined ? '' : String(value)
}

function normalizeUsername(value) {
  return (value || '').trim().toLowerCase()
}

function profileKeyForSession(session) {
  if (session?.userId !== null && session?.userId !== undefined) {
    return `id:${session.userId}`
  }

  const username = normalizeUsername(session?.username)
  return username ? `username:${username}` : null
}

function sanitizeStats(stats) {
  return {
    gamesPlayed: Number(stats?.gamesPlayed || 0),
    wins: Number(stats?.wins || 0),
  }
}

function readProfileMap() {
  if (!hasLocalStorage()) {
    return {}
  }

  const raw = window.localStorage.getItem(PROFILE_STORAGE_KEY)
  if (!raw) {
    return {}
  }

  try {
    const parsed = JSON.parse(raw)
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch {
    return {}
  }
}

function saveProfileMap(profileMap) {
  if (!hasLocalStorage()) {
    return
  }

  window.localStorage.setItem(PROFILE_STORAGE_KEY, JSON.stringify(profileMap))
}

function mergeProfile(session, updates = {}) {
  const key = profileKeyForSession(session)
  if (!key) {
    return {
      userId: session?.userId ?? null,
      username: session?.username ?? '',
      profileImage: null,
      stats: DEFAULT_STATS,
      completedGameIds: [],
    }
  }

  const profileMap = readProfileMap()
  const current = profileMap[key] || {}
  const next = {
    ...current,
    ...updates,
    userId: session?.userId ?? current.userId ?? null,
    username: session?.username ?? current.username ?? '',
    stats: sanitizeStats(updates.stats || current.stats),
    completedGameIds: Array.isArray(updates.completedGameIds)
      ? updates.completedGameIds
      : Array.isArray(current.completedGameIds)
      ? current.completedGameIds
      : [],
  }

  profileMap[key] = next
  saveProfileMap(profileMap)
  return next
}

export function readStoredUserProfiles() {
  return Object.values(readProfileMap()).map((profile) => ({
    ...profile,
    stats: sanitizeStats(profile?.stats),
    completedGameIds: Array.isArray(profile?.completedGameIds) ? profile.completedGameIds : [],
  }))
}

export function readStoredUserProfile(session) {
  const key = profileKeyForSession(session)
  if (!key) {
    return mergeProfile(session)
  }

  const profile = readProfileMap()[key]
  return {
    userId: session?.userId ?? profile?.userId ?? null,
    username: session?.username ?? profile?.username ?? '',
    profileImage: profile?.profileImage || null,
    stats: sanitizeStats(profile?.stats),
    completedGameIds: Array.isArray(profile?.completedGameIds) ? profile.completedGameIds : [],
  }
}

export function saveStoredUserProfileImage(session, profileImage) {
  return mergeProfile(session, { profileImage })
}

export function clearStoredUserProfileImage(session) {
  return mergeProfile(session, { profileImage: null })
}

export function recordCompletedGameStats({ session, gameId, players }) {
  if (!session || !gameId || !Array.isArray(players) || players.length === 0) {
    return readStoredUserProfile(session)
  }

  const profile = readStoredUserProfile(session)
  const completedGameId = normalizeId(gameId)
  if (profile.completedGameIds.includes(completedGameId)) {
    return profile
  }

  const userId = normalizeId(session.userId)
  const username = normalizeUsername(session.username)
  const currentPlayer = players.find((player) => {
    if (normalizeId(player?.userId) && userId && normalizeId(player.userId) === userId) {
      return true
    }

    return username && normalizeUsername(player?.name || player?.username) === username
  })

  if (!currentPlayer) {
    return profile
  }

  const numericTotals = players
    .map((player) => Number(player?.total))
    .filter((score) => Number.isFinite(score))

  const currentTotal = Number(currentPlayer.total)
  const bestScore = numericTotals.length > 0 ? Math.min(...numericTotals) : null
  const didWin = Number.isFinite(currentTotal) && bestScore !== null && currentTotal === bestScore

  return mergeProfile(session, {
    stats: {
      gamesPlayed: profile.stats.gamesPlayed + 1,
      wins: profile.stats.wins + (didWin ? 1 : 0),
    },
    completedGameIds: [...profile.completedGameIds, completedGameId],
  })
}
