CREATE SCHEMA IF NOT EXISTS samply;

CREATE TABLE samply.query
(
    id              SERIAL PRIMARY KEY,
    query           text,
    format          text,
    label           text,
    description     text,
    contact_id      text,
    expiration_date date,
    archived_at     timestamp
);
