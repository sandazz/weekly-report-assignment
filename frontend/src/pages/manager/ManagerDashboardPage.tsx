import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import { Card } from "../../components/Card";
import { Pagination } from "../../components/Pagination";
import { Spinner } from "../../components/Spinner";
import { StatusBadge } from "../../components/StatusBadge";
import * as reviewService from "../../services/reviewService";
import type { ReportSummary } from "../../types";

// Minimal submitted-reports queue for this phase; richer dashboard (stats/charts) comes in Phase 8.
export function ManagerDashboardPage() {
    const [reports, setReports] = useState<ReportSummary[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        setIsLoading(true);
        reviewService
            .getManagerReports({ page, size: 10, status: "SUBMITTED" })
            .then((response) => {
                setReports(response.content);
                setTotalPages(response.totalPages);
            })
            .finally(() => setIsLoading(false));
    }, [page]);

    return (
        <div className="mx-auto flex max-w-4xl flex-col gap-4 p-6">
            <h1 className="text-2xl font-semibold text-gray-900">Manager Dashboard</h1>
            <Card>
                <h2 className="text-base font-semibold text-gray-900">Reports awaiting review</h2>
                {isLoading ? (
                    <Spinner />
                ) : reports.length === 0 ? (
                    <p className="mt-2 text-sm text-gray-500">No reports are currently awaiting review.</p>
                ) : (
                    <table className="mt-3 w-full text-left text-sm">
                        <thead className="text-gray-500">
                            <tr>
                                <th className="pb-2">Week</th>
                                <th className="pb-2">Project</th>
                                <th className="pb-2">Status</th>
                                <th className="pb-2">Last updated</th>
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
                                        <Link
                                            to={`/manager/reports/${report.id}/review`}
                                            className="text-purple-600 hover:underline"
                                        >
                                            Review
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
