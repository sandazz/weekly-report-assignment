import type { TaskType } from "./report.types";

export interface DashboardSummary {
    totalReportsThisWeek: number;
    complianceRatePercent: number;
    submittedCount: number;
    pendingCount: number;
    lateCount: number;
    needsCorrectionCount: number;
    openBlockersCount: number;
}

export interface TaskTrendPoint {
    weekLabel: string;
    completedCount: number;
}

export type MemberReportStatus =
    | "DRAFT"
    | "SUBMITTED"
    | "NEEDS_CORRECTION"
    | "APPROVED"
    | "NOT_STARTED";

export interface MemberStatus {
    userId: number;
    userName: string;
    status: MemberReportStatus;
}

export interface ProjectWorkload {
    projectId: number;
    projectName: string;
    taskCount: number;
    totalPlannedHours: number;
}

export interface HoursByType {
    taskType: TaskType;
    totalHours: number;
}

export interface ActivityFeedItem {
    type: "REVIEW" | "SUBMISSION";
    description: string;
    actorName: string;
    timestamp: string;
    reportId: number;
}
