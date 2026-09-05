import { useSearchParams } from "react-router-dom";

import type { ReportStatus } from "../types";

export interface ReportFilterValues {
    status: ReportStatus | "";
    projectId: number | "";
    memberId: number | "";
    fromDate: string;
    toDate: string;
}

const EMPTY_FILTERS: ReportFilterValues = {
    status: "",
    projectId: "",
    memberId: "",
    fromDate: "",
    toDate: "",
};

const DEFAULT_SORT = "weekStart,desc";

export function useReportFilters() {
    const [searchParams, setSearchParams] = useSearchParams();

    const filters: ReportFilterValues = {
        status: (searchParams.get("status") as ReportStatus) || "",
        projectId: searchParams.get("projectId") ? Number(searchParams.get("projectId")) : "",
        memberId: searchParams.get("userId") ? Number(searchParams.get("userId")) : "",
        fromDate: searchParams.get("fromDate") || "",
        toDate: searchParams.get("toDate") || "",
    };
    const page = searchParams.get("page") ? Number(searchParams.get("page")) : 0;
    const sort = searchParams.get("sort") || DEFAULT_SORT;

    function writeParams(next: Partial<ReportFilterValues> & { page?: number; sort?: string }) {
        const merged = { ...filters, page, sort, ...next };
        const params = new URLSearchParams();
        if (merged.status) params.set("status", merged.status);
        if (merged.projectId) params.set("projectId", String(merged.projectId));
        if (merged.memberId) params.set("userId", String(merged.memberId));
        if (merged.fromDate) params.set("fromDate", merged.fromDate);
        if (merged.toDate) params.set("toDate", merged.toDate);
        if (merged.page) params.set("page", String(merged.page));
        if (merged.sort && merged.sort !== DEFAULT_SORT) params.set("sort", merged.sort);
        setSearchParams(params);
    }

    function setFilters(patch: Partial<ReportFilterValues>) {
        // Any filter change starts back at page 0.
        writeParams({ ...patch, page: 0 });
    }

    function setPage(newPage: number) {
        writeParams({ page: newPage });
    }

    function setSort(field: string) {
        const [currentField, currentDir] = sort.split(",");
        const nextDir = currentField === field && currentDir === "asc" ? "desc" : "asc";
        writeParams({ sort: `${field},${nextDir}`, page: 0 });
    }

    function clearAll() {
        setSearchParams({});
    }

    return { filters: filters, defaultFilters: EMPTY_FILTERS, setFilters, page, setPage, sort, setSort, clearAll };
}
