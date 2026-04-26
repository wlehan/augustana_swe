import { beforeEach, describe, expect, test } from 'vitest'
import {
  clearStoredUserProfileImage,
  readStoredUserProfile,
  recordCompletedGameStats,
  saveStoredUserProfileImage,
} from '../../services/profile'

describe('profile service', () => {
  const session = { userId: 10, username: 'player1' }

  beforeEach(() => {
    localStorage.clear()
  })

  test('saves and clears a user profile image', () => {
    const saved = saveStoredUserProfileImage(session, 'data:image/png;base64,test')

    expect(saved.profileImage).toBe('data:image/png;base64,test')
    expect(readStoredUserProfile(session).profileImage).toBe('data:image/png;base64,test')

    const cleared = clearStoredUserProfileImage(session)
    expect(cleared.profileImage).toBeNull()
  })

  test('records a completed game win once', () => {
    const players = [
      { userId: 10, name: 'player1', total: 12 },
      { userId: 11, name: 'player2', total: 18 },
    ]

    const firstRecord = recordCompletedGameStats({ session, gameId: 5, players })
    const secondRecord = recordCompletedGameStats({ session, gameId: 5, players })

    expect(firstRecord.stats).toEqual({ gamesPlayed: 1, wins: 1 })
    expect(secondRecord.stats).toEqual({ gamesPlayed: 1, wins: 1 })
    expect(secondRecord.completedGameIds).toEqual(['5'])
  })
})
