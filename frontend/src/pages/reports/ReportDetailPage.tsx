import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

import { Button } from "../../components/Button";
import { Card } from "../../components/Card";
import { ReportContentView } from "../../components/ReportContentView";
import { Spinner } from "../../components/Spinner";
import { useAuth } from "../../hooks/useAuth";
import { notifyError } from "../../lib/toast";
import * as reportService from "../../services/reportService";
import * as reportVersionService from "../../services/reportVersionService";
import * as reviewService from "../../services/reviewService";
import type { NormalizedError } from "../../services/apiClient";
import type { ReportDetail, ReportVersionDetail, ReportVersionSummary, ReviewHistoryEntry } from "../../types";

export function ReportDetailPage() {
    const { id } = useParams();
    const { user } = useAuth();
    const [report, setReport] = useState<ReportDetail | null>(null);
    const [versions, setVersions] = useState<ReportVersionSummary[]>([]);
    const [selectedVersion, setSelectedVersion] = useState<ReportVersionDetail | null>(null);
    const [showVersions, setShowVersions] = useState(false);
    const [reviewHistory, setReviewHistory] = useState<ReviewHistoryEntry[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!id) return;
        const reportId = Number(id);
        setIsLoading(true);
        Promise.all([
            reportService.getReport(reportId),
            reportVersionService.getVersions(reportId),
            reviewService.getReviewHistory(reportId),
        ])
            .then(([reportData, versionData, historyData]) => {
                setReport(reportData);
                setVersions(versionData);
                setReviewHistory(historyData);
            })
            .catch((err) => {
                const normalized = err as NormalizedError;
                setError(normalized.message ?? "Failed to load report");
            })
            .finally(() => setIsLoading(false));
    }, [id]);

    async function loadVersion(versionId: number) {
        if (!id) return;
        try {
            const detail = await reportVersionService.getVersionDetail(Number(id), versionId);
            setSelectedVersion(detail);
        } catch (err) {
            notifyError(err, "Failed to load version detail");
        }
    }

    if (isLoading) return <Spinner />;
    if (error) return <div className="p-6 text-sm text-red-600">{error}</div>;
    if (!report || !user) return null;

    const isOwner = report.user.id === user.userId;
    const canEdit = isOwner && (report.status === "DRAFT" || report.status === "NEEDS_CORRECTION");
    const canReview = (user.role === "MANAGER" || user.role === "ADMIN") && report.status === "SUBMITTED";

    return (
        <div className="mx-auto flex max-w-4xl flex-col gap-4 p-6">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <h1 className="text-2xl font-semibold text-gray-900">Report Detail</h1>
                <div className="flex gap-2">
                    {canEdit && (
                        <Link to={`/reports/${report.id}/edit`}>
                            <Button type="button">Edit</Button>
                        </Link>
                    )}
                    {canReview && (
                        <Link to={`/manager/reports/${report.id}/review`}>
                            <Button type="button">Review</Button>
                        </Link>
                    )}
                </div>
            </div>

            <ReportContentView report={report} />

            <Card>
                <button
                    type="button"
                    className="text-base font-semibold text-gray-900"
                    onClick={() => setShowVersions((v) => !v)}
                >
                    Version History ({versions.length}) {showVersions ? "▲" : "▼"}
                </button>
                {showVersions && (
                    <div className="mt-3 flex flex-col gap-2">
                        {versions.length === 0 && <p className="text-sm text-gray-500">No versions yet.</p>}
                        {versions.map((version) => (
                            <div key={version.id}>
                                <button
                                    type="button"
                                    className="text-sm text-purple-600 hover:underline"
                                    onClick={() => loadVersion(version.id)}
                                >
                                    Version {version.versionNumber} — submitted {new Date(version.submittedAt).toLocaleString()}
                                </button>
                                {selectedVersion && selectedVersion.id === version.id && (
                                    <div className="mt-2 rounded-md border border-gray-200 p-3 text-sm">
                                        <p className="text-gray-700">
                                            <strong>Next week's plan:</strong> {selectedVersion.nextWeekPlan || "-"}
                                        </p>
                                        <p className="mt-1 text-gray-700">
                                            <strong>Notes:</strong> {selectedVersion.notes || "-"}
                                        </p>
                                        <div className="mt-2 overflow-x-auto">
                                            <table className="w-full text-left text-sm">
                                                <thead className="text-gray-500">
                                                    <tr>
                                                        <th className="pb-1">Task</th>
                                                        <th className="pb-1">Planned %</th>
                                                        <th className="pb-1">Actual %</th>
                                                        <th className="pb-1">Status</th>
                                                    </tr>
                                                </thead>
                                                <tbody className="divide-y divide-gray-100">
                                                    {selectedVersion.tasks.map((task) => (
                                                        <tr key={task.id}>
                                                            <td className="py-1">{task.taskName}</td>
                                                            <td className="py-1">{task.plannedPercentage ?? "-"}</td>
                                                            <td className="py-1">{task.actualPercentage ?? "-"}</td>
                                                            <td className="py-1">{task.status ?? "-"}</td>
                                                        </tr>
                                                    ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                )}
            </Card>

            <Card>
                <h2 className="text-base font-semibold text-gray-900">Review History</h2>
                {reviewHistory.length === 0 ? (
                    <p className="mt-2 text-sm text-gray-500">No review history yet.</p>
                ) : (
                    <ul className="mt-3 flex flex-col gap-3 border-l-2 border-gray-200 pl-4">
                        {reviewHistory.map((entry) => (
                            <li key={entry.id} className="text-sm">
                                <p className="font-medium text-gray-900">
                                    {entry.reviewerName} — {entry.action === "APPROVED" ? "Approved" : "Requested Changes"}{" "}
                                    <span className="font-normal text-gray-500">
                                        (v{entry.versionNumber}, {new Date(entry.createdAt).toLocaleString()})
                                    </span>
                                </p>
                                {entry.comment && <p className="text-gray-600">{entry.comment}</p>}
                            </li>
                        ))}
                    </ul>
                )}
            </Card>
        </div>
    );
}
