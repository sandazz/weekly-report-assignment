import { useAuth } from "./useAuth";
import type { Role } from "../types";

// Single reusable role-check used by both Sidebar (per-link) and RoleProtectedRoute.
export function useHasRole(allowedRoles: Role[]): boolean {
    const { user } = useAuth();
    if (!user) return false;
    return allowedRoles.includes(user.role);
}
