-- V1: initial schema for Weekly Report Generator & Team Dashboard

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    week_start DATE NOT NULL,
    week_end DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    next_week_plan TEXT,
    key_blocker TEXT,
    key_achievement TEXT,
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_reports_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_reports_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT uk_report_user_project_week UNIQUE (user_id, project_id, week_start)
);

CREATE TABLE report_tasks (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL,
    task_name VARCHAR(255) NOT NULL,
    priority VARCHAR(20),
    planned_percentage INTEGER,
    actual_percentage INTEGER,
    status VARCHAR(20),
    planned_hours DOUBLE PRECISION,
    spent_hours DOUBLE PRECISION,
    deliverable TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_report_tasks_report FOREIGN KEY (report_id) REFERENCES reports (id)
);

CREATE TABLE report_versions (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL,
    version_number INTEGER NOT NULL,
    next_week_plan TEXT,
    key_blocker TEXT,
    key_achievement TEXT,
    notes TEXT,
    submitted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_report_versions_report FOREIGN KEY (report_id) REFERENCES reports (id),
    CONSTRAINT uk_report_version_number UNIQUE (report_id, version_number)
);

CREATE TABLE report_version_tasks (
    id BIGSERIAL PRIMARY KEY,
    report_version_id BIGINT NOT NULL,
    task_name VARCHAR(255) NOT NULL,
    planned_percentage INTEGER,
    actual_percentage INTEGER,
    status VARCHAR(20),
    planned_hours DOUBLE PRECISION,
    spent_hours DOUBLE PRECISION,
    deliverable TEXT,
    CONSTRAINT fk_report_version_tasks_version FOREIGN KEY (report_version_id) REFERENCES report_versions (id)
);

CREATE TABLE review_histories (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL,
    report_version_id BIGINT,
    reviewer_id BIGINT NOT NULL,
    action VARCHAR(30) NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_review_histories_report FOREIGN KEY (report_id) REFERENCES reports (id),
    CONSTRAINT fk_review_histories_version FOREIGN KEY (report_version_id) REFERENCES report_versions (id),
    CONSTRAINT fk_review_histories_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (id)
);

CREATE TABLE report_blockers (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    is_key_issue BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_report_blockers_report FOREIGN KEY (report_id) REFERENCES reports (id)
);

CREATE TABLE report_achievements (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    is_key_achievement BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_report_achievements_report FOREIGN KEY (report_id) REFERENCES reports (id)
);

CREATE TABLE report_hours (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL,
    task_type VARCHAR(30),
    hours DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_report_hours_report FOREIGN KEY (report_id) REFERENCES reports (id)
);
