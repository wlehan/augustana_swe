import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { signup } from '../services/authApi'
import './LoginPage.css'

function SignupPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ username: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  const onChange = (event) => {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const onSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setSuccess('')
    setLoading(true)

    try {
      const data = await signup(form)
      setSuccess(data.message || 'Account created.')

      localStorage.setItem(
        'demo_user',
        JSON.stringify({ userId: data.userId, username: data.username, email: data.email }),
      )

      setTimeout(() => navigate('/game-selection'), 400)
    } catch (apiError) {
      setError(apiError.response?.data?.message || 'Signup failed.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-container">
      <div className="login-window">
        <h1 className="login-title">Sign Up</h1>
        <form className="auth-form" onSubmit={onSubmit}>
          <div className="input-group">
            <label className="input-label" htmlFor="username">
              Username
            </label>
            <input
              type="text"
              id="username"
              name="username"
              className="text-input"
              placeholder="Enter username"
              value={form.username}
              onChange={onChange}
            />
          </div>

          <div className="input-group">
            <label className="input-label" htmlFor="email">
              Email (optional)
            </label>
            <input
              type="email"
              id="email"
              name="email"
              className="text-input"
              placeholder="Enter email"
              value={form.email}
              onChange={onChange}
            />
          </div>

          <div className="input-group">
            <label className="input-label" htmlFor="password">
              Password
            </label>
            <input
              type="password"
              id="password"
              name="password"
              className="text-input"
              placeholder="At least 6 characters"
              value={form.password}
              onChange={onChange}
            />
          </div>

          {error && <p className="auth-message auth-error">{error}</p>}
          {success && <p className="auth-message auth-success">{success}</p>}

          <button className="green-btn login-button" type="submit" disabled={loading}>
            {loading ? 'Creating...' : 'Create Account'}
          </button>

          <button className="auth-link" type="button" onClick={() => navigate('/login')}>
            Already have an account? Log in
          </button>
        </form>
      </div>
    </div>
  )
}

export default SignupPage
