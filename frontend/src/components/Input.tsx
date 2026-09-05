import type { InputHTMLAttributes } from "react";

type Props = InputHTMLAttributes<HTMLInputElement> & {
    label: string;
    error?: string;
};

export function Input({ label, error, id, className = "", ...props }: Props) {
    return (
        <div className="flex flex-col gap-1">
            <label htmlFor={id} className="text-sm font-medium text-gray-700">
                {label}
            </label>
            <input
                id={id}
                className={`rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-purple-500 ${error ? "border-red-500" : "border-gray-300"
                    } ${className}`}
                {...props}
            />
            {error && <span className="text-xs text-red-600">{error}</span>}
        </div>
    );
}
