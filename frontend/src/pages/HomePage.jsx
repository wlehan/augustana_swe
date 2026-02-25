import { useNavigate } from 'react-router-dom'
import './HomePage.css'
import golfLogo from '../assets/golfball.png'
import homepageCards from '../assets/homepage_cards.png'

function HomePage() {
  const navigate = useNavigate()

return (
    <div className="home-container">
      <div className="content-wrapper">
        <div className="title-section">
          <img src={golfLogo} className="golf-ball-img" alt="Golf Ball" />  
          <h1 className="main-title">Six-Card <br />Golf</h1>
        </div>

        <div className="game-area">
          {/* King of Clubs Card */}
          <div className="card-window king-card">
            <img src={homepageCards} alt="King Card" />
          </div>

        <div className="card-window middle-card">
          <img src={homepageCards} alt="Seven Card" />
        </div>
          
        <div className="button-stack">
            <button className="green-btn" onClick={() => navigate('/login')}>Log in</button>
            <button className="green-btn">Create an account</button>
          </div>

          {/* Green Card Back */}
          <div className="card-window green-card">
            <img src={homepageCards} alt="Green Card" />
          </div>
        </div>
      </div>
    </div>
  );
}

export default HomePage;