import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, test, vi } from 'vitest'
import TutorialPage from '../../pages/TutorialPage'
import { startTutorial, getTutorialState, botFlip, botTurn } from '../../services/tutorialApi'
import { discardCard, drawCard, flipInitialCard, swapCard } from '../../services/gameApi'
import { clearStoredSession, hasAuthenticatedSession, readStoredSession } from '../../services/session'

vi.mock('../../services/tutorialApi', () => ({
  startTutorial: vi.fn(),
  getTutorialState: vi.fn(),
  botFlip: vi.fn(),
  botTurn: vi.fn(),
}))

vi.mock('../../services/gameApi', () => ({
  discardCard: vi.fn(),
  drawCard: vi.fn(),
  flipInitialCard: vi.fn(),
  swapCard: vi.fn(),
}))

vi.mock('../../services/session', () => ({
  clearStoredSession: vi.fn(),
  hasAuthenticatedSession: vi.fn(),
  readStoredSession: vi.fn(),
}))

const mockNavigate = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

describe('TutorialPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    sessionStorage.clear()
    mockNavigate.mockClear()
  })

  test('redirects to login if user is not authenticated', () => {
    readStoredSession.mockReturnValue(null)
    hasAuthenticatedSession.mockReturnValue(false)

    render(
      <MemoryRouter>
        <TutorialPage />
      </MemoryRouter>
    )

    expect(clearStoredSession).toHaveBeenCalled()
    expect(mockNavigate).toHaveBeenCalledWith('/login', { replace: true })
  })

  test('shows loading state while starting tutorial', () => {
    const mockUser = { userId: 1, username: 'testuser' }
    readStoredSession.mockReturnValue(mockUser)
    hasAuthenticatedSession.mockReturnValue(true)
    startTutorial.mockImplementation(() => new Promise(() => {})) // Never resolves

    render(
      <MemoryRouter>
        <TutorialPage />
      </MemoryRouter>
    )

    expect(screen.getByText('Setting up your tutorial…')).toBeInTheDocument()
  })

  test('shows error message when tutorial start fails', async () => {
    const mockUser = { userId: 1, username: 'testuser' }
    readStoredSession.mockReturnValue(mockUser)
    hasAuthenticatedSession.mockReturnValue(true)
    startTutorial.mockRejectedValue({
      response: { data: { message: 'Tutorial start failed' } }
    })

    render(
      <MemoryRouter>
        <TutorialPage />
      </MemoryRouter>
    )

    expect(await screen.findByText('Tutorial start failed')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /back to lobby/i })).toBeInTheDocument()
  })

  test('starts tutorial and shows welcome step', async () => {
    const mockUser = { userId: 1, username: 'testuser' }
    readStoredSession.mockReturnValue(mockUser)
    hasAuthenticatedSession.mockReturnValue(true)

    startTutorial.mockResolvedValue({
      gameId: 'tutorial-123',
      currentStep: 'WELCOME',
      stepTitle: 'Welcome to Six-Card Golf!',
      stepDescription: 'This is a tutorial to learn how to play.',
      game: {
        players: [
          {
            userId: 1,
            username: 'testuser',
            gamePlayerId: 101,
            seatNumber: 1,
            totalScore: 0,
            cards: [],
          },
          {
            userId: 2,
            username: 'Bot',
            gamePlayerId: 102,
            seatNumber: 2,
            totalScore: 0,
            cards: [],
          },
        ],
      },
    })

    render(
      <MemoryRouter>
        <TutorialPage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(startTutorial).toHaveBeenCalled()
    })

    expect(await screen.findByText('Tutorial')).toBeInTheDocument()
  })

  test('exit tutorial button navigates to game selection', async () => {
    const mockUser = { userId: 1, username: 'testuser' }
    readStoredSession.mockReturnValue(mockUser)
    hasAuthenticatedSession.mockReturnValue(true)

    startTutorial.mockResolvedValue({
      gameId: 'tutorial-123',
      currentStep: 'WELCOME',
      game: {
        players: [
          {
            userId: 1,
            username: 'testuser',
            gamePlayerId: 101,
            seatNumber: 1,
            totalScore: 0,
            cards: [],
          },
        ],
      },
    })

    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <TutorialPage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.getByText('← Exit Tutorial')).toBeInTheDocument()
    })

    const exitButton = screen.getByRole('button', { name: /exit tutorial/i })
    await user.click(exitButton)

    expect(mockNavigate).toHaveBeenCalledWith('/game-selection')
  })

  test('shows tutorial complete hint when tutorial is finished', async () => {
    const mockUser = { userId: 1, username: 'testuser' }
    readStoredSession.mockReturnValue(mockUser)
    hasAuthenticatedSession.mockReturnValue(true)

    startTutorial.mockResolvedValue({
      gameId: 'tutorial-123',
      currentStep: 'TUTORIAL_COMPLETE',
      stepTitle: 'Tutorial Complete!',
      stepDescription: 'You have completed the tutorial.',
      game: {
        players: [
          {
            userId: 1,
            username: 'testuser',
            gamePlayerId: 101,
            seatNumber: 1,
            totalScore: 12,
            cards: [],
          },
          {
            userId: 2,
            username: 'Bot',
            gamePlayerId: 102,
            seatNumber: 2,
            totalScore: 20,
            cards: [],
          },
        ],
      },
    })

    render(
      <MemoryRouter>
        <TutorialPage />
      </MemoryRouter>
    )

    expect(await screen.findByText('Tutorial Complete!')).toBeInTheDocument()
    expect(screen.getByText('Tutorial')).toBeInTheDocument()
  })
})