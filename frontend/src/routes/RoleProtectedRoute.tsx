import { Navigate, Outlet } from "react-router-dom";

import { Spinner } from "../components/Spinner";
import { useAuth } from "../hooks/useAuth";
import { useHasRole } from "../hooks/useHasRole";
import type { Role } from "../types";

export function RoleProtectedRoute({ allowedRoles }: { allowedRoles: Role[] }) {
    const { user, isLoading } = useAuth();
    const hasRole = useHasRole(allowedRoles);

    if (isLoading) {
        return <Spinner />;
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    if (!hasRole) {
        return <Navigate to="/forbidden" replace />;
    }

    return <Outlet />;
}
