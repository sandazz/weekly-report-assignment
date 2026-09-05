export function PlaceholderPage({ title, description }: { title: string; description: string }) {
    return (
        <div className="p-6">
            <h1 className="text-2xl font-semibold text-gray-900">{title}</h1>
            <p className="mt-2 text-gray-600">{description}</p>
        </div>
    );
}
