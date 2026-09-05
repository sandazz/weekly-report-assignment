import type { Role } from "./auth.types";

export interface UserSummary {
    id: number;
    name: string;
    email: string;
    roleName: Role;
    active: boolean;
    createdAt: string;
}
