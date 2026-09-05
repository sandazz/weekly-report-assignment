import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { Button } from "../../components/Button";
import { Card } from "../../components/Card";
import { ReportContentView } from "../../components/ReportContentView";
import { Spinner } from "../../components/Spinner";
import { useAsyncAction } from "../../hooks/useAsyncAction";
import { notifyError } from "../../lib/toast";
import * as reviewService from "../../services/reviewService";
import * as reportService from "../../services/reportService";
import type { ReportDetail } from "../../types";

export function ManagerReviewPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [report, setReport] = useState<ReportDetail | null>(null);
    const [comment, setComment] = useState("");
    const [commentError, setCommentError] = useState<string | null>(null);
    const [showCommentBox, setShowCommentBox] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        if (!id) return;
        setIsLoading(true);
        reportService
            .getReport(Number(id))
            .then(setReport)
            .catch((err) => notifyError(err, "Failed to load report"))
            .finally(() => setIsLoading(false));
    }, [id]);

    const { run: approve, isLoading: isApproving } = useAsyncAction(
        async () => {
            if (!id) return;
            await reviewService.reviewReport(Number(id), "APPROVED", comment);
            navigate("/manager/dashboard");
        },
        { successMessage: "Report approved.", errorFallback: "Failed to approve report" },
    );

    const { run: requestChanges, isLoading: isRequestingChanges } = useAsyncAction(
        async () => {
            if (!id) return;
            await reviewService.reviewReport(Number(id), "REQUESTED_CHANGES", comment);
            navigate("/manager/dashboard");
        },
        { successMessage: "Changes requested.", errorFallback: "Failed to request changes" },
    );

    const isSaving = isApproving || isRequestingChanges;

    function handleRequestChangesClick() {
        if (!showCommentBox) {
            setShowCommentBox(true);
            return;
        }
        if (!comment.trim()) {
            setCommentError("A comment is required to request changes.");
            return;
        }
        setCommentError(null);
        requestChanges();
    }

    if (isLoading) return <Spinner />;
    if (!report) return null;

    return (
        <div className="mx-auto flex max-w-4xl flex-col gap-4 p-6">
            <h1 className="text-2xl font-semibold text-gray-900">Review Report</h1>

            <ReportContentView report={report} />

            {report.status !== "SUBMITTED" ? (
                <Card>
                    <p className="text-sm text-gray-600">This report is not awaiting review.</p>
                </Card>
            ) : (
                <Card>
                    <h2 className="text-base font-semibold text-gray-900">Review Decision</h2>
                    {showCommentBox && (
                        <>
                            <textarea
                                className="mt-3 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
                                rows={3}
                                placeholder="Comment (required to request changes)"
                                value={comment}
                                onChange={(e) => {
                                    setComment(e.target.value);
                                    if (commentError) setCommentError(null);
                                }}
                            />
                            {commentError && <p className="mt-1 text-xs text-red-600">{commentError}</p>}
                        </>
                    )}
                    <div className="mt-3 flex flex-col gap-2 sm:flex-row">
                        <Button type="button" disabled={isSaving} onClick={() => approve()}>
                            {isApproving ? "Approving..." : "Approve"}
                        </Button>
                        <Button
                            type="button"
                            variant="secondary"
                            disabled={isSaving}
                            onClick={handleRequestChangesClick}
                        >
                            {isRequestingChanges ? "Requesting..." : "Request Changes"}
                        </Button>
                    </div>
                </Card>
            )}
        </div>
    );
}
