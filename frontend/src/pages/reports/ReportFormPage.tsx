import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useForm } from "react-hook-form";

import { Button } from "../../components/Button";
import { Card } from "../../components/Card";
import { Input } from "../../components/Input";
import { Spinner } from "../../components/Spinner";
import { useAsyncAction } from "../../hooks/useAsyncAction";
import { notifyError, notifySuccess } from "../../lib/toast";
import * as reportService from "../../services/reportService";
import * as reportTaskService from "../../services/reportTaskService";
import * as reportBlockerService from "../../services/reportBlockerService";
import * as reportAchievementService from "../../services/reportAchievementService";
import * as reportHourService from "../../services/reportHourService";
import * as projectService from "../../services/projectService";
import * as reviewService from "../../services/reviewService";
import type {
    AchievementFormRow,
    BlockerFormRow,
    HourFormRow,
    Project,
    ReportFormValues,
    ReportStatus,
    TaskFormRow,
    TaskPriority,
    TaskStatus,
    TaskType,
} from "../../types";

const TASK_PRIORITIES: TaskPriority[] = ["LOW", "MEDIUM", "HIGH"];
const TASK_STATUSES: TaskStatus[] = ["NOT_STARTED", "IN_PROGRESS", "COMPLETED", "BLOCKED"];
const TASK_TYPES: TaskType[] = ["DEVELOPMENT", "TESTING", "MEETINGS", "DOCUMENTATION", "OTHER"];
const EDITABLE_STATUSES: ReportStatus[] = ["DRAFT", "NEEDS_CORRECTION"];

const EMPTY_TASK: TaskFormRow = {
    taskName: "",
    priority: "",
    plannedPercentage: "",
    actualPercentage: "",
    status: "",
    plannedHours: "",
    spentHours: "",
    deliverable: "",
};
const EMPTY_BLOCKER: BlockerFormRow = { description: "", isKeyIssue: false };
const EMPTY_ACHIEVEMENT: AchievementFormRow = { description: "", isKeyAchievement: false };
const EMPTY_HOUR: HourFormRow = { taskType: "", hours: "" };

