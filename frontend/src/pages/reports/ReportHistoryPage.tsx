import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import { Button } from "../../components/Button";
import { Card } from "../../components/Card";
import { FilterBar } from "../../components/FilterBar";
import { Pagination } from "../../components/Pagination";
import { Spinner } from "../../components/Spinner";
import { StatusBadge } from "../../components/StatusBadge";
import { useReportFilters } from "../../hooks/useReportFilters";
import * as reportService from "../../services/reportService";
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
            <button type="button" className="font-medium hover:underline" onClick={() => onSort(field)}>
                {label} {isActive && (currentDir === "asc" ? "▲" : "▼")}
            </button>
        </th>
    );
}

export function ReportHistoryPage() {
    const { filters, setFilters, page, setPage, sort, setSort, clearAll } = useReportFilters();

    const [reports, setReports] = useState<ReportSummary[]>([]);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        setIsLoading(true);
        reportService
            .getMyReports({
                page,
                size: 10,
                sort,
                status: filters.status || undefined,
                projectId: filters.projectId || undefined,
                fromDate: filters.fromDate || undefined,
                toDate: filters.toDate || undefined,
            })
            .then((response) => {
                setReports(response.content);
                setTotalPages(response.totalPages);
            })
            .finally(() => setIsLoading(false));
    }, [page, sort, filters.status, filters.projectId, filters.fromDate, filters.toDate]);

    return (
        <div className="mx-auto flex max-w-4xl flex-col gap-4 p-6">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-semibold text-gray-900">Report History</h1>
                <Link to="/reports/new">
                    <Button type="button">+ New Report</Button>
                </Link>
            </div>

            <FilterBar value={filters} onChange={setFilters} onClearAll={clearAll} />

            <Card>
                {isLoading ? (
                    <Spinner />
                ) : reports.length === 0 ? (
                    <div className="flex flex-col items-center gap-3 py-8">
                        <p className="text-gray-600">You haven't created any reports yet.</p>
                        <Link to="/reports/new">
                            <Button type="button">Create your first report</Button>
                        </Link>
                    </div>
                ) : (
                    <table className="w-full text-left text-sm">
                        <thead className="text-gray-500">
                            <tr>
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
                                    <td className="py-2">
                                        {report.weekStart} to {report.weekEnd}
                                    </td>
                                    <td className="py-2">{report.projectName}</td>
                                    <td className="py-2">
                                        <StatusBadge status={report.status} />
                                    </td>
                                    <td className="py-2">{new Date(report.updatedAt).toLocaleString()}</td>
                                    <td className="py-2">
                                        <Link to={`/reports/${report.id}`} className="text-purple-600 hover:underline">
                                            Open
                                        </Link>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
                <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
            </Card>
        </div>
    );
}
