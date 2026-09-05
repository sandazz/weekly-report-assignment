import { createContext, useEffect, useState, type ReactNode } from "react";

import { login as loginRequest, register as registerRequest } from "../services/authService";
import { registerLogout } from "../utils/authBridge";
import { clearSession, getStoredToken, getStoredUser, storeSession } from "../utils/authStorage";
import type { AuthUser } from "../types";

export interface AuthContextValue {
    user: AuthUser | null;
    token: string | null;
    isLoading: boolean;
    login: (email: string, password: string) => Promise<void>;
    register: (name: string, email: string, password: string) => Promise<void>;
    logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<AuthUser | null>(null);
    const [token, setToken] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        // Trust whatever is in localStorage on load; a real request will 401 if it's stale.
        setToken(getStoredToken());
        setUser(getStoredUser());
        setIsLoading(false);
    }, []);

    function logout() {
        clearSession();
        setToken(null);
        setUser(null);
    }

    useEffect(() => {
        registerLogout(logout);
    }, []);

    async function login(email: string, password: string) {
        const response = await loginRequest({ email, password });
        const authUser: AuthUser = {
            userId: response.userId,
            name: response.name,
            email: response.email,
            role: response.role,
        };
        storeSession(response.token, authUser);
        setToken(response.token);
        setUser(authUser);
    }

    async function register(name: string, email: string, password: string) {
        const response = await registerRequest({ name, email, password });
        const authUser: AuthUser = {
            userId: response.userId,
            name: response.name,
            email: response.email,
            role: response.role,
        };
        storeSession(response.token, authUser);
        setToken(response.token);
        setUser(authUser);
    }

    return (
        <AuthContext.Provider value={{ user, token, isLoading, login, register, logout }}>
            {children}
        </AuthContext.Provider>
    );
}
