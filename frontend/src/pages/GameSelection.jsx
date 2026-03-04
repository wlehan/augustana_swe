import { useNavigate } from 'react-router-dom';
import './HomePage.css'; 
import './GameSelection.css'
import './Gamepage.css'
import gearIcon from '../assets/Icon.png';
import profileIcon from '../assets/profile.png';

function GameSelection() {
  const navigate = useNavigate();

  return (
    <div className="home-container">
      <div className="top-nav">
        <button className="icon-btn" onClick={() => navigate('/settings')}>
          <img src={gearIcon} className="nav-image" alt="Settings" />
        </button>
        <button className="icon-btn" onClick={() => navigate('/profile')}>
          <img src={profileIcon} className="nav-image" alt="Profile" />
        </button>
      </div>

      <div className="content-wrapper selection-wrapper">
        <div className="selection-grid">
          <button className="square-selection-btn" onClick={() => navigate('/Gamepage')}>
            Start a game
          </button>
          
          <button className="square-selection-btn" onClick={() => navigate('/join-lobby')}>
            Join a game
          </button>
        </div>
      </div>
    </div>
  );
}

export default GameSelection;