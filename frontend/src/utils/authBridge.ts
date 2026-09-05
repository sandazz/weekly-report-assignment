// Bridges axios (outside React) to AuthContext's real logout, so a 401 clears both
// in-memory state and localStorage through one code path instead of two divergent ones.
type LogoutFn = () => void;

const bridge: { logout: LogoutFn | null } = {
    logout: null,
};

export function registerLogout(fn: LogoutFn) {
    bridge.logout = fn;
}

export function triggerLogout() {
    bridge.logout?.();
}
