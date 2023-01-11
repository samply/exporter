CREATE SCHEMA IF NOT EXISTS samply;

CREATE TABLE samply.query
(
    id              SERIAL NOT NULL PRIMARY KEY,
    query           text,
    format          text,
    label           text,
    description     text,
    contact_id      text,
    expiration_date date,
    created_at      timestamp,
    archived_at     timestamp
);
