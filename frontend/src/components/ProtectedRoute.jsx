import { Navigate, useLocation } from 'react-router-dom'
import { hasAuthenticatedSession } from '../services/session'

/**
 * Redirects unauthenticated users away from routes that require a saved token.
 */
function ProtectedRoute({ children }) {
  const location = useLocation()

  if (!hasAuthenticatedSession()) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return children
}

export default ProtectedRoute