export function ReportFormPage() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [reportId, setReportId] = useState<number | null>(id ? Number(id) : null);
    const [status, setStatus] = useState<ReportStatus | null>(null);
    const [projects, setProjects] = useState<Project[]>([]);
    const [tasks, setTasks] = useState<TaskFormRow[]>([]);
    const [blockers, setBlockers] = useState<BlockerFormRow[]>([]);
    const [achievements, setAchievements] = useState<AchievementFormRow[]>([]);
    const [hours, setHours] = useState<HourFormRow[]>([]);
    const [correctionComment, setCorrectionComment] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(Boolean(id));
    const [isRowActionPending, setIsRowActionPending] = useState(false);

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm<ReportFormValues>({
        defaultValues: {
            projectId: 0,
            weekStart: "",
            weekEnd: "",
            nextWeekPlan: "",
            keyBlocker: "",
            keyAchievement: "",
            note: "",
        },
    });

    const isLocked = status === "SUBMITTED" || status === "APPROVED";
    const isEditable = status !== null && EDITABLE_STATUSES.includes(status);
    const hasSavedTask = tasks.some((t) => t.id);
    const canSubmit = reportId !== null && isEditable && hasSavedTask;
    const submitBlockedReason =
        reportId !== null && isEditable && !hasSavedTask ? "Add and save at least one task before submitting." : null;

    useEffect(() => {
        projectService.getProjects().then(setProjects).catch(() => setProjects([]));
    }, []);

    useEffect(() => {
        if (!id) return;
        setIsLoading(true);
        reportService
            .getReport(Number(id))
            .then(async (report) => {
                setReportId(report.id);
                setStatus(report.status);
                reset({
                    projectId: report.project.id,
                    weekStart: report.weekStart,
                    weekEnd: report.weekEnd,
                    nextWeekPlan: report.nextWeekPlan ?? "",
                    keyBlocker: report.keyBlocker ?? "",
                    keyAchievement: report.keyAchievement ?? "",
                    note: report.note ?? "",
                });
                setTasks(
                    report.tasks.map((t) => ({
                        id: t.id,
                        taskName: t.taskName,
                        priority: t.priority ?? "",
                        plannedPercentage: t.plannedPercentage ?? "",
                        actualPercentage: t.actualPercentage ?? "",
                        status: t.status ?? "",
                        plannedHours: t.plannedHours ?? "",
                        spentHours: t.spentHours ?? "",
                        deliverable: t.deliverable ?? "",
                    })),
                );
                setBlockers(
                    report.blockers.map((b) => ({ id: b.id, description: b.description, isKeyIssue: b.isKeyIssue })),
                );
                setAchievements(
                    report.achievements.map((a) => ({
                        id: a.id,
                        description: a.description,
                        isKeyAchievement: a.isKeyAchievement,
                    })),
                );
                setHours(report.hours.map((h) => ({ id: h.id, taskType: h.taskType ?? "", hours: h.hours })));

                if (report.status === "NEEDS_CORRECTION") {
                    const history = await reviewService.getReviewHistory(report.id);
                    setCorrectionComment(history.length > 0 ? history[history.length - 1].comment : null);
                }
            })
            .catch((err) => notifyError(err, "Failed to load report"))
            .finally(() => setIsLoading(false));
    }, [id, reset]);

    const keyBlockerText = useMemo(() => blockers.find((b) => b.isKeyIssue)?.description ?? "", [blockers]);
    const keyAchievementText = useMemo(
        () => achievements.find((a) => a.isKeyAchievement)?.description ?? "",
        [achievements],
    );

    const { run: onSaveDraft, isLoading: isSavingDraft } = useAsyncAction(
        async (values: ReportFormValues) => {
            const payload: ReportFormValues = {
                ...values,
                projectId: Number(values.projectId),
                keyBlocker: keyBlockerText,
                keyAchievement: keyAchievementText,
            };
            if (reportId === null) {
                const created = await reportService.createReport(payload);
                setReportId(created.id);
                setStatus(created.status);
            } else {
                const updated = await reportService.updateReport(reportId, payload);
                setStatus(updated.status);
            }
        },
        { successMessage: "Draft saved.", errorFallback: "Failed to save draft" },
    );

    const { run: onSubmitForReview, isLoading: isSubmitting } = useAsyncAction(
        async () => {
            if (reportId === null) return;
            await reportService.submitReport(reportId);
            navigate(`/reports/${reportId}`);
        },
        { successMessage: "Report submitted for review.", errorFallback: "Failed to submit report" },
    );

    const isSaving = isSavingDraft || isSubmitting;

    // --- Tasks ---
    function addTaskRow() {
        setTasks((prev) => [...prev, { ...EMPTY_TASK }]);
    }
    function updateTaskRow(index: number, patch: Partial<TaskFormRow>) {
        setTasks((prev) => prev.map((row, i) => (i === index ? { ...row, ...patch } : row)));
    }
    async function saveTaskRow(index: number) {
        if (reportId === null) return;
        const row = tasks[index];
        setIsRowActionPending(true);
        try {
            const saved = row.id
                ? await reportTaskService.update(reportId, row.id, row)
                : await reportTaskService.add(reportId, row);
            updateTaskRow(index, {
                id: saved.id,
                priority: saved.priority ?? "",
                plannedPercentage: saved.plannedPercentage ?? "",
                actualPercentage: saved.actualPercentage ?? "",
                status: saved.status ?? "",
                plannedHours: saved.plannedHours ?? "",
                spentHours: saved.spentHours ?? "",
            });
            notifySuccess("Task saved.");
        } catch (err) {
            notifyError(err, "Failed to save task");
        } finally {
            setIsRowActionPending(false);
        }
    }
    async function deleteTaskRow(index: number) {
        const row = tasks[index];
        if (reportId !== null && row.id) {
            setIsRowActionPending(true);
            try {
                await reportTaskService.remove(reportId, row.id);
            } catch (err) {
                notifyError(err, "Failed to delete task");
                return;
            } finally {
                setIsRowActionPending(false);
            }
        }
        setTasks((prev) => prev.filter((_, i) => i !== index));
    }

    // --- Blockers ---
    function addBlockerRow() {
        setBlockers((prev) => [...prev, { ...EMPTY_BLOCKER }]);
    }
    function updateBlockerRow(index: number, patch: Partial<BlockerFormRow>) {
        setBlockers((prev) => prev.map((row, i) => (i === index ? { ...row, ...patch } : row)));
    }
    function toggleKeyBlocker(index: number) {
        setBlockers((prev) => prev.map((row, i) => ({ ...row, isKeyIssue: i === index })));
    }
    async function saveBlockerRow(index: number) {
        if (reportId === null) return;
        const row = blockers[index];
        setIsRowActionPending(true);
        try {
            const saved = row.id
                ? await reportBlockerService.update(reportId, row.id, row)
                : await reportBlockerService.add(reportId, row);
            updateBlockerRow(index, { id: saved.id });
            notifySuccess("Blocker saved.");
        } catch (err) {
            notifyError(err, "Failed to save blocker");
        } finally {
            setIsRowActionPending(false);
        }
    }
    async function deleteBlockerRow(index: number) {
        const row = blockers[index];
        if (reportId !== null && row.id) {
            setIsRowActionPending(true);
            try {
                await reportBlockerService.remove(reportId, row.id);
            } catch (err) {
                notifyError(err, "Failed to delete blocker");
                return;
            } finally {
                setIsRowActionPending(false);
            }
        }
        setBlockers((prev) => prev.filter((_, i) => i !== index));
    }

    // --- Achievements ---
    function addAchievementRow() {
        setAchievements((prev) => [...prev, { ...EMPTY_ACHIEVEMENT }]);
    }
    function updateAchievementRow(index: number, patch: Partial<AchievementFormRow>) {
        setAchievements((prev) => prev.map((row, i) => (i === index ? { ...row, ...patch } : row)));
    }
    function toggleKeyAchievement(index: number) {
        setAchievements((prev) => prev.map((row, i) => ({ ...row, isKeyAchievement: i === index })));
    }
    async function saveAchievementRow(index: number) {
        if (reportId === null) return;
        const row = achievements[index];
        setIsRowActionPending(true);
        try {
            const saved = row.id
                ? await reportAchievementService.update(reportId, row.id, row)
                : await reportAchievementService.add(reportId, row);
            updateAchievementRow(index, { id: saved.id });
            notifySuccess("Achievement saved.");
        } catch (err) {
            notifyError(err, "Failed to save achievement");
        } finally {
            setIsRowActionPending(false);
        }
    }
    async function deleteAchievementRow(index: number) {
        const row = achievements[index];
        if (reportId !== null && row.id) {
            setIsRowActionPending(true);
            try {
                await reportAchievementService.remove(reportId, row.id);
            } catch (err) {
                notifyError(err, "Failed to delete achievement");
                return;
            } finally {
                setIsRowActionPending(false);
            }
        }
        setAchievements((prev) => prev.filter((_, i) => i !== index));
    }

    // --- Hours ---
    function addHourRow() {
        setHours((prev) => [...prev, { ...EMPTY_HOUR }]);
    }
    function updateHourRow(index: number, patch: Partial<HourFormRow>) {
        setHours((prev) => prev.map((row, i) => (i === index ? { ...row, ...patch } : row)));
    }
    async function saveHourRow(index: number) {
        if (reportId === null) return;
        const row = hours[index];
        setIsRowActionPending(true);
        try {
            const saved = row.id
                ? await reportHourService.update(reportId, row.id, row)
                : await reportHourService.add(reportId, row);
            updateHourRow(index, { id: saved.id });
            notifySuccess("Hours saved.");
        } catch (err) {
            notifyError(err, "Failed to save hours");
        } finally {
            setIsRowActionPending(false);
        }
    }
    async function deleteHourRow(index: number) {
        const row = hours[index];
        if (reportId !== null && row.id) {
            setIsRowActionPending(true);
            try {
                await reportHourService.remove(reportId, row.id);
            } catch (err) {
                notifyError(err, "Failed to delete hours");
                return;
            } finally {
                setIsRowActionPending(false);
            }
        }
        setHours((prev) => prev.filter((_, i) => i !== index));
    }

    if (isLoading) return <Spinner />;

    return (
        <div className="mx-auto flex max-w-4xl flex-col gap-4 p-6">
            <h1 className="text-2xl font-semibold text-gray-900">{id ? "Edit Report" : "New Report"}</h1>

            {isLocked && (
                <div className="rounded-md bg-gray-100 px-4 py-2 text-sm text-gray-700">
                    This report has been {status === "SUBMITTED" ? "submitted" : "approved"} and is locked for
                    editing.
                </div>
            )}

            {status === "NEEDS_CORRECTION" && correctionComment && (
                <div className="rounded-md bg-orange-50 px-4 py-2 text-sm text-orange-800">
                    <strong>Manager's correction comment:</strong> {correctionComment}
                </div>
            )}

            <Card>
                <form onSubmit={handleSubmit(onSaveDraft)} className="flex flex-col gap-4">
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                        <Input
                            label="Week start"
                            type="date"
                            disabled={isLocked}
                            error={errors.weekStart?.message}
                            {...register("weekStart", { required: "Week start is required" })}
                        />
                        <Input
                            label="Week end"
                            type="date"
                            disabled={isLocked}
                            error={errors.weekEnd?.message}
                            {...register("weekEnd", { required: "Week end is required" })}
                        />
                    </div>

                    <div className="flex flex-col gap-1">
                        <label className="text-sm font-medium text-gray-700">Project</label>
                        <select
                            disabled={isLocked}
                            className="rounded-md border border-gray-300 px-3 py-2 text-sm"
                            {...register("projectId", { required: true, valueAsNumber: true })}
                        >
                            <option value={0}>Select a project</option>
                            {projects.map((project) => (
                                <option key={project.id} value={project.id}>
                                    {project.name}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="flex flex-col gap-1">
                        <label className="text-sm font-medium text-gray-700">Next week's plan</label>
                        <textarea
                            disabled={isLocked}
                            rows={3}
                            className="rounded-md border border-gray-300 px-3 py-2 text-sm"
                            {...register("nextWeekPlan")}
                        />
                    </div>

                    <div className="flex flex-col gap-1">
                        <label className="text-sm font-medium text-gray-700">Notes (optional)</label>
                        <textarea
                            disabled={isLocked}
                            rows={3}
                            className="rounded-md border border-gray-300 px-3 py-2 text-sm"
                            {...register("note")}
                        />
                    </div>

                    <div className="flex flex-col gap-2 sm:flex-row">
                        <Button type="submit" disabled={isLocked || isSaving}>
                            {isSavingDraft ? "Saving..." : "Save Draft"}
                        </Button>
                        <Button
                            type="button"
                            variant="secondary"
                            disabled={!canSubmit || isSaving}
                            onClick={() => onSubmitForReview()}
                        >
                            {isSubmitting ? "Submitting..." : "Submit for Review"}
                        </Button>
                    </div>
                    {submitBlockedReason && <p className="text-xs text-gray-500">{submitBlockedReason}</p>}
                </form>
            </Card>

            {reportId === null ? (
                <p className="text-sm text-gray-500">Save the report as a draft first to add tasks, blockers, achievements, and hours.</p>
            ) : (
                <>
                    <Card>
                        <h2 className="text-base font-semibold text-gray-900">Tasks</h2>
                        <div className="mt-3 flex flex-col gap-3">
                            {tasks.map((row, index) => (
                                <div key={index} className="grid grid-cols-1 gap-2 rounded-md border border-gray-200 p-2 md:grid-cols-8">
                                    <input
                                        className="rounded border border-gray-300 px-2 py-1 text-sm md:col-span-2"
                                        placeholder="Task name"
                                        disabled={isLocked}
                                        value={row.taskName}
                                        onChange={(e) => updateTaskRow(index, { taskName: e.target.value })}
                                    />
                                    <select
                                        className="rounded border border-gray-300 px-2 py-1 text-sm"
                                        disabled={isLocked}
                                        value={row.priority}
                                        onChange={(e) => updateTaskRow(index, { priority: e.target.value as TaskPriority })}
                                    >
                                        <option value="">Priority</option>
                                        {TASK_PRIORITIES.map((p) => (
                                            <option key={p} value={p}>
                                                {p}
                                            </option>
                                        ))}
                                    </select>
                                    <input
                                        className="rounded border border-gray-300 px-2 py-1 text-sm"
                                        type="number"
                                        min={0}
                                        max={100}
                                        placeholder="Planned %"
                                        disabled={isLocked}
                                        value={row.plannedPercentage}
                                        onChange={(e) =>
                                            updateTaskRow(index, {
                                                plannedPercentage: e.target.value === "" ? "" : Number(e.target.value),
                                            })
                                        }
                                    />
                                    <input
                                        className="rounded border border-gray-300 px-2 py-1 text-sm"
                                        type="number"
                                        min={0}
                                        max={100}
                                        placeholder="Actual %"
                                        disabled={isLocked}
                                        value={row.actualPercentage}
                                        onChange={(e) =>
                                            updateTaskRow(index, {
                                                actualPercentage: e.target.value === "" ? "" : Number(e.target.value),
                                            })
                                        }
                                    />
                                    <select
                                        className="rounded border border-gray-300 px-2 py-1 text-sm"
                                        disabled={isLocked}
                                        value={row.status}
                                        onChange={(e) => updateTaskRow(index, { status: e.target.value as TaskStatus })}
                                    >
                                        <option value="">Status</option>
                                        {TASK_STATUSES.map((s) => (
                                            <option key={s} value={s}>
                                                {s}
                                            </option>
                                        ))}
                                    </select>
                                    <input
                                        className="rounded border border-gray-300 px-2 py-1 text-sm"
                                        placeholder="Deliverable"
                                        disabled={isLocked}
                                        value={row.deliverable}
                                        onChange={(e) => updateTaskRow(index, { deliverable: e.target.value })}
                                    />
                                    <div className="flex gap-1 md:col-span-1">
                                        <Button type="button" disabled={isLocked || isRowActionPending} onClick={() => saveTaskRow(index)}>
                                            Save
                                        </Button>
                                        <Button
                                            type="button"
                                            variant="secondary"
                                            disabled={isLocked || isRowActionPending}
                                            onClick={() => deleteTaskRow(index)}
                                        >
                                            Delete
                                        </Button>
                                    </div>
                                </div>
                            ))}
                            <Button type="button" variant="secondary" disabled={isLocked || isRowActionPending} onClick={addTaskRow}>
                                + Add Task
                            </Button>
                        </div>
                    </Card>

                    <Card>
                        <h2 className="text-base font-semibold text-gray-900">Blockers</h2>
                        <div className="mt-3 flex flex-col gap-3">
                            {blockers.map((row, index) => (
                                <div key={index} className="flex flex-col gap-2 rounded-md border border-gray-200 p-2 sm:flex-row sm:items-center">
                                    <div className="flex items-center gap-2">
                                        <input
                                            type="radio"
                                            name="key-blocker"
                                            disabled={isLocked}
                                            checked={row.isKeyIssue}
                                            onChange={() => toggleKeyBlocker(index)}
                                        />
                                        <span className="text-xs text-gray-500">Key issue</span>
                                    </div>
                                    <input
                                        className="flex-1 rounded border border-gray-300 px-2 py-1 text-sm"
                                        placeholder="Blocker description"
                                        disabled={isLocked}
                                        value={row.description}
                                        onChange={(e) => updateBlockerRow(index, { description: e.target.value })}
                                    />
                                    <div className="flex gap-2">
                                        <Button type="button" disabled={isLocked || isRowActionPending} onClick={() => saveBlockerRow(index)}>
                                            Save
                                        </Button>
                                        <Button
                                            type="button"
                                            variant="secondary"
                                            disabled={isLocked || isRowActionPending}
                                            onClick={() => deleteBlockerRow(index)}
                                        >
                                            Delete
                                        </Button>
                                    </div>
                                </div>
                            ))}
                            <Button type="button" variant="secondary" disabled={isLocked || isRowActionPending} onClick={addBlockerRow}>
                                + Add Blocker
                            </Button>
                        </div>
                    </Card>

                    <Card>
                        <h2 className="text-base font-semibold text-gray-900">Achievements</h2>
                        <div className="mt-3 flex flex-col gap-3">
                            {achievements.map((row, index) => (
                                <div key={index} className="flex flex-col gap-2 rounded-md border border-gray-200 p-2 sm:flex-row sm:items-center">
                                    <div className="flex items-center gap-2">
                                        <input
                                            type="radio"
                                            name="key-achievement"
                                            disabled={isLocked}
                                            checked={row.isKeyAchievement}
                                            onChange={() => toggleKeyAchievement(index)}
                                        />
                                        <span className="text-xs text-gray-500">Key achievement</span>
                                    </div>
                                    <input
                                        className="flex-1 rounded border border-gray-300 px-2 py-1 text-sm"
                                        placeholder="Achievement description"
                                        disabled={isLocked}
                                        value={row.description}
                                        onChange={(e) => updateAchievementRow(index, { description: e.target.value })}
                                    />
                                    <div className="flex gap-2">
                                        <Button type="button" disabled={isLocked || isRowActionPending} onClick={() => saveAchievementRow(index)}>
                                            Save
                                        </Button>
                                        <Button
                                            type="button"
                                            variant="secondary"
                                            disabled={isLocked || isRowActionPending}
                                            onClick={() => deleteAchievementRow(index)}
                                        >
                                            Delete
                                        </Button>
                                    </div>
                                </div>
                            ))}
                            <Button type="button" variant="secondary" disabled={isLocked || isRowActionPending} onClick={addAchievementRow}>
                                + Add Achievement
                            </Button>
                        </div>
                    </Card>

                    <Card>
                        <h2 className="text-base font-semibold text-gray-900">Hours breakdown (optional)</h2>
                        <div className="mt-3 flex flex-col gap-3">
                            {hours.map((row, index) => (
                                <div key={index} className="flex flex-col gap-2 rounded-md border border-gray-200 p-2 sm:flex-row sm:items-center">
                                    <select
                                        className="rounded border border-gray-300 px-2 py-1 text-sm"
                                        disabled={isLocked}
                                        value={row.taskType}
                                        onChange={(e) => updateHourRow(index, { taskType: e.target.value as TaskType })}
                                    >
                                        <option value="">Type</option>
                                        {TASK_TYPES.map((t) => (
                                            <option key={t} value={t}>
                                                {t}
                                            </option>
                                        ))}
                                    </select>
                                    <input
                                        className="w-full rounded border border-gray-300 px-2 py-1 text-sm sm:w-24"
                                        type="number"
                                        min={0}
                                        placeholder="Hours"
                                        disabled={isLocked}
                                        value={row.hours}
                                        onChange={(e) =>
                                            updateHourRow(index, { hours: e.target.value === "" ? "" : Number(e.target.value) })
                                        }
                                    />
                                    <div className="flex gap-2">
                                        <Button type="button" disabled={isLocked || isRowActionPending} onClick={() => saveHourRow(index)}>
                                            Save
                                        </Button>
                                        <Button
                                            type="button"
                                            variant="secondary"
                                            disabled={isLocked || isRowActionPending}
                                            onClick={() => deleteHourRow(index)}
                                        >
                                            Delete
                                        </Button>
                                    </div>
                                </div>
                            ))}
                            <Button type="button" variant="secondary" disabled={isLocked || isRowActionPending} onClick={addHourRow}>
                                + Add Hours
                            </Button>
                        </div>
                    </Card>
                </>
            )}
        </div>
    );
}
