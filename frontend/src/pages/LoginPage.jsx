import './LoginPage.css'
import { useNavigate } from 'react-router-dom'

function LoginPage() {
    const navigate = useNavigate();

    return (
        <div className="login-container">
            <div className="login-window">
                <h1 className="login-title">Login</h1>
                <div className="input-group">
                    <label className="input-label" htmlFor="username">
                      Username
                    </label>
                    <input
                      type="text"
                      id="username"
                      className="text-input"
                      placeholder="Enter here"
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
                      placeholder="Enter here"
                    />
                </div>
                <button className="green-btn login-button" onClick={() => navigate("/game-selection")}>
                    Go!
                </button>
            </div>
        </div>
    );
}

export default LoginPage;