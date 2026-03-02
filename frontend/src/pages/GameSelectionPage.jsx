import { useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import './LoginPage.css'

function GameSelectionPage() {
  const navigate = useNavigate()

  const user = useMemo(() => {
    const saved = localStorage.getItem('demo_user')
    return saved ? JSON.parse(saved) : null
  }, [])

  const logout = () => {
    localStorage.removeItem('demo_user')
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
      </div>
    </div>
  )
}

export default GameSelectionPage
