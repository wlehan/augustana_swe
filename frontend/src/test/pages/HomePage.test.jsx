import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { vi } from 'vitest'
import HomePage from '../../pages/HomePage'

const mockNavigate = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

describe('HomePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    sessionStorage.clear()
  })

  test('renders the main title and golf ball image', () => {
    render(
      <MemoryRouter>
        <HomePage />
      </MemoryRouter>
    )

    expect(screen.getByRole('heading', { name: /Six-Card.*Golf/i })).toBeInTheDocument()
    expect(screen.getByAltText('Golf Ball')).toBeInTheDocument()
  })

  test('renders login and signup buttons', () => {
    render(
      <MemoryRouter>
        <HomePage />
      </MemoryRouter>
    )

    expect(screen.getByRole('button', { name: /log in/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /create an account/i })).toBeInTheDocument()
  })

  test('renders card images', () => {
    render(
      <MemoryRouter>
        <HomePage />
      </MemoryRouter>
    )

    expect(screen.getByAltText('King Card')).toBeInTheDocument()
    expect(screen.getByAltText('Seven Card')).toBeInTheDocument()
    expect(screen.getByAltText('Green Card')).toBeInTheDocument()
  })

  test('navigates to login page when login button is clicked', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <HomePage />
      </MemoryRouter>
    )

    const loginButton = screen.getByRole('button', { name: /log in/i })
    await user.click(loginButton)

    expect(mockNavigate).toHaveBeenCalledWith('/login')
  })

  test('navigates to signup page when create account button is clicked', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <HomePage />
      </MemoryRouter>
    )

    const signupButton = screen.getByRole('button', { name: /create an account/i })
    await user.click(signupButton)

    expect(mockNavigate).toHaveBeenCalledWith('/signup')
  })
})