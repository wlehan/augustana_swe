import apiClient from './apiClient'

export async function createGame({ maxPlayers = 4 }) {
  const resp = await apiClient.post('/api/games', { maxPlayers })
  return resp.data
}

export async function joinGame({ gameCode }) {
  const resp = await apiClient.post('/api/games/join', { gameCode })
  return resp.data
}

export async function leaveGame({ gameId }) {
  const resp = await apiClient.post(`/api/games/${gameId}/leave`, {})
  return resp.data
}

export async function getGame({ gameId }) {
  const resp = await apiClient.get(`/api/games/${gameId}`)
  return resp.data
}

export async function getGameState({ gameId }) {
  const resp = await apiClient.get(`/api/games/${gameId}/state`)
  return resp.data
}

export async function startGame({ gameId }) {
  const resp = await apiClient.post(`/api/games/${gameId}/start`, {})
  return resp.data
}



export async function flipInitialCard({ gameId, position }) {
  const resp = await apiClient.post(`/api/games/${gameId}/actions/flip-initial`, { position })
  return resp.data
}


export async function drawCard({ gameId, source }) {
  const resp = await apiClient.post(`/api/games/${gameId}/actions/draw`, { source })
  return resp.data
}


export async function swapCard({ gameId, position }) {
  const resp = await apiClient.post(`/api/games/${gameId}/actions/swap`, { position })
  return resp.data
}


export async function discardCard({ gameId, flipPosition = null }) {
  const resp = await apiClient.post(`/api/games/${gameId}/actions/discard`, { flipPosition })
  return resp.data
}
