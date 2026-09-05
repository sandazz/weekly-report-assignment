export type Role = "TEAM_MEMBER" | "MANAGER" | "ADMIN";

export interface AuthUser {
    userId: number;
    name: string;
    email: string;
    role: Role;
}

export interface AuthResponse {
    token: string;
    tokenType: string;
    userId: number;
    name: string;
    email: string;
    role: Role;
    expiresAt: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    name: string;
    email: string;
    password: string;
}
