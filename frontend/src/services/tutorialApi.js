import apiClient from './apiClient'

export async function startTutorial() {
  const res = await apiClient.post('/api/tutorial/start')
  return res.data
}

export async function getTutorialState({ gameId }) {
  const res = await apiClient.get(`/api/tutorial/${gameId}/state`)
  return res.data
}

export async function botFlip({ gameId }) {
  const res = await apiClient.post(`/api/tutorial/${gameId}/bot-flip`)
  return res.data
}

export async function botTurn({ gameId }) {
  const res = await apiClient.post(`/api/tutorial/${gameId}/bot-turn`)
  return res.data
}
