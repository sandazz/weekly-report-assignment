import { useNavigate } from "react-router-dom";

import { formatRelativeTime } from "../utils/relativeTime";
import type { ActivityFeedItem } from "../types";

export function ActivityFeed({ items }: { items: ActivityFeedItem[] }) {
    const navigate = useNavigate();

    if (items.length === 0) {
        return <p className="py-8 text-center text-sm text-gray-500">No recent activity yet</p>;
    }

    return (
        <ul className="flex flex-col divide-y divide-gray-100">
            {items.map((item, index) => (
                <li key={`${item.type}-${item.timestamp}-${index}`}>
                    <button
                        type="button"
                        onClick={() => navigate(`/reports/${item.reportId}`)}
                        className="w-full py-2 text-left hover:bg-gray-50"
                    >
                        <p className="text-sm text-gray-900">{item.description}</p>
                        <p className="text-xs text-gray-500">{formatRelativeTime(item.timestamp)}</p>
                    </button>
                </li>
            ))}
        </ul>
    );
}
