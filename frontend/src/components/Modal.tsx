import type { ReactNode } from "react";

import { Card } from "./Card";

interface Props {
    title: string;
    children: ReactNode;
    onClose: () => void;
}

export function Modal({ title, children, onClose }: Props) {
    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
            <Card className="w-full max-w-lg">
                <div className="flex items-center justify-between">
                    <h2 className="text-lg font-semibold text-gray-900">{title}</h2>
                    <button
                        type="button"
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600"
                        aria-label="Close"
                    >
                        ✕
                    </button>
                </div>
                <div className="mt-4">{children}</div>
            </Card>
        </div>
    );
}
