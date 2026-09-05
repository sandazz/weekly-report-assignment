import { Card } from "./Card";
import type { ReportDetail } from "../types";

export function ReportContentView({ report }: { report: ReportDetail }) {
    return (
        <div className="flex flex-col gap-4">
            <Card>
                <h2 className="text-lg font-semibold text-gray-900">Report Details</h2>
                <dl className="mt-3 grid grid-cols-2 gap-3 text-sm">
                    <div>
                        <dt className="text-gray-500">Team member</dt>
                        <dd className="text-gray-900">{report.user.name}</dd>
                    </div>
                    <div>
                        <dt className="text-gray-500">Project</dt>
                        <dd className="text-gray-900">{report.project.name}</dd>
                    </div>
                    <div>
                        <dt className="text-gray-500">Week</dt>
                        <dd className="text-gray-900">
                            {report.weekStart} to {report.weekEnd}
                        </dd>
                    </div>
                    <div>
                        <dt className="text-gray-500">Last updated</dt>
                        <dd className="text-gray-900">{new Date(report.updatedAt).toLocaleString()}</dd>
                    </div>
                </dl>
            </Card>

            <Card>
                <h3 className="text-base font-semibold text-gray-900">Tasks</h3>
                {report.tasks.length === 0 ? (
                    <p className="mt-2 text-sm text-gray-500">No tasks recorded.</p>
                ) : (
                    <table className="mt-3 w-full text-left text-sm">
                        <thead className="text-gray-500">
                            <tr>
                                <th className="pb-2">Task</th>
                                <th className="pb-2">Priority</th>
                                <th className="pb-2">Planned %</th>
                                <th className="pb-2">Actual %</th>
                                <th className="pb-2">Status</th>
                                <th className="pb-2">Planned hrs</th>
                                <th className="pb-2">Spent hrs</th>
                                <th className="pb-2">Deliverable</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100">
                            {report.tasks.map((task) => (
                                <tr key={task.id}>
                                    <td className="py-2">{task.taskName}</td>
                                    <td className="py-2">{task.priority ?? "-"}</td>
                                    <td className="py-2">{task.plannedPercentage ?? "-"}</td>
                                    <td className="py-2">{task.actualPercentage ?? "-"}</td>
                                    <td className="py-2">{task.status ?? "-"}</td>
                                    <td className="py-2">{task.plannedHours ?? "-"}</td>
                                    <td className="py-2">{task.spentHours ?? "-"}</td>
                                    <td className="py-2">{task.deliverable ?? "-"}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </Card>

            <Card>
                <h3 className="text-base font-semibold text-gray-900">Next Week's Plan</h3>
                <p className="mt-2 whitespace-pre-wrap text-sm text-gray-700">{report.nextWeekPlan || "-"}</p>
            </Card>

            <Card>
                <h3 className="text-base font-semibold text-gray-900">Blockers</h3>
                {report.blockers.length === 0 ? (
                    <p className="mt-2 text-sm text-gray-500">No blockers recorded.</p>
                ) : (
                    <ul className="mt-2 flex flex-col gap-1 text-sm">
                        {report.blockers.map((blocker) => (
                            <li
                                key={blocker.id}
                                className={blocker.isKeyIssue ? "font-medium text-red-700" : "text-gray-700"}
                            >
                                {blocker.isKeyIssue && "★ "}
                                {blocker.description}
                            </li>
                        ))}
                    </ul>
                )}
            </Card>

            <Card>
                <h3 className="text-base font-semibold text-gray-900">Achievements</h3>
                {report.achievements.length === 0 ? (
                    <p className="mt-2 text-sm text-gray-500">No achievements recorded.</p>
                ) : (
                    <ul className="mt-2 flex flex-col gap-1 text-sm">
                        {report.achievements.map((achievement) => (
                            <li
                                key={achievement.id}
                                className={
                                    achievement.isKeyAchievement ? "font-medium text-green-700" : "text-gray-700"
                                }
                            >
                                {achievement.isKeyAchievement && "★ "}
                                {achievement.description}
                            </li>
                        ))}
                    </ul>
                )}
            </Card>

            <Card>
                <h3 className="text-base font-semibold text-gray-900">Hours Breakdown</h3>
                {report.hours.length === 0 ? (
                    <p className="mt-2 text-sm text-gray-500">No hours recorded.</p>
                ) : (
                    <ul className="mt-2 flex flex-col gap-1 text-sm">
                        {report.hours.map((hour) => (
                            <li key={hour.id} className="text-gray-700">
                                {hour.taskType ?? "OTHER"}: {hour.hours}h
                            </li>
                        ))}
                    </ul>
                )}
            </Card>

            <Card>
                <h3 className="text-base font-semibold text-gray-900">Notes</h3>
                <p className="mt-2 whitespace-pre-wrap text-sm text-gray-700">{report.note || "-"}</p>
            </Card>
        </div>
    );
}
