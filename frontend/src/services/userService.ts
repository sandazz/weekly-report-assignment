import apiClient from "./apiClient";
import type { PageParams, PageResponse, Role, UserSummary } from "../types";

export interface GetUsersParams extends PageParams {
    roleName?: Role;
}

export async function getUsers(params: GetUsersParams): Promise<PageResponse<UserSummary>> {
    const response = await apiClient.get<PageResponse<UserSummary>>("/admin/users", { params });
    return response.data;
}

export async function updateUserRole(id: number, roleName: Role): Promise<void> {
    await apiClient.patch(`/admin/users/${id}/role`, { roleName });
}

export async function updateUserActive(id: number, active: boolean): Promise<void> {
    await apiClient.patch(`/admin/users/${id}/active`, { active });
}
