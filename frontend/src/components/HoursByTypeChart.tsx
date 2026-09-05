import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";

import type { HoursByType } from "../types";

const COLORS = ["#9333ea", "#3b82f6", "#f97316", "#10b981", "#ef4444"];

export function HoursByTypeChart({ data }: { data: HoursByType[] }) {
    if (data.length === 0) {
        return <p className="py-8 text-center text-sm text-gray-500">No data yet</p>;
    }

    return (
        <ResponsiveContainer width="100%" height={250}>
            <PieChart>
                <Pie data={data} dataKey="totalHours" nameKey="taskType" outerRadius={80} label>
                    {data.map((entry, index) => (
                        <Cell key={entry.taskType} fill={COLORS[index % COLORS.length]} />
                    ))}
                </Pie>
                <Tooltip />
                <Legend />
            </PieChart>
        </ResponsiveContainer>
    );
}
