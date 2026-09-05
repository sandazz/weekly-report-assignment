// Mirrors the backend's Monday-boundary "current week" logic (DashboardService.currentWeekMonday).
export function getCurrentWeekMonday(): string {
    const now = new Date();
    const day = now.getDay(); // 0 = Sunday, 1 = Monday, ...
    const diffToMonday = day === 0 ? 6 : day - 1;
    const monday = new Date(now);
    monday.setDate(now.getDate() - diffToMonday);
    return toIsoDate(monday);
}

export function addDays(isoDate: string, days: number): string {
    const date = new Date(isoDate);
    date.setDate(date.getDate() + days);
    return toIsoDate(date);
}

export function toIsoDate(date: Date): string {
    return date.toISOString().slice(0, 10);
}
