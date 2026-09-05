import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";

import { Button } from "../../components/Button";
import { Card } from "../../components/Card";
import { Input } from "../../components/Input";
import { useAuth } from "../../hooks/useAuth";
import type { NormalizedError } from "../../services/apiClient";

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function LoginPage() {
    const { login } = useAuth();
    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [fieldErrors, setFieldErrors] = useState<{ email?: string; password?: string }>({});
    const [formError, setFormError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    function validate(): boolean {
        const errors: { email?: string; password?: string } = {};
        if (!email) errors.email = "Email is required";
        else if (!EMAIL_PATTERN.test(email)) errors.email = "Enter a valid email address";
        if (!password) errors.password = "Password is required";
        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    }

    async function handleSubmit(e: FormEvent) {
        e.preventDefault();
        setFormError(null);
        if (!validate()) return;

        setIsSubmitting(true);
        try {
            await login(email, password);
            navigate("/dashboard", { replace: true });
        } catch (err) {
            const normalized = err as NormalizedError;
            setFormError(normalized.message ?? "Login failed");
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <div className="flex min-h-screen items-center justify-center bg-gray-50 px-4">
            <Card className="w-full max-w-sm">
                <h1 className="mb-6 text-xl font-semibold text-gray-900">Log in</h1>
                <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                    <Input
                        id="email"
                        label="Email"
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        error={fieldErrors.email}
                    />
                    <Input
                        id="password"
                        label="Password"
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        error={fieldErrors.password}
                    />
                    {formError && <p className="text-sm text-red-600">{formError}</p>}
                    <Button type="submit" disabled={isSubmitting}>
                        {isSubmitting ? "Logging in..." : "Log in"}
                    </Button>
                </form>
                <p className="mt-4 text-sm text-gray-600">
                    No account? <Link to="/register" className="text-purple-600 hover:underline">Register</Link>
                </p>
            </Card>
        </div>
    );
}
