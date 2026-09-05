import { useNavigate } from "react-router-dom";

import { useAuth } from "../hooks/useAuth";
import { Button } from "../components/Button";

export function Header() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    function handleLogout() {
        logout();
        navigate("/login", { replace: true });
    }

    return (
        <header className="flex items-center justify-between border-b border-gray-200 bg-white px-6 py-4">
            <span className="text-lg font-semibold text-gray-900">Weekly Report System</span>
            <div className="flex items-center gap-4">
                {user && (
                    <span className="text-sm text-gray-600">
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
