import toast from "react-hot-toast";

import type { NormalizedError } from "../services/apiClient";

export function notifySuccess(message: string) {
    toast.success(message);
}

// Handles both the normalized {status, message} shape and the raw axios error
// that 403s pass through un-normalized from the apiClient interceptor.
export function notifyError(err: unknown, fallback: string) {
    const normalized = err as Partial<NormalizedError> & {
        response?: { data?: { message?: string } };
        message?: string;
    };
    const message = normalized?.response?.data?.message ?? normalized?.message ?? fallback;
    toast.error(message);
}
