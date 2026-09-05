import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { Button } from "../../components/Button";
import { Card } from "../../components/Card";
import { ReportContentView } from "../../components/ReportContentView";
import { Spinner } from "../../components/Spinner";
import * as reviewService from "../../services/reviewService";
import * as reportService from "../../services/reportService";
import type { NormalizedError } from "../../services/apiClient";
import type { ReportDetail } from "../../types";

export function ManagerReviewPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [report, setReport] = useState<ReportDetail | null>(null);
    const [comment, setComment] = useState("");
    const [showCommentBox, setShowCommentBox] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);
    const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);

    useEffect(() => {
        if (!id) return;
        setIsLoading(true);
        reportService
            .getReport(Number(id))
            .then(setReport)
            .catch((err) => {
                const normalized = err as NormalizedError;
                setMessage({ type: "error", text: normalized.message ?? "Failed to load report" });
            })
            .finally(() => setIsLoading(false));
    }, [id]);

    async function handleApprove() {
        if (!id) return;
        setIsSaving(true);
        try {
            await reviewService.reviewReport(Number(id), "APPROVED", comment);
            navigate("/manager/dashboard");
        } catch (err) {
            const normalized = err as NormalizedError;
            setMessage({ type: "error", text: normalized.message ?? "Failed to approve report" });
        } finally {
            setIsSaving(false);
        }
    }

    async function handleRequestChanges() {
        if (!id) return;
        if (!comment.trim()) {
            setMessage({ type: "error", text: "A comment is required to request changes." });
            return;
        }
        setIsSaving(true);
        try {
            await reviewService.reviewReport(Number(id), "REQUESTED_CHANGES", comment);
            navigate("/manager/dashboard");
        } catch (err) {
            const normalized = err as NormalizedError;
            setMessage({ type: "error", text: normalized.message ?? "Failed to request changes" });
        } finally {
            setIsSaving(false);
        }
    }

    if (isLoading) return <Spinner />;
    if (!report) return null;

    return (
        <div className="mx-auto flex max-w-4xl flex-col gap-4 p-6">
            <h1 className="text-2xl font-semibold text-gray-900">Review Report</h1>

            {message && (
                <div
                    className={`rounded-md px-4 py-2 text-sm ${message.type === "success" ? "bg-green-50 text-green-700" : "bg-red-50 text-red-700"
                        }`}
                >
                    {message.text}
                </div>
            )}

            <ReportContentView report={report} />

            {report.status !== "SUBMITTED" ? (
                <Card>
                    <p className="text-sm text-gray-600">This report is not awaiting review.</p>
                </Card>
            ) : (
                <Card>
                    <h2 className="text-base font-semibold text-gray-900">Review Decision</h2>
                    {showCommentBox && (
                        <textarea
                            className="mt-3 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
                            rows={3}
                            placeholder="Comment (required to request changes)"
                            value={comment}
                            onChange={(e) => setComment(e.target.value)}
                        />
                    )}
                    <div className="mt-3 flex gap-2">
                        <Button type="button" disabled={isSaving} onClick={handleApprove}>
                            Approve
                        </Button>
                        <Button
                            type="button"
                            variant="secondary"
                            disabled={isSaving}
                            onClick={() => (showCommentBox ? handleRequestChanges() : setShowCommentBox(true))}
                        >
                            Request Changes
                        </Button>
                    </div>
                </Card>
            )}
        </div>
    );
}
