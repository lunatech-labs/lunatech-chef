import { Navigate, Outlet } from "react-router-dom";

const ProtectedRoutes = (isAdmin) => {
  return (
    isAdmin ? <Outlet /> : <Navigate to="/" replace />
  )
}

export default ProtectedRoutes;
