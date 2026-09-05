import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

import type { ProjectWorkload } from "../types";

export function ProjectWorkloadChart({ data }: { data: ProjectWorkload[] }) {
    if (data.length === 0) {
        return <p className="py-8 text-center text-sm text-gray-500">No data yet</p>;
    }

    return (
        <ResponsiveContainer width="100%" height={250}>
            <BarChart data={data}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="projectName" fontSize={12} />
                <YAxis allowDecimals={false} fontSize={12} />
                <Tooltip />
                <Bar dataKey="taskCount" fill="#9333ea" />
            </BarChart>
        </ResponsiveContainer>
    );
}
