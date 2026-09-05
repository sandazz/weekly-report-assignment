import apiClient from "./apiClient";
import type {
    ActivityFeedItem,
    DashboardSummary,
    HoursByType,
    MemberStatus,
    ProjectWorkload,
    TaskTrendPoint,
} from "../types";

export async function getSummary(): Promise<DashboardSummary> {
    const response = await apiClient.get<DashboardSummary>("/manager/dashboard/summary");
    return response.data;
}

export async function getTaskTrend(weeks = 8, userId?: number): Promise<TaskTrendPoint[]> {
    const response = await apiClient.get<TaskTrendPoint[]>("/manager/dashboard/task-trend", {
        params: { weeks, userId },
    });
    return response.data;
}

export async function getMemberStatus(weekStart?: string): Promise<MemberStatus[]> {
    const response = await apiClient.get<MemberStatus[]>("/manager/dashboard/member-status", {
        params: { weekStart },
    });
    return response.data;
}

export async function getWorkloadByProject(weekStart?: string, weekEnd?: string): Promise<ProjectWorkload[]> {
    const response = await apiClient.get<ProjectWorkload[]>("/manager/dashboard/workload-by-project", {
        params: { weekStart, weekEnd },
    });
    return response.data;
}

export async function getHoursByType(weekStart?: string, weekEnd?: string): Promise<HoursByType[]> {
    const response = await apiClient.get<HoursByType[]>("/manager/dashboard/hours-by-type", {
        params: { weekStart, weekEnd },
    });
    return response.data;
}

export async function getActivityFeed(limit = 10): Promise<ActivityFeedItem[]> {
    const response = await apiClient.get<ActivityFeedItem[]>("/manager/dashboard/activity-feed", {
        params: { limit },
    });
    return response.data;
}
