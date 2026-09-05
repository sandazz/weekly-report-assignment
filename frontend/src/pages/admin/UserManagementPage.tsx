import { useEffect, useState } from "react";

import { Card } from "../../components/Card";
import { ConfirmDialog } from "../../components/ConfirmDialog";
import { Pagination } from "../../components/Pagination";
import { Spinner } from "../../components/Spinner";
import { Button } from "../../components/Button";
import { useAsyncAction } from "../../hooks/useAsyncAction";
import * as userService from "../../services/userService";
import type { Role, UserSummary } from "../../types";

const ROLES: Role[] = ["TEAM_MEMBER", "MANAGER", "ADMIN"];

type PendingRoleChange = { user: UserSummary; newRole: Role };

export function UserManagementPage() {
    const [users, setUsers] = useState<UserSummary[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
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

    const { run: confirmRoleChange, isLoading: isChangingRole } = useAsyncAction(
        async () => {
            if (!pendingRoleChange) return;
            await userService.updateUserRole(pendingRoleChange.user.id, pendingRoleChange.newRole);
            setPendingRoleChange(null);
            loadUsers();
        },
        { successMessage: "Role updated.", errorFallback: "Failed to update role" },
    );

    const { run: confirmActiveChange, isLoading: isChangingActive } = useAsyncAction(
        async () => {
            if (!pendingActiveChange) return;
            await userService.updateUserActive(pendingActiveChange.id, !pendingActiveChange.active);
            setPendingActiveChange(null);
            loadUsers();
        },
        { successMessage: "User status updated.", errorFallback: "Failed to update user status" },
    );

    return (
        <div className="mx-auto flex max-w-4xl flex-col gap-4 p-6">
            <h1 className="text-2xl font-semibold text-gray-900">User Management</h1>

            <Card>
                {isLoading ? (
                    <Spinner />
                ) : users.length === 0 ? (
                    <p className="py-8 text-center text-gray-600">No users found.</p>
                ) : (
                    <div className="overflow-x-auto">
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
                                                disabled={isChangingRole}
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
                                        <td className="py-2">
                                            <span
                                                className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium ${u.active ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-500"
                                                    }`}
                                            >
                                                {u.active ? "Active" : "Inactive"}
                                            </span>
                                        </td>
                                        <td className="py-2">
                                            <Button
                                                type="button"
                                                variant={u.active ? "destructive" : "secondary"}
                                                disabled={isChangingActive}
                                                onClick={() => setPendingActiveChange(u)}
                                            >
                                                {u.active ? "Deactivate" : "Reactivate"}
                                            </Button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
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
