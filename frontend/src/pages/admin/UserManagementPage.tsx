import { useEffect, useState } from "react";

import { Card } from "../../components/Card";
import { ConfirmDialog } from "../../components/ConfirmDialog";
import { Pagination } from "../../components/Pagination";
import { Spinner } from "../../components/Spinner";
import * as userService from "../../services/userService";
import type { NormalizedError } from "../../services/apiClient";
import type { Role, UserSummary } from "../../types";

const ROLES: Role[] = ["TEAM_MEMBER", "MANAGER", "ADMIN"];

type PendingRoleChange = { user: UserSummary; newRole: Role };

export function UserManagementPage() {
    const [users, setUsers] = useState<UserSummary[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);
    const [pendingRoleChange, setPendingRoleChange] = useState<PendingRoleChange | null>(null);
    const [pendingActiveChange, setPendingActiveChange] = useState<UserSummary | null>(null);

    function loadUsers() {
        setIsLoading(true);
        userService
            .getUsers({ page, size: 10 })
            .then((response) => {
                setUsers(response.content);
                setTotalPages(response.totalPages);
            })
            .finally(() => setIsLoading(false));
    }

    useEffect(() => {
        loadUsers();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [page]);

    async function confirmRoleChange() {
        if (!pendingRoleChange) return;
        try {
            await userService.updateUserRole(pendingRoleChange.user.id, pendingRoleChange.newRole);
            setMessage({ type: "success", text: "Role updated." });
            setPendingRoleChange(null);
            loadUsers();
        } catch (err) {
            const normalized = err as NormalizedError;
            setMessage({ type: "error", text: normalized.message ?? "Failed to update role" });
        }
    }

    async function confirmActiveChange() {
        if (!pendingActiveChange) return;
        try {
            await userService.updateUserActive(pendingActiveChange.id, !pendingActiveChange.active);
            setMessage({ type: "success", text: "User status updated." });
            setPendingActiveChange(null);
            loadUsers();
        } catch (err) {
            const normalized = err as NormalizedError;
            setMessage({ type: "error", text: normalized.message ?? "Failed to update user status" });
        }
    }

    return (
        <div className="mx-auto flex max-w-4xl flex-col gap-4 p-6">
            <h1 className="text-2xl font-semibold text-gray-900">User Management</h1>

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
                                <th className="pb-2">Email</th>
                                <th className="pb-2">Role</th>
                                <th className="pb-2">Status</th>
                                <th className="pb-2"></th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100">
                            {users.map((u) => (
                                <tr key={u.id}>
                                    <td className="py-2">{u.name}</td>
                                    <td className="py-2">{u.email}</td>
                                    <td className="py-2">
                                        <select
                                            className="rounded border border-gray-300 px-2 py-1 text-sm"
                                            value={u.roleName}
                                            onChange={(e) =>
                                                setPendingRoleChange({ user: u, newRole: e.target.value as Role })
                                            }
                                        >
                                            {ROLES.map((r) => (
                                                <option key={r} value={r}>
                                                    {r}
                                                </option>
                                            ))}
                                        </select>
                                    </td>
                                    <td className="py-2">{u.active ? "Active" : "Inactive"}</td>
                                    <td className="py-2">
                                        <button
                                            type="button"
                                            className="text-purple-600 hover:underline"
                                            onClick={() => setPendingActiveChange(u)}
                                        >
                                            {u.active ? "Deactivate" : "Reactivate"}
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
                <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
            </Card>

            {pendingRoleChange && (
                <ConfirmDialog
                    title="Change user role"
                    message={`Change ${pendingRoleChange.user.name}'s role to ${pendingRoleChange.newRole}?`}
                    confirmLabel="Change Role"
                    onConfirm={confirmRoleChange}
                    onCancel={() => setPendingRoleChange(null)}
                />
            )}

            {pendingActiveChange && (
                <ConfirmDialog
                    title={pendingActiveChange.active ? "Deactivate user" : "Reactivate user"}
                    message={`Are you sure you want to ${pendingActiveChange.active ? "deactivate" : "reactivate"} ${pendingActiveChange.name}?`}
                    confirmLabel={pendingActiveChange.active ? "Deactivate" : "Reactivate"}
                    onConfirm={confirmActiveChange}
                    onCancel={() => setPendingActiveChange(null)}
                />
            )}
        </div>
    );
}
