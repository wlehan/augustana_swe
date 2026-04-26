import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './HomePage.css'; 
import './GameSelection.css'
import './Gamepage.css'
import gearIcon from '../assets/Icon.png';
import profileIcon from '../assets/profile.png';
import { createGame } from '../services/gameApi'
import {
  clearStoredSession,
  hasAuthenticatedSession,
  isUnauthorizedError,
  readStoredSession,
} from '../services/session'
import AudioSettingsButton from '../components/AudioSettingsButton';


function GameSelection() {
  const navigate = useNavigate();
  const [isProfileOpen, setIsProfileOpen] = useState(false);

  const user = useMemo(() => {
    const session = readStoredSession();
    if (!session) {
      return { username: 'Guest' };
    }
    return session;
  }, []);

  useEffect(() => {
    if (!isProfileOpen) {
      return undefined;
    }

    const handleEscape = (event) => {
      if (event.key === 'Escape') {
        setIsProfileOpen(false);
      }
    };

    document.addEventListener('keydown', handleEscape);
    return () => {
      document.removeEventListener('keydown', handleEscape);
    };
  }, [isProfileOpen]);

  const handleLogout = () => {
    clearStoredSession();
    navigate('/login');
  };

  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const onStartGame = async () => {
    setErrorMsg('');

    if (!hasAuthenticatedSession(user)) {
      clearStoredSession();
      navigate('/login');
      return;
    }

    setLoading(true);
    try {
      const data = await createGame({ userId: user.userId, maxPlayers: 4 });
      localStorage.setItem('active_game', JSON.stringify(data));
      navigate(`/play?gameId=${data.gameId}`);
    } catch (e) {
      if (isUnauthorizedError(e)) {
        clearStoredSession();
        navigate('/login');
        return;
      }
      setErrorMsg(e?.response?.data?.message || 'Failed to create game');
    } finally {
      setLoading(false);
    }
  };

  const onJoinGame = () => {
      if (!hasAuthenticatedSession(user)) {
        clearStoredSession();
        navigate('/login');
        return;
      }
      navigate('/join');
    };

  const onPlayTutorial = () => {
    if (!user?.userId) { navigate('/login'); return; }
    navigate('/tutorial');
  };

  return (
    <div className="home-container">
      <div className="golf-decor-scene">
        <div className="selection-scene-copy">
          <p className="selection-scene-kicker">Six-Card Golf</p>
          <h2 className="selection-scene-title">Game Lobby</h2>
          <p className="selection-scene-subtitle">Choose how you want to play</p>
        </div>
        <div className="golf-art-cluster" aria-hidden="true">
          <div className="golf-flag-group">
            <span className="golf-flag-pole" />
            <span className="golf-flag-cloth" />
            <span className="golf-hole-cup" />
          </div>
          <div className="golf-ball" />
          <div className="golf-club">
            <span className="golf-club-shaft" />
            <span className="golf-club-head" />
          </div>
        </div>
      </div>

      <div className="top-nav">
        <AudioSettingsButton iconSrc={gearIcon} iconAlt="Settings">
          <button className="settings-item danger" type="button" onClick={handleLogout}>
            Log out
          </button>
        </AudioSettingsButton>

        <button className="icon-btn" onClick={() => setIsProfileOpen(true)} aria-label="Open profile">
          <img src={profileIcon} className="nav-image" alt="Profile" />
        </button>
      </div>

      <div className="content-wrapper selection-wrapper">
        <div className="selection-grid">
          <button
            className="square-selection-btn"
            onClick={onStartGame}
            disabled={loading}
          >
            {loading ? 'Starting...' : 'Start a game'}
          </button>

          <button
            className="square-selection-btn"
            onClick={onJoinGame}
            disabled={loading}
          >
            {loading ? 'Joining...' : 'Join a game'}
          </button>

          <button
            className="square-selection-btn tutorial-selection-btn"
            onClick={onPlayTutorial}
            disabled={loading}
          >
            Play Tutorial
          </button>

        </div>
          {errorMsg && <p className="error-text">{errorMsg}</p>}
      </div>

      {isProfileOpen && (
        <div className="profile-modal-overlay" onClick={() => setIsProfileOpen(false)}>
          <div className="profile-modal-card" onClick={(event) => event.stopPropagation()}>
            <button
              className="profile-close-btn"
              type="button"
              onClick={() => setIsProfileOpen(false)}
              aria-label="Close profile"
            >
              <span aria-hidden="true">×</span>
            </button>
            <h2 className="profile-modal-title">Profile</h2>
            <img className="profile-avatar-large" src={profileIcon} alt="Profile avatar" />
            <p className="profile-name">{user.username || 'Guest'}</p>

            <div className="stats-section">
              <h3>Stats</h3>
              <div className="stat-row">
                <span>Games played</span>
                <strong>{Number(user.gamesPlayed || 0)}</strong>
              </div>
              <div className="stat-row">
                <span>Wins</span>
                <strong>{Number(user.wins || 0)}</strong>
              </div>
            </div>

            <button className="green-btn profile-done-btn" type="button" onClick={() => setIsProfileOpen(false)}>
              Done
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default GameSelection;
