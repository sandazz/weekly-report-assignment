import { createBrowserRouter, Navigate } from "react-router-dom";

import { MainLayout } from "../layouts/MainLayout";
import { DashboardPage } from "../pages/DashboardPage";
import { ForbiddenPage } from "../pages/ForbiddenPage";
import { NotFoundPage } from "../pages/NotFoundPage";
import { ProjectsPage } from "../pages/ProjectsPage";
import { UsersPage } from "../pages/UsersPage";
import { LoginPage } from "../pages/auth/LoginPage";
import { RegisterPage } from "../pages/auth/RegisterPage";
import { ManagerDashboardPage } from "../pages/manager/ManagerDashboardPage";
import { ManagerReviewPage } from "../pages/manager/ManagerReviewPage";
import { NewReportPage } from "../pages/reports/NewReportPage";
import { ReportDetailPage } from "../pages/reports/ReportDetailPage";
import { ReportHistoryPage } from "../pages/reports/ReportHistoryPage";
import { ProtectedRoute } from "./ProtectedRoute";
import { RoleProtectedRoute } from "./RoleProtectedRoute";

export const router = createBrowserRouter([
    { path: "/login", element: <LoginPage /> },
    { path: "/register", element: <RegisterPage /> },
    { path: "/forbidden", element: <ForbiddenPage /> },
    {
        element: <ProtectedRoute />,
        children: [
            {
                element: <MainLayout />,
                children: [
                    { index: true, element: <Navigate to="/dashboard" replace /> },
                    { path: "dashboard", element: <DashboardPage /> },
                    { path: "projects", element: <ProjectsPage /> },
                    { path: "reports/:id", element: <ReportDetailPage /> },
                    {
                        element: <RoleProtectedRoute allowedRoles={["TEAM_MEMBER"]} />,
                        children: [
                            { path: "reports/new", element: <NewReportPage /> },
                            { path: "reports/history", element: <ReportHistoryPage /> },
                        ],
                    },
                    {
                        element: <RoleProtectedRoute allowedRoles={["MANAGER", "ADMIN"]} />,
                        children: [
                            { path: "manager/dashboard", element: <ManagerDashboardPage /> },
                            { path: "manager/reports/:id/review", element: <ManagerReviewPage /> },
                        ],
                    },
                    {
                        element: <RoleProtectedRoute allowedRoles={["ADMIN"]} />,
                        children: [{ path: "users", element: <UsersPage /> }],
                    },
                ],
            },
        ],
    },
    { path: "*", element: <NotFoundPage /> },
]);
