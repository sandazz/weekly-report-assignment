import { PlaceholderPage } from "../components/PlaceholderPage";
import { useAuth } from "../hooks/useAuth";

export function DashboardPage() {
    const { user } = useAuth();
    const isManagerOrAdmin = user?.role === "MANAGER" || user?.role === "ADMIN";

    return (
        <PlaceholderPage
            title={isManagerOrAdmin ? "Manager Dashboard" : "Team Member Dashboard"}
            description={isManagerOrAdmin ? "Coming in Phase 8" : "Coming in Phase 7"}
        />
    );
}
