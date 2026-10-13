--liquibase formatted sql
--changeset eliasib:001-create-assignments
CREATE TABLE assignments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    driver_id UUID NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--changeset eliasib:002-create-assignment-files

CREATE TABLE assignment_files (
    id UUID PRIMARY KEY,
    assignment_id UUID NOT NULL REFERENCES assignments(id),
    file_name TEXT NOT NULL,
    content_type TEXT NOT NULL
        CHECK (content_type IN ('application/pdf', 'image/png', 'image/jpeg')),
    content BYTEA NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
