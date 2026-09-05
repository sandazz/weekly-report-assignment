import { Navigate, Outlet } from "react-router-dom";

import { Spinner } from "../components/Spinner";
import { useAuth } from "../hooks/useAuth";

export function ProtectedRoute() {
    const { user, isLoading } = useAuth();

    if (isLoading) {
        return <Spinner />;
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    return <Outlet />;
}
