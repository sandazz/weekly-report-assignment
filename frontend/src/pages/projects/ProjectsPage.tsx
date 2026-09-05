import { useEffect, useState } from "react";

import { Button } from "../../components/Button";
import { Card } from "../../components/Card";
import { ConfirmDialog } from "../../components/ConfirmDialog";
import { Input } from "../../components/Input";
import { Modal } from "../../components/Modal";
import { Spinner } from "../../components/Spinner";
import { useAuth } from "../../hooks/useAuth";
import * as projectService from "../../services/projectService";
import type { NormalizedError } from "../../services/apiClient";
import type { Project, ProjectFormValues } from "../../types";

const EMPTY_FORM: ProjectFormValues = { name: "", description: "" };

export function ProjectsPage() {
    const { user } = useAuth();
    const canManage = user?.role === "MANAGER" || user?.role === "ADMIN";

    const [projects, setProjects] = useState<Project[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);

    const [editingProject, setEditingProject] = useState<Project | null>(null);
    const [showModal, setShowModal] = useState(false);
    const [form, setForm] = useState<ProjectFormValues>(EMPTY_FORM);
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
        setShowModal(true);
    }

    function openEditModal(project: Project) {
        setEditingProject(project);
        setForm({ name: project.name, description: project.description ?? "" });
        setShowModal(true);
    }

    async function handleSave() {
        try {
            if (editingProject) {
                await projectService.updateProject(editingProject.id, form);
                setMessage({ type: "success", text: "Project updated." });
            } else {
                await projectService.createProject(form);
                setMessage({ type: "success", text: "Project created." });
            }
            setShowModal(false);
            loadProjects();
        } catch (err) {
            const normalized = err as NormalizedError;
            setMessage({ type: "error", text: normalized.message ?? "Failed to save project" });
        }
    }

    async function handleDeactivate() {
        if (!deactivateTarget) return;
        try {
            await projectService.deleteProject(deactivateTarget.id);
            setMessage({ type: "success", text: "Project deactivated." });
            setDeactivateTarget(null);
            loadProjects();
        } catch (err) {
            const normalized = err as NormalizedError;
            setMessage({ type: "error", text: normalized.message ?? "Failed to deactivate project" });
        }
    }

    return (
        <div className="mx-auto flex max-w-4xl flex-col gap-4 p-6">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-semibold text-gray-900">Projects</h1>
                {canManage && (
                    <Button type="button" onClick={openCreateModal}>
                        + Add Project
                    </Button>
                )}
            </div>

            {message && (
                <div
                    className={`rounded-md px-4 py-2 text-sm ${message.type === "success" ? "bg-green-50 text-green-700" : "bg-red-50 text-red-700"
                        }`}
                >
                    {message.text}
                </div>
            )}

            <Card>
                {isLoading ? (
                    <Spinner />
                ) : (
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
                                        {project.active ? (
                                            <span className="text-green-700">Active</span>
                                        ) : (
                                            <span className="text-gray-500">Inactive</span>
                                        )}
                                    </td>
                                    {canManage && (
                                        <td className="flex gap-2 py-2">
                                            <button
                                                type="button"
                                                className="text-purple-600 hover:underline"
                                                onClick={() => openEditModal(project)}
                                            >
                                                Edit
                                            </button>
                                            {project.active && (
                                                <button
                                                    type="button"
                                                    className="text-red-600 hover:underline"
                                                    onClick={() => setDeactivateTarget(project)}
                                                >
                                                    Deactivate
                                                </button>
                                            )}
                                        </td>
                                    )}
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </Card>

            {showModal && (
                <Modal title={editingProject ? "Edit Project" : "Add Project"} onClose={() => setShowModal(false)}>
                    <div className="flex flex-col gap-3">
                        <Input
                            label="Name"
                            value={form.name}
                            onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
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
                            <Button type="button" variant="secondary" onClick={() => setShowModal(false)}>
                                Cancel
                            </Button>
                            <Button type="button" onClick={handleSave}>
                                Save
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
