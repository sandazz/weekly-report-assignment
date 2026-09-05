export interface Project {
    id: number;
    name: string;
    description: string | null;
    active: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface ProjectFormValues {
    name: string;
    description: string;
}
