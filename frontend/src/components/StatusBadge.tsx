import type { ReportStatus } from "../types";

const STYLES: Record<ReportStatus, string> = {
    DRAFT: "bg-gray-100 text-gray-700",
    SUBMITTED: "bg-blue-100 text-blue-700",
    NEEDS_CORRECTION: "bg-orange-100 text-orange-700",
    APPROVED: "bg-green-100 text-green-700",
};

const LABELS: Record<ReportStatus, string> = {
    DRAFT: "Draft",
    SUBMITTED: "Submitted",
    NEEDS_CORRECTION: "Needs Correction",
    APPROVED: "Approved",
};

export function StatusBadge({ status }: { status: ReportStatus }) {
    return (
        <span className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium ${STYLES[status]}`}>
            {LABELS[status]}
        </span>
    );
}
