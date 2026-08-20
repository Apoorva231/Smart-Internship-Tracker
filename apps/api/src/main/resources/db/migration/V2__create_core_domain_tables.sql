CREATE TYPE application_status AS ENUM (
    'SAVED',
    'APPLIED',
    'INTERVIEW',
    'TECHNICAL',
    'OFFER',
    'REJECTED',
    'ARCHIVED'
);

CREATE TYPE work_mode AS ENUM (
    'REMOTE',
    'HYBRID',
    'ONSITE'
);

CREATE TABLE users (
    id TEXT PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    city TEXT NOT NULL DEFAULT 'Montreal, QC',
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE companies (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    location TEXT NOT NULL DEFAULT 'Montreal, QC',
    website TEXT,
    industry TEXT NOT NULL DEFAULT 'Technology',
    size TEXT,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT companies_name_location_key UNIQUE (name, location)
);

CREATE TABLE applications (
    id TEXT PRIMARY KEY,
    role TEXT NOT NULL,
    status application_status NOT NULL DEFAULT 'SAVED',
    work_mode work_mode NOT NULL DEFAULT 'HYBRID',
    priority INTEGER NOT NULL DEFAULT 2,
    deadline TIMESTAMP(3),
    job_url TEXT,
    salary_range TEXT,
    contact_name TEXT,
    contact_email TEXT,
    notes TEXT,
    applied_at TIMESTAMP(3),
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id TEXT NOT NULL,
    company_id TEXT NOT NULL,

    CONSTRAINT applications_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT applications_company_id_fkey
        FOREIGN KEY (company_id) REFERENCES companies(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE tasks (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    due_date TIMESTAMP(3),
    completed BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    application_id TEXT NOT NULL,

    CONSTRAINT tasks_application_id_fkey
        FOREIGN KEY (application_id) REFERENCES applications(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX applications_user_id_status_idx ON applications(user_id, status);
CREATE INDEX applications_company_id_idx ON applications(company_id);
