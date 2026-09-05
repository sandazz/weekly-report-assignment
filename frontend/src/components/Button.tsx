import type { ButtonHTMLAttributes } from "react";

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
    variant?: "primary" | "secondary";
};

export function Button({ variant = "primary", className = "", ...props }: Props) {
    const base = "rounded-md px-4 py-2 text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed";
    const variants = {
        primary: "bg-purple-600 text-white hover:bg-purple-700",
        secondary: "bg-gray-100 text-gray-900 hover:bg-gray-200",
    };
    return <button className={`${base} ${variants[variant]} ${className}`} {...props} />;
}
