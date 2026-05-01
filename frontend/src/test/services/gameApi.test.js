import { describe, test, expect, beforeEach, vi } from 'vitest'

const { mockApiClient } = vi.hoisted(() => ({
  mockApiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    interceptors: {
      request: {
        use: vi.fn()
      },
      response: {
        use: vi.fn()
      }
    }
  }
}))

vi.mock('../../services/apiClient', () => ({
  default: mockApiClient
}))

import * as gameApi from '../../services/gameApi'

describe('gameApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  test('createGame posts to the games endpoint', async () => {
    mockApiClient.post.mockResolvedValue({ data: { gameId: 1 } })

    const result = await gameApi.createGame({ userId: 10, maxPlayers: 3 })

    expect(mockApiClient.post).toHaveBeenCalledWith(
      '/api/games',
      { maxPlayers: 3 }
    )

    expect(result).toEqual({ gameId: 1 })
  })

  test('joinGame posts a join request with the supplied code', async () => {
    mockApiClient.post.mockResolvedValue({ data: { gameId: 2 } })

    const result = await gameApi.joinGame({ userId: 10, gameCode: 'ABC123' })

    expect(mockApiClient.post).toHaveBeenCalledWith(
      '/api/games/join',
      { gameCode: 'ABC123' }
    )

    expect(result).toEqual({ gameId: 2 })
  })

  test('getGameState gets game state by game id', async () => {
    mockApiClient.get.mockResolvedValue({ data: { gameStatus: 'IN_PROGRESS' } })

    const result = await gameApi.getGameState({ gameId: 5, userId: 10 })

    expect(mockApiClient.get).toHaveBeenCalledWith('/api/games/5/state')

    expect(result).toEqual({ gameStatus: 'IN_PROGRESS' })
  })

  test('discardCard sends flip position when provided', async () => {
    mockApiClient.post.mockResolvedValue({ data: { success: true } })

    const result = await gameApi.discardCard({
      gameId: 5,
      userId: 10,
      flipPosition: 3
    })

    expect(mockApiClient.post).toHaveBeenCalledWith(
      '/api/games/5/actions/discard',
      { flipPosition: 3 }
    )

    expect(result).toEqual({ success: true })
  })
})