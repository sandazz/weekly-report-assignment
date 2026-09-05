import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { ActivityFeed } from "../../components/ActivityFeed";
import { Card } from "../../components/Card";
import { HoursByTypeChart } from "../../components/HoursByTypeChart";
import { ProjectWorkloadChart } from "../../components/ProjectWorkloadChart";
import { Spinner } from "../../components/Spinner";
import { StatusBadge } from "../../components/StatusBadge";
import { SummaryCard } from "../../components/SummaryCard";
import { TasksTrendChart } from "../../components/TasksTrendChart";
import * as dashboardService from "../../services/dashboardService";
import { addDays, getCurrentWeekMonday } from "../../utils/week";
import type {
    ActivityFeedItem,
    DashboardSummary,
    HoursByType,
    MemberStatus,
    ProjectWorkload,
    TaskTrendPoint,
} from "../../types";

export function ManagerDashboardPage() {
    const navigate = useNavigate();
    const [weekStart, setWeekStart] = useState(getCurrentWeekMonday());

    const [summary, setSummary] = useState<DashboardSummary | null>(null);
    const [isSummaryLoading, setIsSummaryLoading] = useState(true);

    const [trend, setTrend] = useState<TaskTrendPoint[]>([]);
    const [isTrendLoading, setIsTrendLoading] = useState(true);

    const [memberStatus, setMemberStatus] = useState<MemberStatus[]>([]);
    const [isMemberStatusLoading, setIsMemberStatusLoading] = useState(true);

    const [workload, setWorkload] = useState<ProjectWorkload[]>([]);
    const [isWorkloadLoading, setIsWorkloadLoading] = useState(true);

    const [hoursByType, setHoursByType] = useState<HoursByType[]>([]);
    const [isHoursLoading, setIsHoursLoading] = useState(true);

    const [activity, setActivity] = useState<ActivityFeedItem[]>([]);
    const [isActivityLoading, setIsActivityLoading] = useState(true);

    // Trend + activity feed are multi-week/recent by nature — not affected by the week selector.
    useEffect(() => {
        dashboardService.getSummary().then(setSummary).finally(() => setIsSummaryLoading(false));
        dashboardService.getTaskTrend().then(setTrend).finally(() => setIsTrendLoading(false));
        dashboardService.getActivityFeed().then(setActivity).finally(() => setIsActivityLoading(false));
    }, []);

    useEffect(() => {
        setIsMemberStatusLoading(true);
        dashboardService
            .getMemberStatus(weekStart)
            .then(setMemberStatus)
            .finally(() => setIsMemberStatusLoading(false));

        const weekEnd = addDays(weekStart, 6);
        setIsWorkloadLoading(true);
        dashboardService
            .getWorkloadByProject(weekStart, weekEnd)
            .then(setWorkload)
            .finally(() => setIsWorkloadLoading(false));

        setIsHoursLoading(true);
        dashboardService
            .getHoursByType(weekStart, weekEnd)
            .then(setHoursByType)
            .finally(() => setIsHoursLoading(false));
    }, [weekStart]);

    return (
        <div className="mx-auto flex max-w-5xl flex-col gap-4 p-6">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-semibold text-gray-900">Manager Dashboard</h1>
                <div className="flex items-center gap-2">
                    <label className="text-sm font-medium text-gray-700">Week of</label>
                    <input
                        type="date"
                        value={weekStart}
                        onChange={(e) => setWeekStart(e.target.value)}
                        className="rounded-md border border-gray-300 px-3 py-2 text-sm"
                    />
                </div>
            </div>

            {isSummaryLoading ? (
                <Spinner />
            ) : (
                summary && (
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
                        <SummaryCard label="Total Reports This Week" value={summary.totalReportsThisWeek} />
                        <SummaryCard
                            label="Compliance Rate"
                            value={`${summary.complianceRatePercent.toFixed(0)}%`}
                            subtext={`${summary.submittedCount} submitted · ${summary.pendingCount} pending · ${summary.lateCount} late`}
                        />
                        <SummaryCard label="Needs Correction" value={summary.needsCorrectionCount} />
                        <SummaryCard label="Open Blockers" value={summary.openBlockersCount} />
                    </div>
                )
            )}

            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                <Card>
                    <h2 className="text-base font-semibold text-gray-900">Tasks Completed Trend</h2>
                    {isTrendLoading ? <Spinner /> : <TasksTrendChart data={trend} />}
                </Card>

                <Card>
                    <h2 className="text-base font-semibold text-gray-900">Submission Status by Team Member</h2>
                    {isMemberStatusLoading ? (
                        <Spinner />
                    ) : memberStatus.length === 0 ? (
                        <p className="py-8 text-center text-sm text-gray-500">No data yet</p>
                    ) : (
                        <ul className="mt-3 flex flex-col divide-y divide-gray-100">
                            {memberStatus.map((member) => (
                                <li key={member.userId}>
                                    <button
                                        type="button"
                                        onClick={() => navigate(`/manager/reports?userId=${member.userId}`)}
                                        className="flex w-full items-center justify-between py-2 text-left hover:bg-gray-50"
                                    >
                                        <span className="text-sm text-gray-900">{member.userName}</span>
                                        <StatusBadge status={member.status} />
                                    </button>
                                </li>
                            ))}
                        </ul>
                    )}
                </Card>

                <Card>
                    <h2 className="text-base font-semibold text-gray-900">Workload by Project</h2>
                    {isWorkloadLoading ? <Spinner /> : <ProjectWorkloadChart data={workload} />}
                </Card>

                <Card>
                    <h2 className="text-base font-semibold text-gray-900">Time Spent by Task Type</h2>
                    {isHoursLoading ? <Spinner /> : <HoursByTypeChart data={hoursByType} />}
                </Card>
            </div>

            <Card>
                <h2 className="text-base font-semibold text-gray-900">Recent Activity</h2>
                {isActivityLoading ? <Spinner /> : <ActivityFeed items={activity} />}
            </Card>
        </div>
    );
}
