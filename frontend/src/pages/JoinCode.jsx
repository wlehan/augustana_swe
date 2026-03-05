import './JoinCode.css'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

function JoinCode({ onClose }) {
    const navigate = useNavigate()
    const [joinCode, setJoinCode] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

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

            // TODO: Call API to join game with joinCode
            // await joinGame({ code: joinCode })
            
            // For now, navigate to game page
            navigate(`/game/${joinCode}`)
        } catch (apiError) {
            setError(apiError.response?.data?.message || 'Failed to join game.')
        } finally {
            setLoading(false)
        }
    }

    const handleClose = () => {
        setJoinCode('')
        setError('')
        if (onClose) onClose()
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
