import apiClient from "./apiClient";
import type { AchievementFormRow, ReportAchievement } from "../types";

function toRequest(row: AchievementFormRow) {
    return { description: row.description, isKeyAchievement: row.isKeyAchievement };
}

export async function list(reportId: number): Promise<ReportAchievement[]> {
    const response = await apiClient.get<ReportAchievement[]>(`/reports/${reportId}/achievements`);
    return response.data;
}

export async function add(reportId: number, row: AchievementFormRow): Promise<ReportAchievement> {
    const response = await apiClient.post<ReportAchievement>(`/reports/${reportId}/achievements`, toRequest(row));
    return response.data;
}

export async function update(
    reportId: number,
    achievementId: number,
    row: AchievementFormRow,
): Promise<ReportAchievement> {
    const response = await apiClient.put<ReportAchievement>(
        `/reports/${reportId}/achievements/${achievementId}`,
        toRequest(row),
    );
    return response.data;
}

export async function remove(reportId: number, achievementId: number): Promise<void> {
    await apiClient.delete(`/reports/${reportId}/achievements/${achievementId}`);
}
