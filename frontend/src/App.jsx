import { BrowserRouter, Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import GameSelection from "./pages/GameSelection";
import JoinCode from "./pages/JoinCode";
import GamePage from "./pages/Gamepage";
import ProtectedRoute from "./components/ProtectedRoute";
import { AudioProvider } from "./audio/AudioProvider";
import "./App.css";

function App() {
  return (
    <AudioProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/tutorial" element={<TutorialPage />} />
          <Route
            path="/game-selection"
            element={
              <ProtectedRoute>
                <GameSelection />
              </ProtectedRoute>
            }
          />
          <Route
            path="/join"
            element={
              <ProtectedRoute>
                <JoinCode />
              </ProtectedRoute>
            }
          />
          <Route
            path="/play"
            element={
              <ProtectedRoute>
                <GamePage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </BrowserRouter>
    </AudioProvider>
  );
}

export default App;
