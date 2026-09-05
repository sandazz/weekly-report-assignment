import { Card } from "./Card";

interface Props {
    label: string;
    value: string | number;
    subtext?: string;
}

export function SummaryCard({ label, value, subtext }: Props) {
    return (
        <Card className="flex flex-col gap-1">
            <span className="text-3xl font-semibold text-gray-900">{value}</span>
            <span className="text-sm font-medium text-gray-600">{label}</span>
            {subtext && <span className="text-xs text-gray-500">{subtext}</span>}
        </Card>
    );
}
