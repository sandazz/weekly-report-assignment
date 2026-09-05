import { useEffect, useState } from "react";

import * as projectService from "../services/projectService";
import * as userService from "../services/userService";
import type { Project, ReportStatus, UserSummary } from "../types";
import type { ReportFilterValues } from "../hooks/useReportFilters";
import { Button } from "./Button";

const STATUS_OPTIONS: ReportStatus[] = ["DRAFT", "SUBMITTED", "NEEDS_CORRECTION", "APPROVED"];

interface Props {
    value: ReportFilterValues;
    onChange: (patch: Partial<ReportFilterValues>) => void;
    onClearAll: () => void;
    showMemberFilter?: boolean;
}

// Self-contained: fetches its own dropdown data so both pages that use it don't duplicate that
// boilerplate. Not wired for a free-text field yet, but the value/onChange shape makes adding one
// straightforward without restructuring this component.
export function FilterBar({ value, onChange, onClearAll, showMemberFilter = false }: Props) {
    const [projects, setProjects] = useState<Project[]>([]);
    const [members, setMembers] = useState<UserSummary[]>([]);

    useEffect(() => {
        projectService.getProjects(true).then(setProjects).catch(() => setProjects([]));
    }, []);

    useEffect(() => {
        if (!showMemberFilter) return;
        userService
            .getUsers({ roleName: "TEAM_MEMBER", size: 100 })
            .then((response) => setMembers(response.content))
            .catch(() => setMembers([]));
    }, [showMemberFilter]);

    return (
        <div className="flex flex-wrap items-end gap-3">
            <div className="flex flex-col gap-1">
                <label className="text-xs font-medium text-gray-500">Status</label>
                <select
                    className="rounded-md border border-gray-300 px-3 py-2 text-sm"
                    value={value.status}
                    onChange={(e) => onChange({ status: e.target.value as ReportStatus | "" })}
                >
                    <option value="">All statuses</option>
                    {STATUS_OPTIONS.map((s) => (
                        <option key={s} value={s}>
                            {s}
                        </option>
                    ))}
                </select>
            </div>

            <div className="flex flex-col gap-1">
                <label className="text-xs font-medium text-gray-500">Project</label>
                <select
                    className="rounded-md border border-gray-300 px-3 py-2 text-sm"
                    value={value.projectId}
                    onChange={(e) => onChange({ projectId: e.target.value === "" ? "" : Number(e.target.value) })}
                >
                    <option value="">All projects</option>
                    {projects.map((project) => (
                        <option key={project.id} value={project.id}>
                            {project.name}
                        </option>
                    ))}
                </select>
            </div>

            {showMemberFilter && (
                <div className="flex flex-col gap-1">
                    <label className="text-xs font-medium text-gray-500">Member</label>
                    <select
                        className="rounded-md border border-gray-300 px-3 py-2 text-sm"
                        value={value.memberId}
                        onChange={(e) => onChange({ memberId: e.target.value === "" ? "" : Number(e.target.value) })}
                    >
                        <option value="">All members</option>
                        {members.map((member) => (
                            <option key={member.id} value={member.id}>
                                {member.name}
                            </option>
                        ))}
                    </select>
                </div>
            )}

            <div className="flex flex-col gap-1">
                <label className="text-xs font-medium text-gray-500">From</label>
                <input
                    type="date"
                    className="rounded-md border border-gray-300 px-3 py-2 text-sm"
                    value={value.fromDate}
                    onChange={(e) => onChange({ fromDate: e.target.value })}
                />
            </div>

            <div className="flex flex-col gap-1">
                <label className="text-xs font-medium text-gray-500">To</label>
                <input
                    type="date"
                    className="rounded-md border border-gray-300 px-3 py-2 text-sm"
                    value={value.toDate}
                    onChange={(e) => onChange({ toDate: e.target.value })}
                />
            </div>

            {(value.fromDate || value.toDate) && (
                <Button
                    type="button"
                    variant="secondary"
                    onClick={() => onChange({ fromDate: "", toDate: "" })}
                >
                    Clear dates
                </Button>
            )}

            <Button type="button" variant="secondary" onClick={onClearAll}>
                Clear All Filters
            </Button>
        </div>
    );
}
