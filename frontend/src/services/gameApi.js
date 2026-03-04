// src/services/gameApi.js
import axios from 'axios'

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

function getUserId() {
  const saved = localStorage.getItem('demo_user')
  if (!saved) throw new Error('Not logged in')
  const user = JSON.parse(saved)
  if (!user?.userId) throw new Error('Missing userId')
  return String(user.userId)
}

function authHeaders() {
  return { 'X-User-Id': getUserId() }
}

export async function createGame({ maxPlayers = 4 } = {}) {
  const res = await axios.post(`${BASE_URL}/api/games`, { maxPlayers }, { headers: authHeaders() })
  return res.data
}

export async function joinGame({ gameCode }) {
  const res = await axios.post(`${BASE_URL}/api/games/join`, { gameCode }, { headers: authHeaders() })
  return res.data
}

export async function getGame({ gameId }) {
  const res = await axios.get(`${BASE_URL}/api/games/${gameId}`, { headers: authHeaders() })
  return res.data
}