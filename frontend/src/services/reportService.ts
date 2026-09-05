import apiClient from "./apiClient";
import type { PageParams, PageResponse, ReportDetail, ReportFormValues, ReportStatus, ReportSummary } from "../types";

export interface GetMyReportsParams extends PageParams {
    status?: ReportStatus;
    projectId?: number;
}

export async function createReport(data: ReportFormValues): Promise<ReportDetail> {
    const response = await apiClient.post<ReportDetail>("/reports", data);
    return response.data;
}

export async function updateReport(id: number, data: ReportFormValues): Promise<ReportDetail> {
    const response = await apiClient.put<ReportDetail>(`/reports/${id}`, data);
    return response.data;
}

export async function getReport(id: number): Promise<ReportDetail> {
    const response = await apiClient.get<ReportDetail>(`/reports/${id}`);
    return response.data;
}

export async function getMyReports(params: GetMyReportsParams): Promise<PageResponse<ReportSummary>> {
    const response = await apiClient.get<PageResponse<ReportSummary>>("/reports/my", { params });
    return response.data;
}

export async function submitReport(id: number): Promise<ReportDetail> {
    const response = await apiClient.post<ReportDetail>(`/reports/${id}/submit`);
    return response.data;
}

export async function deleteReport(id: number): Promise<void> {
    await apiClient.delete(`/reports/${id}`);
}
