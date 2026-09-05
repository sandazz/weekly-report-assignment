import apiClient from "./apiClient";
import type { BlockerFormRow, ReportBlocker } from "../types";

function toRequest(row: BlockerFormRow) {
    return { description: row.description, isKeyIssue: row.isKeyIssue };
}

export async function list(reportId: number): Promise<ReportBlocker[]> {
    const response = await apiClient.get<ReportBlocker[]>(`/reports/${reportId}/blockers`);
    return response.data;
}

export async function add(reportId: number, row: BlockerFormRow): Promise<ReportBlocker> {
    const response = await apiClient.post<ReportBlocker>(`/reports/${reportId}/blockers`, toRequest(row));
    return response.data;
}

export async function update(reportId: number, blockerId: number, row: BlockerFormRow): Promise<ReportBlocker> {
    const response = await apiClient.put<ReportBlocker>(`/reports/${reportId}/blockers/${blockerId}`, toRequest(row));
    return response.data;
}

export async function remove(reportId: number, blockerId: number): Promise<void> {
    await apiClient.delete(`/reports/${reportId}/blockers/${blockerId}`);
}
