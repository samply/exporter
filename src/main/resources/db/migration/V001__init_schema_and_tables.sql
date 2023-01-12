CREATE SCHEMA IF NOT EXISTS samply;

SET
search_path TO samply;

CREATE TABLE samply.query
(
    id              SERIAL NOT NULL PRIMARY KEY,
    query           TEXT,
    format          TEXT,
    label           TEXT,
    description     TEXT,
    contact_id      TEXT,
    expiration_date DATE,
    created_at      TIMESTAMP,
    archived_at     TIMESTAMP
);

CREATE TABLE samply.query_execution
(
    id          SERIAL NOT NULL PRIMARY KEY,
    query_id    SERIAL,
    status      TEXT,
    executed_at TIMESTAMP
);

CREATE INDEX idx_query_execution_query_id ON samply.query_execution (query_id);

CREATE TABLE samply.query_execution_file
(
    id                 SERIAL NOT NULL PRIMARY KEY,
    query_execution_id SERIAL,
    file_path          TEXT
);

CREATE INDEX idx_query_execution_file_query_execution_id ON samply.query_execution_file (query_execution_id);

ALTER TABLE samply.query_execution
    ADD CONSTRAINT query_execution_query_id_fkey FOREIGN KEY (query_id) REFERENCES samply.query (id) ON DELETE CASCADE;
ALTER TABLE samply.query_execution_file
    ADD CONSTRAINT query_execution_file_query_execution_id_fkey FOREIGN KEY (query_execution_id) REFERENCES samply.query_execution (id) ON DELETE CASCADE;
