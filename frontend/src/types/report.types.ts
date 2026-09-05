export type ReportStatus = "DRAFT" | "SUBMITTED" | "NEEDS_CORRECTION" | "APPROVED";
export type TaskPriority = "LOW" | "MEDIUM" | "HIGH";
export type TaskStatus = "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED" | "BLOCKED";
export type TaskType = "DEVELOPMENT" | "TESTING" | "MEETINGS" | "DOCUMENTATION" | "OTHER";
export type ReviewAction = "REQUESTED_CHANGES" | "APPROVED";

export interface ReportSummary {
    id: number;
    weekStart: string;
    weekEnd: string;
    projectName: string;
    userName: string;
    status: ReportStatus;
    updatedAt: string;
}

export interface ReportTask {
    id: number;
    taskName: string;
    priority: TaskPriority | null;
    plannedPercentage: number | null;
    actualPercentage: number | null;
    status: TaskStatus | null;
    plannedHours: number | null;
    spentHours: number | null;
    deliverable: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface ReportBlocker {
    id: number;
    description: string;
    isKeyIssue: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface ReportAchievement {
    id: number;
    description: string;
    isKeyAchievement: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface ReportHour {
    id: number;
    taskType: TaskType | null;
    hours: number;
    createdAt: string;
    updatedAt: string;
}

export interface ReportDetail {
    id: number;
    user: { id: number; name: string; email: string };
    project: { id: number; name: string; description: string | null; active: boolean };
    weekStart: string;
    weekEnd: string;
    status: ReportStatus;
    nextWeekPlan: string | null;
    keyBlocker: string | null;
    keyAchievement: string | null;
    note: string | null;
    tasks: ReportTask[];
    blockers: ReportBlocker[];
    achievements: ReportAchievement[];
    hours: ReportHour[];
    createdAt: string;
    updatedAt: string;
}

export interface ReportVersionSummary {
    id: number;
    versionNumber: number;
    submittedAt: string;
}

export interface ReportVersionTask {
    id: number;
    taskName: string;
    plannedPercentage: number | null;
    actualPercentage: number | null;
    status: TaskStatus | null;
    plannedHours: number | null;
    spentHours: number | null;
    deliverable: string | null;
}

export interface ReportVersionDetail {
    id: number;
    versionNumber: number;
    nextWeekPlan: string | null;
    keyBlocker: string | null;
    keyAchievement: string | null;
    notes: string | null;
    submittedAt: string;
    createdAt: string;
    tasks: ReportVersionTask[];
}

export interface ReviewHistoryEntry {
    id: number;
    reviewerName: string;
    action: ReviewAction;
    comment: string | null;
    versionNumber: number;
    createdAt: string;
}

// Request shapes

export interface ReportFormValues {
    projectId: number;
    weekStart: string;
    weekEnd: string;
    nextWeekPlan: string;
    keyBlocker: string;
    keyAchievement: string;
    note: string;
}

export interface TaskFormRow {
    id?: number;
    taskName: string;
    priority: TaskPriority | "";
    plannedPercentage: number | "";
    actualPercentage: number | "";
    status: TaskStatus | "";
    plannedHours: number | "";
    spentHours: number | "";
    deliverable: string;
}

export interface BlockerFormRow {
    id?: number;
    description: string;
    isKeyIssue: boolean;
}

export interface AchievementFormRow {
    id?: number;
    description: string;
    isKeyAchievement: boolean;
}

export interface HourFormRow {
    id?: number;
    taskType: TaskType | "";
    hours: number | "";
}
