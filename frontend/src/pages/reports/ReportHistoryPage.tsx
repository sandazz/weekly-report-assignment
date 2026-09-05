import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import { Button } from "../../components/Button";
import { Card } from "../../components/Card";
import { Pagination } from "../../components/Pagination";
import { Spinner } from "../../components/Spinner";
import { StatusBadge } from "../../components/StatusBadge";
import * as reportService from "../../services/reportService";
import * as projectService from "../../services/projectService";
import type { Project, ReportStatus, ReportSummary } from "../../types";

const STATUS_OPTIONS: ReportStatus[] = ["DRAFT", "SUBMITTED", "NEEDS_CORRECTION", "APPROVED"];

export function ReportHistoryPage() {
    const [reports, setReports] = useState<ReportSummary[]>([]);
    const [projects, setProjects] = useState<Project[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [statusFilter, setStatusFilter] = useState<ReportStatus | "">("");
    const [projectFilter, setProjectFilter] = useState<number | "">("");
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        projectService.getProjects().then(setProjects).catch(() => setProjects([]));
    }, []);

    useEffect(() => {
        setIsLoading(true);
        reportService
            .getMyReports({
                page,
                size: 10,
                status: statusFilter || undefined,
                projectId: projectFilter || undefined,
            })
            .then((response) => {
                setReports(response.content);
                setTotalPages(response.totalPages);
            })
            .finally(() => setIsLoading(false));
    }, [page, statusFilter, projectFilter]);

    return (
        <div className="mx-auto flex max-w-4xl flex-col gap-4 p-6">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-semibold text-gray-900">Report History</h1>
                <Link to="/reports/new">
                    <Button type="button">+ New Report</Button>
                </Link>
            </div>

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
