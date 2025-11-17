-- liquibase formatted sql

-- changeset aravindh:0004-alter-show-seats

ALTER TABLE show_seats
ADD COLUMN version INT NOT NULL DEFAULT 0;

--rollback ALTER TABLE show_seats DROP COLUMN version;