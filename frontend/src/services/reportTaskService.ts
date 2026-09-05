import apiClient from "./apiClient";
import type { ReportTask, TaskFormRow } from "../types";

function toRequest(row: TaskFormRow) {
    return {
        taskName: row.taskName,
        priority: row.priority || null,
        plannedPercentage: row.plannedPercentage === "" ? null : row.plannedPercentage,
        actualPercentage: row.actualPercentage === "" ? null : row.actualPercentage,
        status: row.status || null,
        plannedHours: row.plannedHours === "" ? null : row.plannedHours,
        spentHours: row.spentHours === "" ? null : row.spentHours,
        deliverable: row.deliverable || null,
    };
}

export async function list(reportId: number): Promise<ReportTask[]> {
    const response = await apiClient.get<ReportTask[]>(`/reports/${reportId}/tasks`);
    return response.data;
}

export async function add(reportId: number, row: TaskFormRow): Promise<ReportTask> {
    const response = await apiClient.post<ReportTask>(`/reports/${reportId}/tasks`, toRequest(row));
    return response.data;
}

export async function update(reportId: number, taskId: number, row: TaskFormRow): Promise<ReportTask> {
    const response = await apiClient.put<ReportTask>(`/reports/${reportId}/tasks/${taskId}`, toRequest(row));
    return response.data;
}

export async function remove(reportId: number, taskId: number): Promise<void> {
    await apiClient.delete(`/reports/${reportId}/tasks/${taskId}`);
}
