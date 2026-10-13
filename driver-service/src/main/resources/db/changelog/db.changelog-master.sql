--liquibase formatted sql
--changeset eliasib:001-create-drivers

CREATE TABLE drivers (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    license_number TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);