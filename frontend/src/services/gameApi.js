import axios from 'axios'
import { API_BASE_URL } from './authApi'

const headersFor = (userId) =>
  userId === null || userId === undefined
    ? {}
    : { headers: { 'X-User-Id': String(userId) } }

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

export async function getGameState({ gameId, userId }) {
  const resp = await axios.get(`${API_BASE_URL}/api/games/${gameId}/state`, headersFor(userId))
  return resp.data
}

export async function startGame({ gameId, userId }) {
  const resp = await axios.post(
    `${API_BASE_URL}/api/games/${gameId}/start`,
    {},
    headersFor(userId)
  )
  return resp.data
}



export async function flipInitialCard({ gameId, userId, position }) {
  const resp = await axios.post(
    `${API_BASE_URL}/api/games/${gameId}/actions/flip-initial`,
    { position },
    headersFor(userId)
  )
  return resp.data
}


export async function drawCard({ gameId, userId, source }) {
  const resp = await axios.post(
    `${API_BASE_URL}/api/games/${gameId}/actions/draw`,
    { source },
    headersFor(userId)
  )
  return resp.data
}


export async function swapCard({ gameId, userId, position }) {
  const resp = await axios.post(
    `${API_BASE_URL}/api/games/${gameId}/actions/swap`,
    { position },
    headersFor(userId)
  )
  return resp.data
}


export async function discardCard({ gameId, userId, flipPosition = null }) {
  const resp = await axios.post(
    `${API_BASE_URL}/api/games/${gameId}/actions/discard`,
    { flipPosition },
    headersFor(userId)
  )
  return resp.data
}
