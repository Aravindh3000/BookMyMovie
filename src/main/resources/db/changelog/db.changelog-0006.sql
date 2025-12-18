-- liquibase formatted sql
-- changeset aravindh:0006-add-canceled-status-to-bookings
ALTER TABLE bookings
    DROP CONSTRAINT bookings_status_check;

ALTER TABLE bookings
    ADD CONSTRAINT bookings_status_check
    CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED'));

-- rollback ALTER TABLE bookings DROP CONSTRAINT bookings_status_check;
-- rollback ALTER TABLE bookings ADD CONSTRAINT bookings_status_check CHECK (status IN ('PENDING', 'CONFIRMED'));
