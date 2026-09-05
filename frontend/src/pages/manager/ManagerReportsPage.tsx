import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import { Card } from "../../components/Card";
import { FilterBar } from "../../components/FilterBar";
import { Pagination } from "../../components/Pagination";
import { Spinner } from "../../components/Spinner";
import { StatusBadge } from "../../components/StatusBadge";
import { useReportFilters } from "../../hooks/useReportFilters";
import { notifyError } from "../../lib/toast";
import * as reviewService from "../../services/reviewService";
import type { ReportSummary } from "../../types";

function SortHeader({
    label,
    field,
    sort,
    onSort,
}: {
    label: string;
    field: string;
    sort: string;
    onSort: (field: string) => void;
}) {
    const [currentField, currentDir] = sort.split(",");
    const isActive = currentField === field;
    return (
        <th className="pb-2">
            <button type="button" className="-mx-2 px-2 py-1 font-medium hover:underline" onClick={() => onSort(field)}>
                {label} {isActive && (currentDir === "asc" ? "▲" : "▼")}
            </button>
        </th>
    );
}

export function ManagerReportsPage() {
    const { filters, setFilters, page, setPage, sort, setSort, clearAll } = useReportFilters();

    const [reports, setReports] = useState<ReportSummary[]>([]);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        setIsLoading(true);
        reviewService
            .getManagerReports({
                page,
                size: 10,
                sort,
                status: filters.status || undefined,
                projectId: filters.projectId || undefined,
                userId: filters.memberId || undefined,
                fromDate: filters.fromDate || undefined,
                toDate: filters.toDate || undefined,
            })
            .then((response) => {
                setReports(response.content);
                setTotalPages(response.totalPages);
            })
            .catch((err) => notifyError(err, "Failed to load reports"))
            .finally(() => setIsLoading(false));
    }, [page, sort, filters.status, filters.projectId, filters.memberId, filters.fromDate, filters.toDate]);

    return (
        <div className="mx-auto flex max-w-5xl flex-col gap-4 p-6">
            <h1 className="text-2xl font-semibold text-gray-900">Manager Reports</h1>

            {filters.memberId && (
                <div className="text-sm text-gray-600">
                    Filtered by user #{filters.memberId} ·{" "}
                    <button
                        type="button"
                        className="-mx-2 px-2 py-1 text-purple-600 hover:underline"
                        onClick={() => setFilters({ memberId: "" })}
                    >
                        Clear
                    </button>
                </div>
            )}

            <FilterBar
                value={filters}
                onChange={setFilters}
                onClearAll={clearAll}
                showMemberFilter
            />

            <Card>
                {isLoading ? (
                    <Spinner />
                ) : reports.length === 0 ? (
                    <p className="py-8 text-center text-sm text-gray-500">No reports match these filters.</p>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm">
                            <thead className="text-gray-500">
                                <tr>
                                    <th className="pb-2">Member</th>
                                    <SortHeader label="Week range" field="weekStart" sort={sort} onSort={setSort} />
                                    <th className="pb-2">Project</th>
                                    <th className="pb-2">Status</th>
                                    <SortHeader label="Last updated" field="updatedAt" sort={sort} onSort={setSort} />
                                    <th className="pb-2"></th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-100">
                                {reports.map((report) => (
                                    <tr key={report.id}>
                                        <td className="py-2">{report.userName}</td>
                                        <td className="py-2">
                                            {report.weekStart} to {report.weekEnd}
                                        </td>
                                        <td className="py-2">{report.projectName}</td>
                                        <td className="py-2">
                                            <StatusBadge status={report.status} />
                                        </td>
                                        <td className="py-2">{new Date(report.updatedAt).toLocaleString()}</td>
                                        <td className="py-2">
                                            <Link
                                                to={
                                                    report.status === "SUBMITTED"
                                                        ? `/manager/reports/${report.id}/review`
                                                        : `/reports/${report.id}`
                                                }
                                                className="text-purple-600 hover:underline"
                                            >
                                                {report.status === "SUBMITTED" ? "Review" : "Open"}
                                            </Link>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
                <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
            </Card>
        </div>
    );
}
