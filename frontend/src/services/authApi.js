import apiClient, { API_BASE_URL } from './apiClient'

export async function signup({ username, password, email }) {
  const response = await apiClient.post('/api/auth/signup', {
    username,
    password,
    email,
  })

  return response.data
}

export async function login({ username, password }) {
  const response = await apiClient.post('/api/auth/login', {
    username,
    password,
  })

  return response.data
}

export { API_BASE_URL }
