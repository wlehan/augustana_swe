import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { vi } from 'vitest'
import SignupPage from '../../pages/SignupPage'
import { signup } from '../../services/authApi'

const mockNavigate = vi.fn()

vi.mock('../../services/authApi', () => ({
  signup: vi.fn(),
}))

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

describe('SignupPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  test('renders signup page and password progress text updates', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <SignupPage />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/password/i), 'short')
    expect(screen.getByText(/5\/12 characters/i)).toBeInTheDocument()

    await user.clear(screen.getByLabelText(/password/i))
    await user.type(screen.getByLabelText(/password/i), 'verysecurepass')
    expect(screen.getByText(/good password \(12\/12\)/i)).toBeInTheDocument()
  })

  test('shows error message when signup fails', async () => {
    signup.mockRejectedValue({
      response: {
        data: { message: 'Username already exists.' },
      },
    })

    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <SignupPage />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/username/i), 'existinguser')
    await user.type(screen.getByLabelText(/email/i), 'existing@test.com')
    await user.type(screen.getByLabelText(/password/i), 'verysecurepass')
    await user.click(screen.getByRole('button', { name: /create account/i }))

    expect(await screen.findByText(/username already exists/i)).toBeInTheDocument()
    expect(mockNavigate).not.toHaveBeenCalled()
  })

  test('stores user and redirects when signup succeeds', async () => {
    signup.mockResolvedValue({
      userId: 2,
      username: 'newuser',
      email: 'newuser@test.com',
      message: 'Signup successful.',
    })

    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <SignupPage />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/username/i), 'newuser')
    await user.type(screen.getByLabelText(/email/i), 'newuser@test.com')
    await user.type(screen.getByLabelText(/password/i), 'verysecurepass')
    await user.click(screen.getByRole('button', { name: /create account/i }))

    expect(await screen.findByText(/signup successful/i)).toBeInTheDocument()

    await waitFor(() => {
      expect(localStorage.getItem('demo_user')).toBeTruthy()
      expect(mockNavigate).toHaveBeenCalledWith('/game-selection')
    })

    expect(JSON.parse(localStorage.getItem('demo_user'))).toEqual({
      userId: 2,
      username: 'newuser',
      email: 'newuser@test.com',
    })
  })

  test('shows loading state while signup request is pending', async () => {
    let resolveSignup

    signup.mockImplementation(
      () =>
        new Promise((resolve) => {
          resolveSignup = resolve
        }),
    )

    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <SignupPage />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/username/i), 'newuser')
    await user.type(screen.getByLabelText(/email/i), 'newuser@test.com')
    await user.type(screen.getByLabelText(/password/i), 'verysecurepass')
    await user.click(screen.getByRole('button', { name: /create account/i }))

    expect(screen.getByRole('button', { name: /creating/i })).toBeDisabled()

    resolveSignup({
      userId: 2,
      username: 'newuser',
      email: 'newuser@test.com',
      message: 'Signup successful.',
    })

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/game-selection')
    })
  })

  test('allows blank optional email and submits it as empty string', async () => {
    signup.mockResolvedValue({
      userId: 2,
      username: 'newuser',
      email: null,
      message: 'Signup successful.',
    })

    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <SignupPage />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/username/i), 'newuser')
    await user.type(screen.getByLabelText(/password/i), 'verysecurepass')
    await user.click(screen.getByRole('button', { name: /create account/i }))

    await waitFor(() => {
      expect(signup).toHaveBeenCalledWith({
        username: 'newuser',
        email: '',
        password: 'verysecurepass',
      })
    })
  })

  test('blocks whitespace-only optional email through UI', async () => {
    signup.mockResolvedValue({
      userId: 2,
      username: 'newuser',
      email: null,
      message: 'Signup successful.',
    })

    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <SignupPage />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/username/i), 'newuser')
    await user.type(screen.getByLabelText(/email/i), '   ')
    await user.type(screen.getByLabelText(/password/i), 'verysecurepass')
    await user.click(screen.getByRole('button', { name: /create account/i }))

    expect(signup).toHaveBeenCalledWith({
      username: 'newuser',
      email: "",
      password: 'verysecurepass',
    })
  })

  test('shows backend short-password error', async () => {
    signup.mockRejectedValue({
      response: {
        data: { message: 'Password must be at least 12 characters.' },
      },
    })

    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <SignupPage />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/username/i), 'newuser')
    await user.type(screen.getByLabelText(/password/i), 'short')
    await user.click(screen.getByRole('button', { name: /create account/i }))

    expect(await screen.findByText(/password must be at least 12 characters/i)).toBeInTheDocument()
  })

  test('submits when Enter is pressed', async () => {
    signup.mockResolvedValue({
      userId: 2,
      username: 'newuser',
      email: 'newuser@test.com',
      message: 'Signup successful.',
    })

    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <SignupPage />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/username/i), 'newuser')
    await user.type(screen.getByLabelText(/email/i), 'newuser@test.com')
    await user.type(screen.getByLabelText(/password/i), 'verysecurepass{enter}')

    await waitFor(() => {
      expect(signup).toHaveBeenCalledWith({
        username: 'newuser',
        email: 'newuser@test.com',
        password: 'verysecurepass',
      })
    })
  })
})