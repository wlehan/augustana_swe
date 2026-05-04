import './LoginPage.css'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { login } from '../services/authApi'
import { saveStoredSession } from '../services/session'

/**
 * Login form that saves the returned session and enters the game lobby.
 */
function LoginPage() {
    const navigate = useNavigate()
    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')
    const [loading, setLoading] = useState(false)

    const onSubmit = async (event) => {
        event.preventDefault()
        setError('')
        setSuccess('')
        setLoading(true)

        try {
            const data = await login({ username, password })
            saveStoredSession(data)
            setSuccess(data.message || 'Login successful.')
            setTimeout(() => navigate('/game-selection'), 400)
        } catch (apiError) {
            setError(apiError.response?.data?.message || 'Login failed.')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="login-container">
            <div className="login-window">
                <h1 className="login-title">Login</h1>
                <form className="auth-form" onSubmit={onSubmit}>
                    <div className="input-group">
                        <label className="input-label" htmlFor="username">
                            Username
                        </label>
                        <input
                            type="text"
                            id="username"
                            className="text-input"
                            placeholder="Enter username"
                            value={username}
                            onChange={(event) => setUsername(event.target.value)}
                        />
                    </div>
                    <div className="input-group">
                        <label className="input-label" htmlFor="password">
                            Password
                        </label>
                        <input
                            type="password"
                            id="password"
                            className="text-input"
                            placeholder="Enter password"
                            value={password}
                            onChange={(event) => setPassword(event.target.value)}
                        />
                    </div>

                    {error && <p className="auth-message auth-error">{error}</p>}
                    {success && <p className="auth-message auth-success">{success}</p>}

                    <button className="green-btn login-button" type="submit" disabled={loading}>
                        {loading ? 'Logging in...' : 'Go!'}
                    </button>

                    <button className="auth-link" type="button" onClick={() => navigate('/signup')}>
                        New here? Create an account
                    </button>
                </form>
            </div>
        </div>
    )
}

export default LoginPage
