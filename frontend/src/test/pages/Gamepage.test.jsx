import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, test, vi } from 'vitest'
import GamePage from '../../pages/Gamepage'
import { getGame, getGameState } from '../../services/gameApi'

vi.mock('../../services/gameApi', () => ({
  discardCard: vi.fn(),
  drawCard: vi.fn(),
  flipInitialCard: vi.fn(),
  getGame: vi.fn(),
  getGameState: vi.fn(),
  startGame: vi.fn(),
  swapCard: vi.fn(),
}))

describe('GamePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    sessionStorage.clear()
    localStorage.setItem('demo_user', JSON.stringify({ userId: 10, username: 'player1', token: 'token' }))
  })

  test('shows the current player their drawn held card', async () => {
    const profileImage = 'data:image/png;base64,profile-image'
    localStorage.setItem('user_profiles', JSON.stringify({
      'id:10': {
        userId: 10,
        username: 'player1',
        profileImage,
        stats: { gamesPlayed: 0, wins: 0 },
        completedGameIds: [],
      },
    }))

    getGame.mockResolvedValue({ status: 'IN_PROGRESS' })
    getGameState.mockResolvedValue({
      gameStatus: 'IN_PROGRESS',
      status: 'IN_PROGRESS',
      gameCode: 'ABC123',
      currentRound: 1,
      round: {
        status: 'ACTIVE',
        currentTurnUserId: 10,
        currentTurnGamePlayerId: 101,
        currentDrawSource: 'STOCK',
        drawPileCount: 31,
        discardTop: {
          faceUp: true,
          revealedToViewer: true,
          suit: 'HEARTS',
          rank: 'ACE',
        },
      },
      players: [
        {
          userId: 10,
          username: 'player1',
          gamePlayerId: 101,
          seatNumber: 1,
          totalScore: 0,
          initialFlipsCount: 2,
          heldCard: {
            faceUp: false,
            revealedToViewer: true,
            suit: 'SPADES',
            rank: 'KING',
          },
          cards: [],
        },
      ],
      allRoundScores: [],
    })

    render(
      <MemoryRouter initialEntries={['/game?gameId=5']}>
        <GamePage />
      </MemoryRouter>,
    )

    expect(await screen.findByText('Card in hand')).toBeInTheDocument()
    expect(screen.getByText('King of Spades')).toBeInTheDocument()
    expect(screen.getByAltText('Held card: King of Spades')).toBeInTheDocument()
    expect(screen.getByAltText('my profile')).toHaveAttribute('src', profileImage)
    expect(screen.getByAltText('player1 profile')).toHaveAttribute('src', profileImage)

    await userEvent.click(screen.getByRole('button', { name: /Open profile/i }))
    expect(screen.getByRole('heading', { name: 'Profile' })).toBeInTheDocument()
    expect(screen.getAllByText('player1')).toHaveLength(3)

    await waitFor(() => {
      expect(getGameState).toHaveBeenCalledWith({ gameId: '5', userId: 10 })
    })
  })

  test('records completed games and wins once for profile stats', async () => {
    getGame.mockResolvedValue({ status: 'COMPLETED' })
    getGameState.mockResolvedValue({
      gameStatus: 'COMPLETED',
      status: 'COMPLETED',
      gameCode: 'WIN123',
      currentRound: 9,
      round: { status: 'SCORED' },
      players: [
        {
          userId: 10,
          username: 'player1',
          gamePlayerId: 101,
          seatNumber: 1,
          totalScore: 12,
          cards: [],
        },
        {
          userId: 11,
          username: 'player2',
          gamePlayerId: 102,
          seatNumber: 2,
          totalScore: 20,
          cards: [],
        },
      ],
      allRoundScores: [],
    })

    render(
      <MemoryRouter initialEntries={['/game?gameId=9']}>
        <GamePage />
      </MemoryRouter>,
    )

    expect(await screen.findByText('Game Over!')).toBeInTheDocument()

    await waitFor(() => {
      const profile = JSON.parse(localStorage.getItem('user_profiles'))['id:10']
      expect(profile.stats).toEqual({ gamesPlayed: 1, wins: 1 })
      expect(profile.completedGameIds).toEqual(['9'])
    })
  })
})
