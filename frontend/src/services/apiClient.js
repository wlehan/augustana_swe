import axios from 'axios'
import { getAuthToken } from './session'

const defaultApiHost =
  typeof window === 'undefined' ? 'localhost' : window.location.hostname || 'localhost'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || `http://${defaultApiHost}:8080`

/**
 * Shared Axios client that points at the backend and attaches the current JWT.
 */
const apiClient = axios.create({
  baseURL: API_BASE_URL,
})

apiClient.interceptors.request.use((config) => {
  const token = getAuthToken()

  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

export default apiClient
