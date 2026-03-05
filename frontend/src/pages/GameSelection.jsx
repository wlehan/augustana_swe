import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './HomePage.css'; 
import './GameSelection.css'
import './Gamepage.css'
import gearIcon from '../assets/Icon.png';
import profileIcon from '../assets/profile.png';
import { createGame, joinGame } from '../services/gameApi'

function GameSelection() {
  const navigate = useNavigate();
  const [createdGame, setCreatedGame] = useState(null);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [isSfxEnabled, setIsSfxEnabled] = useState(() => localStorage.getItem('sfx_enabled') !== 'false');
  const settingsRef = useRef(null);

  const user = useMemo(() => {
    const raw = localStorage.getItem('demo_user');
    if (!raw) {
      return { username: 'Guest' };
    }

    try {
      return JSON.parse(raw);
    } catch {
      return { username: 'Guest' };
    }
  }, []);

  useEffect(() => {
    localStorage.setItem('sfx_enabled', isSfxEnabled ? 'true' : 'false');
  }, [isSfxEnabled]);

  useEffect(() => {
    if (!isSettingsOpen && !isProfileOpen) {
      return undefined;
    }

    const handleClickOutside = (event) => {
      if (isSettingsOpen && settingsRef.current && !settingsRef.current.contains(event.target)) {
        setIsSettingsOpen(false);
      }
    };

    const handleEscape = (event) => {
      if (event.key === 'Escape') {
        setIsSettingsOpen(false);
        setIsProfileOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('keydown', handleEscape);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleEscape);
    };
  }, [isSettingsOpen, isProfileOpen]);

  const handleLogout = () => {
    localStorage.removeItem('demo_user');
    navigate('/login');
  };

  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const onStartGame = async () => {
    setErrorMsg('');

    if (!user?.userId) {
      navigate('/login');
      return;
    }

    setLoading(true);
    try {
      const data = await createGame({ userId: user.userId, maxPlayers: 4 });
      localStorage.setItem('active_game', JSON.stringify(data));
      setCreatedGame(data);
      navigate(`/play?gameId=${data.gameId}`);
    } catch (e) {
      setErrorMsg(e?.response?.data?.message || 'Failed to create game');
    } finally {
      setLoading(false);
    }
  };

  const onJoinGame = async () => {
    setErrorMsg('');

    if (!user?.userId) {
      navigate('/login');
      return;
    }

    const code = window.prompt('Enter game code (6 characters):');
    if (!code) return;

    setLoading(true);
    try {
      const data = await joinGame({ userId: user.userId, gameCode: code.trim() });
      localStorage.setItem('active_game', JSON.stringify(data));
      navigate(`/play?gameId=${data.gameId}`);
    } catch (e) {
      setErrorMsg(e?.response?.data?.message || 'Failed to join game');
    } finally {
      setLoading(false);
    }
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
        <div className="settings-wrap" ref={settingsRef}>
          <button
            className="icon-btn"
            onClick={() => setIsSettingsOpen((prev) => !prev)}
            aria-label="Open settings"
            aria-expanded={isSettingsOpen}
          >
            <img src={gearIcon} className="nav-image" alt="Settings" />
          </button>

          {isSettingsOpen && (
            <div className="settings-menu">
              <p className="settings-title">Settings</p>
              <button
                className="settings-item"
                type="button"
                onClick={() => setIsSfxEnabled((prev) => !prev)}
              >
                <span>Sound effects</span>
                <span className={`toggle-pill ${isSfxEnabled ? 'on' : ''}`}>
                  {isSfxEnabled ? 'On' : 'Off'}
                </span>
              </button>
              <button className="settings-item danger" type="button" onClick={handleLogout}>
                Log out
              </button>
            </div>
          )}
        </div>

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
