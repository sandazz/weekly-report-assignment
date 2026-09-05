import { useNavigate } from "react-router-dom";

import { useAuth } from "../hooks/useAuth";
import { Button } from "../components/Button";

interface Props {
    onMenuClick: () => void;
}

export function Header({ onMenuClick }: Props) {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    function handleLogout() {
        logout();
        navigate("/login", { replace: true });
    }

    return (
        <header className="flex items-center justify-between border-b border-gray-200 bg-white px-4 py-4 sm:px-6">
            <div className="flex items-center gap-3">
                <button
                    type="button"
                    aria-label="Open menu"
                    onClick={onMenuClick}
                    className="-ml-1 rounded-md p-2 text-gray-600 hover:bg-gray-100 md:hidden"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} className="h-6 w-6">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5M3.75 17.25h16.5" />
                    </svg>
                </button>
                <span className="text-lg font-semibold text-gray-900">Weekly Report System</span>
            </div>
            <div className="flex items-center gap-4">
                {user && (
                    <span className="hidden text-sm text-gray-600 sm:inline">
                        {user.name} <span className="text-gray-400">({user.role})</span>
                    </span>
                )}
                <Button variant="secondary" onClick={handleLogout}>
                    Logout
                </Button>
            </div>
        </header>
    );
}
