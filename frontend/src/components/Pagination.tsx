import { Button } from "./Button";

interface Props {
    page: number;
    totalPages: number;
    onPageChange: (page: number) => void;
}

export function Pagination({ page, totalPages, onPageChange }: Props) {
    if (totalPages <= 1) return null;

    return (
        <div className="flex items-center justify-between pt-4">
            <Button
                type="button"
                variant="secondary"
                disabled={page <= 0}
                onClick={() => onPageChange(page - 1)}
            >
                Previous
            </Button>
            <span className="text-sm text-gray-600">
                Page {page + 1} of {totalPages}
            </span>
            <Button
                type="button"
                variant="secondary"
                disabled={page >= totalPages - 1}
                onClick={() => onPageChange(page + 1)}
            >
                Next
            </Button>
        </div>
    );
}
