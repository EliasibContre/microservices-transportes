--liquibase formatted sql
--changeset eliasib:001-create-orders

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('CREATED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED')),
    origin TEXT NOT NULL,
    destination TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);