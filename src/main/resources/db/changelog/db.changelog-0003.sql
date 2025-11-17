-- liquibase formatted sql

-- changeset aravindh:0003-alter-seats

ALTER TABLE seats
ADD COLUMN screen_id BIGINT NOT NULL;

ALTER TABLE seats
ADD CONSTRAINT fk_screen_id
FOREIGN KEY (screen_id)
REFERENCES screens(id);

--rollback ALTER TABLE seats DROP CONSTRAINT fk_screen_id;
--rollback ALTER TABLE seats DROP COLUMN screen_id;