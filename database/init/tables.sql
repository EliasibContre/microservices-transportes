\connect order_db

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('CREATED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED')),
    origin TEXT NOT NULL,
    destination TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

\connect driver_db

CREATE TABLE drivers (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    license_number TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

\connect assignment_db

CREATE TABLE assignments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    driver_id UUID NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE assignment_files (
    id UUID PRIMARY KEY,
    assignment_id UUID NOT NULL REFERENCES assignments(id),
    file_name TEXT NOT NULL,
    content_type TEXT NOT NULL
        CHECK (content_type IN ('application/pdf', 'image/png', 'image/jpeg')),
    content BYTEA NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);