import apiClient from "./apiClient";
import type { Project, ProjectFormValues } from "../types";

export async function getProjects(includeInactive = false): Promise<Project[]> {
    const response = await apiClient.get<Project[]>("/projects", { params: { includeInactive } });
    return response.data;
}

export async function getProject(id: number): Promise<Project> {
    const response = await apiClient.get<Project>(`/projects/${id}`);
    return response.data;
}

export async function createProject(data: ProjectFormValues): Promise<Project> {
    const response = await apiClient.post<Project>("/projects", data);
    return response.data;
}

export async function updateProject(id: number, data: ProjectFormValues): Promise<Project> {
    const response = await apiClient.put<Project>(`/projects/${id}`, data);
    return response.data;
}

export async function deleteProject(id: number): Promise<void> {
    await apiClient.delete(`/projects/${id}`);
}
