import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { vi } from 'vitest'
import LoginPage from '../../pages/LoginPage'
import { login } from '../../services/authApi'

const mockNavigate = vi.fn()

vi.mock('../../services/authApi', () => ({
  login: vi.fn(),
}))

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  test('renders login page and accepts typing', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    )

    const usernameInput = screen.getByLabelText(/username/i)
    const passwordInput = screen.getByLabelText(/password/i)

    await user.type(usernameInput, 'testuser')
    await user.type(passwordInput, 'testpassword123')

    expect(usernameInput).toHaveValue('testuser')
    expect(passwordInput).toHaveValue('testpassword123')
  })

  test('shows error message when login fails', async () => {
    login.mockRejectedValue({
      response: {
        data: { message: 'Invalid username or password.' },
      },
    })

    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/username/i), 'wronguser')
    await user.type(screen.getByLabelText(/password/i), 'wrongpassword')
    await user.click(screen.getByRole('button', { name: /go!/i }))

    expect(await screen.findByText(/invalid username or password/i)).toBeInTheDocument()
    expect(mockNavigate).not.toHaveBeenCalled()
  })

  test('stores user and redirects when login succeeds', async () => {
    login.mockResolvedValue({
      userId: 1,
      username: 'testuser',
      email: 'test@example.com',
      message: 'Login successful.',
    })

    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/username/i), 'testuser')
    await user.type(screen.getByLabelText(/password/i), 'testpassword123')
    await user.click(screen.getByRole('button', { name: /go!/i }))

    expect(await screen.findByText(/login successful/i)).toBeInTheDocument()

    await waitFor(() => {
      expect(localStorage.getItem('demo_user')).toBeTruthy()
      expect(mockNavigate).toHaveBeenCalledWith('/game-selection')
    })

    expect(JSON.parse(localStorage.getItem('demo_user'))).toEqual({
      userId: 1,
      username: 'testuser',
      email: 'test@example.com',
    })
  })

    test('shows loading state while login request is pending', async () => {
    let resolveLogin

    login.mockImplementation(
      () =>
        new Promise((resolve) => {
          resolveLogin = resolve
        }),
    )

    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/username/i), 'testuser')
    await user.type(screen.getByLabelText(/password/i), 'testpassword123')
    await user.click(screen.getByRole('button', { name: /go!/i }))

    expect(screen.getByRole('button', { name: /logging in/i })).toBeDisabled()

    resolveLogin({
      userId: 1,
      username: 'testuser',
      email: 'test@example.com',
      message: 'Login successful.',
    })

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/game-selection')
    })
  })

  test('shows username required message for blank username', async () => {
    login.mockRejectedValue({
      response: {
        data: { message: 'Username is required.' },
      },
    })

    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/password/i), 'testpassword123')
    await user.click(screen.getByRole('button', { name: /go!/i }))

    expect(await screen.findByText(/username is required/i)).toBeInTheDocument()
  })

  test('shows password required message for blank password', async () => {
    login.mockRejectedValue({
      response: {
        data: { message: 'Password is required.' },
      },
    })

    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/username/i), 'testuser')
    await user.click(screen.getByRole('button', { name: /go!/i }))

    expect(await screen.findByText(/password is required/i)).toBeInTheDocument()
  })

  test('submits when Enter is pressed in a field', async () => {
    login.mockResolvedValue({
      userId: 1,
      username: 'testuser',
      email: 'test@example.com',
      message: 'Login successful.',
    })

    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/username/i), 'testuser')
    await user.type(screen.getByLabelText(/password/i), 'testpassword123{enter}')

    await waitFor(() => {
      expect(login).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'testpassword123',
      })
    })
  })

  test('passes whitespace-padded username through UI and still shows backend result', async () => {
    login.mockResolvedValue({
      userId: 1,
      username: 'testuser',
      email: 'test@example.com',
      message: 'Login successful.',
    })

    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/username/i), '  testuser  ')
    await user.type(screen.getByLabelText(/password/i), 'testpassword123')
    await user.click(screen.getByRole('button', { name: /go!/i }))

    await waitFor(() => {
      expect(login).toHaveBeenCalledWith({
        username: '  testuser  ',
        password: 'testpassword123',
      })
    })
  })
})