import axios from 'axios'
import { API_BASE_URL } from './authApi'

const headersFor = (userId) => ({ headers: { 'X-User-Id': String(userId) } })

export async function createGame({ userId, maxPlayers = 4 }) {
  const resp = await axios.post(`${API_BASE_URL}/api/games`, { maxPlayers }, headersFor(userId))
  return resp.data
}

export async function joinGame({ userId, gameCode }) {
  const resp = await axios.post(`${API_BASE_URL}/api/games/join`, { gameCode }, headersFor(userId))
  return resp.data
}

export async function getGame({ gameId }) {
  const resp = await axios.get(`${API_BASE_URL}/api/games/${gameId}`)
  return resp.data
}