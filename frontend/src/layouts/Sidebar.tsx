import { NavLink } from "react-router-dom";

import { useHasRole } from "../hooks/useHasRole";
import type { Role } from "../types";

interface NavItem {
    label: string;
    to: string;
    roles: Role[];
}

const NAV_ITEMS: NavItem[] = [
    { label: "Dashboard", to: "/dashboard", roles: ["TEAM_MEMBER", "MANAGER", "ADMIN"] },
    { label: "New Report", to: "/reports/new", roles: ["TEAM_MEMBER"] },
    { label: "Report History", to: "/reports/history", roles: ["TEAM_MEMBER"] },
    { label: "Manager Dashboard", to: "/manager/dashboard", roles: ["MANAGER", "ADMIN"] },
    { label: "Manager Reports", to: "/manager/reports", roles: ["MANAGER", "ADMIN"] },
    { label: "Projects", to: "/projects", roles: ["TEAM_MEMBER", "MANAGER", "ADMIN"] },
    { label: "Users", to: "/users", roles: ["ADMIN"] },
];

function NavItemLink({ item, onNavigate }: { item: NavItem; onNavigate: () => void }) {
    const visible = useHasRole(item.roles);
    if (!visible) return null;

    return (
        <NavLink
            to={item.to}
            onClick={onNavigate}
            className={({ isActive }) =>
                `block rounded-md px-3 py-2 text-sm font-medium ${isActive ? "bg-purple-100 text-purple-700" : "text-gray-700 hover:bg-gray-100"
                }`
            }
        >
            {item.label}
        </NavLink>
    );
}

interface Props {
    isOpen: boolean;
    onClose: () => void;
}

export function Sidebar({ isOpen, onClose }: Props) {
    return (
        <>
            {isOpen && (
                <div
                    className="fixed inset-0 z-30 bg-black/40 md:hidden"
                    aria-hidden="true"
                    onClick={onClose}
                />
            )}
            <nav
                className={`fixed inset-y-0 left-0 z-40 w-64 shrink-0 transform border-r border-gray-200 bg-white p-4 transition-transform duration-200 ease-in-out md:static md:z-auto md:w-56 md:translate-x-0 ${isOpen ? "translate-x-0" : "-translate-x-full"
                    }`}
            >
                <div className="flex flex-col gap-1">
                    {NAV_ITEMS.map((item) => (
                        <NavItemLink key={item.to} item={item} onNavigate={onClose} />
                    ))}
                </div>
            </nav>
        </>
    );
}
