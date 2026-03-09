import './JoinCode.css'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { joinGame } from '../services/gameApi'

function JoinCode({ onClose }) {
    const navigate = useNavigate()
    const [joinCode, setJoinCode] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const extractJoinError = (apiError) => {
        if (!apiError?.response) {
            return 'Could not reach server (possible CORS/port mismatch).'
        }

        const { status, data } = apiError.response
        const message =
            data?.message ||
            data?.error ||
            (typeof data === 'string' ? data : '') ||
            apiError.message

        return message
            ? `Join failed (${status}): ${message}`
            : `Join failed (${status}).`
    }

    const handleSubmit = async (event) => {
        event.preventDefault()
        setError('')
        setLoading(true)

        try {
            if (!joinCode.trim()) {
                setError('Please enter a join code')
                setLoading(false)
                return
            }

            const raw = localStorage.getItem('demo_user')
            let user = null
            try {
                user = raw ? JSON.parse(raw) : null
            } catch {
                user = null
            }

            if (!user?.userId) {
                navigate('/login')
                return
            }

            const code = joinCode.trim().toUpperCase()
            const data = await joinGame({ userId: user.userId, gameCode: code })

            localStorage.setItem('active_game', JSON.stringify(data))
            navigate(`/play?gameId=${data.gameId}`)

        } catch (apiError) {
            setError(extractJoinError(apiError))
        } finally {
            setLoading(false)
        }
    }

    const handleClose = () => {
        setJoinCode('')
        setError('')
        if (onClose) onClose()
        else navigate('/game-selection')
    }

    return (
        <div className="join-code-overlay">
            <div className="join-code-modal">
                <button className="join-code-close" onClick={handleClose} aria-label="Close">
                    ✕
                </button>

                <h1 className="join-code-title">Enter the join code</h1>

                <form className="join-code-form" onSubmit={handleSubmit}>
                    <div className="join-code-input-group">
                        <label className="join-code-label" htmlFor="join-code">
                            Join code
                        </label>
                        <input
                            type="text"
                            id="join-code"
                            className="join-code-input"
                            placeholder="Enter here"
                            value={joinCode}
                            onChange={(event) => setJoinCode(event.target.value)}
                            maxLength="10"
                        />
                    </div>

                    {error && <p className="join-code-error">{error}</p>}

                    <button className="green-btn join-code-button" type="submit" disabled={loading}>
                        {loading ? 'Joining...' : 'Go!'}
                    </button>
                </form>
            </div>
        </div>
    )
}

export default JoinCode
