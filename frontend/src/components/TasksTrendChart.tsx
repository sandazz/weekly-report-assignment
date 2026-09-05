import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

import type { TaskTrendPoint } from "../types";

export function TasksTrendChart({ data }: { data: TaskTrendPoint[] }) {
    if (data.length === 0 || data.every((point) => point.completedCount === 0)) {
        return <p className="py-8 text-center text-sm text-gray-500">No data yet</p>;
    }

    return (
        <ResponsiveContainer width="100%" height={250}>
            <LineChart data={data}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="weekLabel" fontSize={12} />
                <YAxis allowDecimals={false} fontSize={12} />
                <Tooltip />
                <Line type="monotone" dataKey="completedCount" stroke="#9333ea" strokeWidth={2} />
            </LineChart>
        </ResponsiveContainer>
    );
}
