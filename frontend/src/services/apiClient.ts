import axios from "axios";

import { router } from "../routes/router";
import { getStoredToken, clearSession } from "../utils/authStorage";
import { triggerLogout } from "../utils/authBridge";

export interface NormalizedError {
    status: number;
    message: string;
}

const apiClient = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api",
    headers: {
        "Content-Type": "application/json",
    },
});

apiClient.interceptors.request.use((config) => {
    const token = getStoredToken();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        const status: number | undefined = error.response?.status;

        if (status === 401) {
            // authBridge may not be registered yet (e.g. error during initial load) - clear directly too.
            triggerLogout();
            clearSession();
            router.navigate("/login", { replace: true });
            return Promise.reject({ status, message: "Your session has expired. Please log in again." });
        }

        if (status === 403) {
            return Promise.reject(error);
        }

        const backendMessage = error.response?.data?.message;
        const normalized: NormalizedError = {
            status: status ?? 0,
            message: backendMessage ?? error.message ?? "An unexpected error occurred",
        };
        return Promise.reject(normalized);
    },
);

export default apiClient;
