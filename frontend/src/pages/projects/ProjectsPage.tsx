import { useEffect, useState } from "react";

import { Button } from "../../components/Button";
import { Card } from "../../components/Card";
import { ConfirmDialog } from "../../components/ConfirmDialog";
import { Input } from "../../components/Input";
import { Modal } from "../../components/Modal";
import { Spinner } from "../../components/Spinner";
import { useAsyncAction } from "../../hooks/useAsyncAction";
import { useAuth } from "../../hooks/useAuth";
import * as projectService from "../../services/projectService";
import type { Project, ProjectFormValues } from "../../types";

const EMPTY_FORM: ProjectFormValues = { name: "", description: "" };

export function ProjectsPage() {
    const { user } = useAuth();
    const canManage = user?.role === "MANAGER" || user?.role === "ADMIN";

    const [projects, setProjects] = useState<Project[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    const [editingProject, setEditingProject] = useState<Project | null>(null);
    const [showModal, setShowModal] = useState(false);
    const [form, setForm] = useState<ProjectFormValues>(EMPTY_FORM);
    const [nameError, setNameError] = useState<string | null>(null);
    const [deactivateTarget, setDeactivateTarget] = useState<Project | null>(null);

    function loadProjects() {
        setIsLoading(true);
        projectService
            .getProjects(canManage)
            .then(setProjects)
            .catch(() => setProjects([]))
            .finally(() => setIsLoading(false));
    }

    useEffect(() => {
        loadProjects();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [canManage]);

    function openCreateModal() {
        setEditingProject(null);
        setForm(EMPTY_FORM);
        setNameError(null);
        setShowModal(true);
    }

    function openEditModal(project: Project) {
        setEditingProject(project);
        setForm({ name: project.name, description: project.description ?? "" });
        setNameError(null);
        setShowModal(true);
    }

    const { run: save, isLoading: isSaving } = useAsyncAction(
        async () => {
            if (editingProject) {
                await projectService.updateProject(editingProject.id, form);
            } else {
                await projectService.createProject(form);
            }
            setShowModal(false);
            loadProjects();
        },
        {
            successMessage: editingProject ? "Project updated." : "Project created.",
            errorFallback: "Failed to save project",
        },
    );

    function handleSave() {
        if (!form.name.trim()) {
            setNameError("Project name is required.");
            return;
        }
        setNameError(null);
        save();
    }

    const { run: handleDeactivate, isLoading: isDeactivating } = useAsyncAction(
        async () => {
            if (!deactivateTarget) return;
            await projectService.deleteProject(deactivateTarget.id);
            setDeactivateTarget(null);
            loadProjects();
        },
        { successMessage: "Project deactivated.", errorFallback: "Failed to deactivate project" },
    );

    return (
        <div className="mx-auto flex max-w-4xl flex-col gap-4 p-6">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <h1 className="text-2xl font-semibold text-gray-900">Projects</h1>
                {canManage && (
                    <Button type="button" onClick={openCreateModal}>
                        + Add Project
                    </Button>
                )}
            </div>

            <Card>
                {isLoading ? (
                    <Spinner />
                ) : projects.length === 0 ? (
                    <div className="flex flex-col items-center gap-3 py-8">
                        <p className="text-gray-600">No projects yet.</p>
                        {canManage && (
                            <Button type="button" onClick={openCreateModal}>
                                + Add Project
                            </Button>
                        )}
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm">
                            <thead className="text-gray-500">
                                <tr>
                                    <th className="pb-2">Name</th>
                                    <th className="pb-2">Description</th>
                                    <th className="pb-2">Status</th>
                                    {canManage && <th className="pb-2"></th>}
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-100">
                                {projects.map((project) => (
                                    <tr key={project.id}>
                                        <td className="py-2">{project.name}</td>
                                        <td className="py-2">{project.description || "-"}</td>
                                        <td className="py-2">
                                            <span
                                                className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium ${project.active ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-500"
                                                    }`}
                                            >
                                                {project.active ? "Active" : "Inactive"}
                                            </span>
                                        </td>
                                        {canManage && (
                                            <td className="py-2">
                                                <div className="flex gap-2">
                                                    <Button
                                                        type="button"
                                                        variant="secondary"
                                                        onClick={() => openEditModal(project)}
                                                    >
                                                        Edit
                                                    </Button>
                                                    {project.active && (
                                                        <Button
                                                            type="button"
                                                            variant="destructive"
                                                            disabled={isDeactivating}
                                                            onClick={() => setDeactivateTarget(project)}
                                                        >
                                                            Deactivate
                                                        </Button>
                                                    )}
                                                </div>
                                            </td>
                                        )}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </Card>

            {showModal && (
                <Modal title={editingProject ? "Edit Project" : "Add Project"} onClose={() => setShowModal(false)}>
                    <div className="flex flex-col gap-3">
                        <Input
                            label="Name *"
                            value={form.name}
                            error={nameError ?? undefined}
                            onChange={(e) => {
                                setForm((f) => ({ ...f, name: e.target.value }));
                                if (nameError) setNameError(null);
                            }}
                        />
                        <div className="flex flex-col gap-1">
                            <label className="text-sm font-medium text-gray-700">Description</label>
                            <textarea
                                className="rounded-md border border-gray-300 px-3 py-2 text-sm"
                                rows={3}
                                value={form.description}
                                onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
                            />
                        </div>
                        <div className="flex justify-end gap-2">
                            <Button type="button" variant="secondary" disabled={isSaving} onClick={() => setShowModal(false)}>
                                Cancel
                            </Button>
                            <Button type="button" disabled={isSaving} onClick={handleSave}>
                                {isSaving ? "Saving..." : "Save"}
                            </Button>
                        </div>
                    </div>
                </Modal>
            )}

            {deactivateTarget && (
                <ConfirmDialog
                    title="Deactivate project"
                    message={`Are you sure you want to deactivate "${deactivateTarget.name}"? It will no longer be selectable for new reports.`}
                    confirmLabel="Deactivate"
                    onConfirm={handleDeactivate}
                    onCancel={() => setDeactivateTarget(null)}
                />
            )}
        </div>
    );
}
