import { useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";

import { Button } from "../components/Button";
import { Card } from "../components/Card";
import { Spinner } from "../components/Spinner";
import { StatusBadge } from "../components/StatusBadge";
import { useAuth } from "../hooks/useAuth";
import { notifyError } from "../lib/toast";
import * as reportService from "../services/reportService";
import type { ReportStatus, ReportSummary } from "../types";

const STATUSES: ReportStatus[] = ["DRAFT", "SUBMITTED", "NEEDS_CORRECTION", "APPROVED"];

// Monday of the current week, as a YYYY-MM-DD string matching the backend's weekStart filter.
function currentWeekMonday(): string {
    const now = new Date();
    const day = now.getDay();
    const diff = day === 0 ? -6 : 1 - day;
    const monday = new Date(now);
    monday.setDate(now.getDate() + diff);
    return monday.toISOString().slice(0, 10);
}

export function DashboardPage() {
    const { user } = useAuth();
    const isManagerOrAdmin = user?.role === "MANAGER" || user?.role === "ADMIN";

    const [isLoading, setIsLoading] = useState(true);
    const [currentWeekReport, setCurrentWeekReport] = useState<ReportSummary | null>(null);
    const [statusCounts, setStatusCounts] = useState<Record<ReportStatus, number> | null>(null);
    const [recentReports, setRecentReports] = useState<ReportSummary[]>([]);

    useEffect(() => {
        if (isManagerOrAdmin) return;

        const weekStart = currentWeekMonday();
        setIsLoading(true);
        Promise.all([
            reportService.getMyReports({ page: 0, size: 1, fromDate: weekStart, toDate: weekStart }),
            Promise.all(STATUSES.map((status) => reportService.getMyReports({ page: 0, size: 1, status }))),
            reportService.getMyReports({ page: 0, size: 5, sort: "updatedAt,desc" }),
        ])
            .then(([currentWeek, byStatus, recent]) => {
                setCurrentWeekReport(currentWeek.content[0] ?? null);
                setStatusCounts(
                    STATUSES.reduce(
                        (acc, status, index) => ({ ...acc, [status]: byStatus[index].totalElements }),
                        {} as Record<ReportStatus, number>,
                    ),
                );
                setRecentReports(recent.content);
            })
            .catch((err) => notifyError(err, "Failed to load dashboard"))
            .finally(() => setIsLoading(false));
    }, [isManagerOrAdmin]);

    if (isManagerOrAdmin) return <Navigate to="/manager/dashboard" replace />;
    if (isLoading) return <Spinner />;

    return (
        <div className="mx-auto flex max-w-4xl flex-col gap-4 p-6">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <h1 className="text-2xl font-semibold text-gray-900">Dashboard</h1>
                <Link to="/reports/new">
                    <Button type="button">+ New Report</Button>
                </Link>
            </div>

            <Card>
                <h2 className="text-base font-semibold text-gray-900">This week</h2>
                {currentWeekReport ? (
                    <div className="mt-3 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                        <div>
                            <p className="text-sm text-gray-700">
                                {currentWeekReport.weekStart} to {currentWeekReport.weekEnd} ·{" "}
                                {currentWeekReport.projectName}
                            </p>
                            <div className="mt-1">
                                <StatusBadge status={currentWeekReport.status} />
                            </div>
                        </div>
                        <Link to={`/reports/${currentWeekReport.id}`} className="text-purple-600 hover:underline">
                            Open
                        </Link>
                    </div>
                ) : (
                    <div className="mt-3 flex flex-col items-center gap-3 py-4">
                        <p className="text-gray-600">You haven't started this week's report yet.</p>
                        <Link to="/reports/new">
                            <Button type="button">Start this week's report</Button>
                        </Link>
                    </div>
                )}
            </Card>

            <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
                {STATUSES.map((status) => (
                    <Card key={status}>
                        <p className="text-2xl font-semibold text-gray-900">{statusCounts?.[status] ?? 0}</p>
                        <div className="mt-1">
                            <StatusBadge status={status} />
                        </div>
                    </Card>
                ))}
            </div>

            <Card>
                <div className="flex items-center justify-between">
                    <h2 className="text-base font-semibold text-gray-900">Recent reports</h2>
                    <Link to="/reports/history" className="text-sm text-purple-600 hover:underline">
                        View all
                    </Link>
                </div>
                {recentReports.length === 0 ? (
                    <p className="mt-3 text-sm text-gray-500">You haven't created any reports yet.</p>
                ) : (
                    <ul className="mt-3 flex flex-col divide-y divide-gray-100">
                        {recentReports.map((report) => (
                            <li
                                key={report.id}
                                className="flex flex-col gap-1 py-2 text-sm sm:flex-row sm:items-center sm:justify-between"
                            >
                                <span>
                                    {report.weekStart} to {report.weekEnd} · {report.projectName}
                                </span>
                                <div className="flex items-center gap-3">
                                    <StatusBadge status={report.status} />
                                    <Link to={`/reports/${report.id}`} className="text-purple-600 hover:underline">
                                        Open
                                    </Link>
                                </div>
                            </li>
                        ))}
                    </ul>
                )}
            </Card>
        </div>
    );
}
