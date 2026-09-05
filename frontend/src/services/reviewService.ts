import apiClient from "./apiClient";
import type { PageParams, PageResponse, ReportDetail, ReportStatus, ReportSummary, ReviewAction, ReviewHistoryEntry } from "../types";

export interface GetManagerReportsParams extends PageParams {
    status?: ReportStatus;
    projectId?: number;
    userId?: number;
}

export async function reviewReport(reportId: number, action: ReviewAction, comment: string): Promise<ReportDetail> {
    const response = await apiClient.post<ReportDetail>(`/reports/${reportId}/review`, {
        action,
        comment: comment || null,
    });
    return response.data;
}

export async function getReviewHistory(reportId: number): Promise<ReviewHistoryEntry[]> {
    const response = await apiClient.get<ReviewHistoryEntry[]>(`/reports/${reportId}/review-history`);
    return response.data;
}

export async function getManagerReports(params: GetManagerReportsParams): Promise<PageResponse<ReportSummary>> {
    const response = await apiClient.get<PageResponse<ReportSummary>>("/manager/reports", { params });
    return response.data;
}
