import { useCallback, useState } from "react";

import { notifyError, notifySuccess } from "../lib/toast";

interface Options {
    successMessage?: string;
    errorFallback?: string;
}

// Wraps an async call with loading state + automatic success/error toasts, so pages
// don't each hand-roll isSaving/try-catch/finally around every write action.
export function useAsyncAction<Args extends unknown[], Result>(
    action: (...args: Args) => Promise<Result>,
    options: Options = {},
) {
    const [isLoading, setIsLoading] = useState(false);

    const run = useCallback(
        async (...args: Args) => {
            setIsLoading(true);
            try {
                const result = await action(...args);
                if (options.successMessage) {
                    notifySuccess(options.successMessage);
                }
                return result;
            } catch (err) {
                notifyError(err, options.errorFallback ?? "Something went wrong");
                throw err;
            } finally {
                setIsLoading(false);
            }
        },
        [action, options.successMessage, options.errorFallback],
    );

    return { run, isLoading };
}
