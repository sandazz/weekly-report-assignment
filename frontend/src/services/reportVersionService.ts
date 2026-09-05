import apiClient from "./apiClient";
import type { ReportVersionDetail, ReportVersionSummary } from "../types";

export async function getVersions(reportId: number): Promise<ReportVersionSummary[]> {
    const response = await apiClient.get<ReportVersionSummary[]>(`/reports/${reportId}/versions`);
    return response.data;
}

export async function getVersionDetail(reportId: number, versionId: number): Promise<ReportVersionDetail> {
    const response = await apiClient.get<ReportVersionDetail>(`/reports/${reportId}/versions/${versionId}`);
    return response.data;
}
