import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import GameSelectionPage from '../../pages/GameSelectionPage'

const mockNavigate = vi.fn()
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}))

describe('GameSelectionPage', () => {
  beforeEach(() => {
    localStorage.clear()
    sessionStorage.clear()
    mockNavigate.mockReset()
  })

  test('renders logged-in username and shows action buttons', () => {
    localStorage.setItem('demo_user', JSON.stringify({ username: 'Tester' }))

    render(<GameSelectionPage />)

    expect(screen.getByText(/Logged in as:/i)).toBeInTheDocument()
    expect(screen.getByText('Tester')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Log Out/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Start a game/i })).toBeInTheDocument()
  })

  test('logout clears user and navigates home', async () => {
    localStorage.setItem('demo_user', JSON.stringify({ username: 'Tester' }))

    render(<GameSelectionPage />)

    await userEvent.click(screen.getByRole('button', { name: /Log Out/i }))

    expect(localStorage.getItem('demo_user')).toBeNull()
    expect(mockNavigate).toHaveBeenCalledWith('/')
  })

  test('start game button navigates to game page', async () => {
    localStorage.setItem('demo_user', JSON.stringify({ username: 'Tester' }))

    render(<GameSelectionPage />)

    await userEvent.click(screen.getByRole('button', { name: /Start a game/i }))

    expect(mockNavigate).toHaveBeenCalledWith('/game')
  })
})
