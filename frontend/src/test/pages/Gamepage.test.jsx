import { render, screen, waitFor } from '@testing-library/react'
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
    localStorage.setItem('demo_user', JSON.stringify({ userId: 10, username: 'player1' }))
  })

  test('shows the current player their drawn held card', async () => {
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

    await waitFor(() => {
      expect(getGameState).toHaveBeenCalledWith({ gameId: '5', userId: 10 })
    })
  })
})
