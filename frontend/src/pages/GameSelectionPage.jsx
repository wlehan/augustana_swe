import { useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import './LoginPage.css'
import { clearStoredSession, readStoredSession } from '../services/session'

function GameSelectionPage() {
  const navigate = useNavigate()

  const user = useMemo(() => {
    return readStoredSession()
  }, [])

  const logout = () => {
    clearStoredSession()
    navigate('/')
  }

  return (
    <div className="login-container">
      <div className="login-window">
        <h1 className="login-title">Welcome</h1>
        <p className="auth-message auth-success">
          Logged in as: <strong>{user?.username || 'Guest'}</strong>
        </p>
        <button className="green-btn login-button" type="button" onClick={logout}>
          Log Out
        </button>
        <button className="green-btn" onClick={() => navigate('/game')}>
          Start a game
        </button>
      </div>
    </div>
  )
}

export default GameSelectionPage
