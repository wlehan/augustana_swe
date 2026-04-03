import axios from 'axios'

const defaultApiHost =
  typeof window === 'undefined' ? 'localhost' : window.location.hostname || 'localhost'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || `http://${defaultApiHost}:8080`

export async function signup({ username, password, email }) {
  const response = await axios.post(`${API_BASE_URL}/api/auth/signup`, {
    username,
    password,
    email,
  })

  return response.data
}

export async function login({ username, password }) {
  const response = await axios.post(`${API_BASE_URL}/api/auth/login`, {
    username,
    password,
  })

  return response.data
}

export { API_BASE_URL }
