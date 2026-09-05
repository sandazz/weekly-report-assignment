import apiClient from "./apiClient";
import type { HourFormRow, ReportHour } from "../types";

function toRequest(row: HourFormRow) {
    return { taskType: row.taskType || null, hours: row.hours === "" ? 0 : row.hours };
}

export async function list(reportId: number): Promise<ReportHour[]> {
    const response = await apiClient.get<ReportHour[]>(`/reports/${reportId}/hours`);
    return response.data;
}

export async function add(reportId: number, row: HourFormRow): Promise<ReportHour> {
    const response = await apiClient.post<ReportHour>(`/reports/${reportId}/hours`, toRequest(row));
    return response.data;
}

export async function update(reportId: number, hourId: number, row: HourFormRow): Promise<ReportHour> {
    const response = await apiClient.put<ReportHour>(`/reports/${reportId}/hours/${hourId}`, toRequest(row));
    return response.data;
}

export async function remove(reportId: number, hourId: number): Promise<void> {
    await apiClient.delete(`/reports/${reportId}/hours/${hourId}`);
}
