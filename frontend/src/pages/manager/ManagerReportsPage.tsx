import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";

import { Card } from "../../components/Card";
import { Pagination } from "../../components/Pagination";
import { Spinner } from "../../components/Spinner";
import { StatusBadge } from "../../components/StatusBadge";
import * as reviewService from "../../services/reviewService";
import * as projectService from "../../services/projectService";
import type { Project, ReportStatus, ReportSummary } from "../../types";

const STATUS_OPTIONS: ReportStatus[] = ["DRAFT", "SUBMITTED", "NEEDS_CORRECTION", "APPROVED"];

export function ManagerReportsPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const userId = searchParams.get("userId") ? Number(searchParams.get("userId")) : undefined;

    const [reports, setReports] = useState<ReportSummary[]>([]);
    const [projects, setProjects] = useState<Project[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [statusFilter, setStatusFilter] = useState<ReportStatus | "">("");
    const [projectFilter, setProjectFilter] = useState<number | "">("");
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        projectService.getProjects(true).then(setProjects).catch(() => setProjects([]));
    }, []);

    useEffect(() => {
        setIsLoading(true);
        reviewService
            .getManagerReports({
                page,
                size: 10,
                status: statusFilter || undefined,
                projectId: projectFilter || undefined,
                userId,
            })
            .then((response) => {
                setReports(response.content);
                setTotalPages(response.totalPages);
            })
            .finally(() => setIsLoading(false));
    }, [page, statusFilter, projectFilter, userId]);

    return (
        <div className="mx-auto flex max-w-4xl flex-col gap-4 p-6">
            <h1 className="text-2xl font-semibold text-gray-900">Manager Reports</h1>

            {userId && (
                <div className="text-sm text-gray-600">
                    Filtered by user #{userId} ·{" "}
                    <button
                        type="button"
                        className="text-purple-600 hover:underline"
                        onClick={() => setSearchParams({})}
                    >
                        Clear
                    </button>
                </div>
            )}

            <div className="flex gap-3">
                <select
                    className="rounded-md border border-gray-300 px-3 py-2 text-sm"
                    value={statusFilter}
                    onChange={(e) => {
                        setStatusFilter(e.target.value as ReportStatus | "");
                        setPage(0);
                    }}
                >
                    <option value="">All statuses</option>
                    {STATUS_OPTIONS.map((s) => (
                        <option key={s} value={s}>
                            {s}
                        </option>
                    ))}
                </select>
                <select
                    className="rounded-md border border-gray-300 px-3 py-2 text-sm"
                    value={projectFilter}
                    onChange={(e) => {
                        setProjectFilter(e.target.value === "" ? "" : Number(e.target.value));
                        setPage(0);
                    }}
                >
                    <option value="">All projects</option>
                    {projects.map((project) => (
                        <option key={project.id} value={project.id}>
                            {project.name}
                        </option>
                    ))}
                </select>
            </div>

            <Card>
                {isLoading ? (
                    <Spinner />
                ) : reports.length === 0 ? (
                    <p className="py-8 text-center text-sm text-gray-500">No reports match these filters.</p>
                ) : (
                    <table className="w-full text-left text-sm">
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
                )}
                <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
            </Card>
        </div>
    );
}
