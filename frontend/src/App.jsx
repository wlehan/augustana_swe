import { BrowserRouter, Routes, Route } from 'react-router-dom'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import GameSelection from './pages/GameSelection'
import JoinCode from './pages/JoinCode'
import GamePage from './pages/Gamepage'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} /> 
        <Route path="/game-selection" element={<GameSelection />} />
        <Route path="/join" element={<JoinCode />} />
        <Route path="/play" element={<GamePage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
