import axios from 'axios'
import * as gameApi from '../../services/gameApi'
import { API_BASE_URL } from '../../services/authApi'
import { vi } from 'vitest'
vi.mock('axios')

describe('gameApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  test('createGame posts to the games endpoint with headers', async () => {
    axios.post.mockResolvedValue({ data: { gameId: 1 } })

    const result = await gameApi.createGame({ userId: 10, maxPlayers: 3 })

    expect(axios.post).toHaveBeenCalledWith(
      `${API_BASE_URL}/api/games`,
      { maxPlayers: 3 },
      { headers: { 'X-User-Id': '10' } }
    )
    expect(result).toEqual({ gameId: 1 })
  })

  test('joinGame posts a join request with the supplied code', async () => {
    axios.post.mockResolvedValue({ data: { gameId: 2 } })

    const result = await gameApi.joinGame({ userId: 10, gameCode: 'ABC123' })

    expect(axios.post).toHaveBeenCalledWith(
      `${API_BASE_URL}/api/games/join`,
      { gameCode: 'ABC123' },
      { headers: { 'X-User-Id': '10' } }
    )
    expect(result).toEqual({ gameId: 2 })
  })

  test('getGameState sends headers for requesting user', async () => {
    axios.get.mockResolvedValue({ data: { gameStatus: 'IN_PROGRESS' } })

    const result = await gameApi.getGameState({ gameId: 5, userId: 10 })

    expect(axios.get).toHaveBeenCalledWith(
      `${API_BASE_URL}/api/games/5/state`,
      { headers: { 'X-User-Id': '10' } }
    )
    expect(result).toEqual({ gameStatus: 'IN_PROGRESS' })
  })

  test('discardCard sends flip position when provided', async () => {
    axios.post.mockResolvedValue({ data: { success: true } })

    const result = await gameApi.discardCard({ gameId: 5, userId: 10, flipPosition: 3 })

    expect(axios.post).toHaveBeenCalledWith(
      `${API_BASE_URL}/api/games/5/actions/discard`,
      { flipPosition: 3 },
      { headers: { 'X-User-Id': '10' } }
    )
    expect(result).toEqual({ success: true })
  })
})
