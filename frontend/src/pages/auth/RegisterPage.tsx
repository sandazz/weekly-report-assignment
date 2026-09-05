import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";

import { Button } from "../../components/Button";
import { Card } from "../../components/Card";
import { Input } from "../../components/Input";
import { useAuth } from "../../hooks/useAuth";
import { notifyError } from "../../lib/toast";

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

interface FieldErrors {
    name?: string;
    email?: string;
    password?: string;
    confirmPassword?: string;
}

export function RegisterPage() {
    const { register } = useAuth();
    const navigate = useNavigate();

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
    const [isSubmitting, setIsSubmitting] = useState(false);

    function validate(): boolean {
        const errors: FieldErrors = {};
        if (!name) errors.name = "Name is required";
        if (!email) errors.email = "Email is required";
        else if (!EMAIL_PATTERN.test(email)) errors.email = "Enter a valid email address";
        if (!password) errors.password = "Password is required";
        else if (password.length < 8) errors.password = "Password must be at least 8 characters";
        if (confirmPassword !== password) errors.confirmPassword = "Passwords do not match";
        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    }

    async function handleSubmit(e: FormEvent) {
        e.preventDefault();
        if (!validate()) return;

        setIsSubmitting(true);
        try {
            await register(name, email, password);
            navigate("/dashboard", { replace: true });
        } catch (err) {
            notifyError(err, "Registration failed");
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <div className="flex min-h-screen items-center justify-center bg-gray-50 px-4">
            <Card className="w-full max-w-sm">
                <h1 className="mb-6 text-xl font-semibold text-gray-900">Register</h1>
                <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                    <Input id="name" label="Name" value={name} onChange={(e) => setName(e.target.value)} error={fieldErrors.name} />
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
                    <Input
                        id="confirmPassword"
                        label="Confirm password"
                        type="password"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        error={fieldErrors.confirmPassword}
                    />
                    <Button type="submit" disabled={isSubmitting}>
                        {isSubmitting ? "Registering..." : "Register"}
                    </Button>
                </form>
                <p className="mt-4 text-sm text-gray-600">
                    Already have an account? <Link to="/login" className="text-purple-600 hover:underline">Log in</Link>
                </p>
            </Card>
        </div>
    );
}
