import { Navigate, useLocation } from 'react-router-dom'
import { hasAuthenticatedSession } from '../services/session'

function ProtectedRoute({ children }) {
  const location = useLocation()

  if (!hasAuthenticatedSession()) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return children
}

export default ProtectedRoute
