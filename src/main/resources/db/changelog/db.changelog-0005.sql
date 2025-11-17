-- liquibase formatted sql

-- changeset aravindh:0005-alter-payments

ALTER TABLE payments
DROP CONSTRAINT IF EXISTS payments_status_check;

ALTER TABLE payments
ADD CONSTRAINT payments_status_check
CHECK (status IN ('PENDING', 'SUCCESS', 'REFUNDED', 'FAILED'));

--rollback ALTER TABLE payments DROP CONSTRAINT payments_status_check;
--rollback ALTER TABLE payments ADD CONSTRAINT payments_status_check CHECK (status IN ('PENDING', 'CONFIRMED', 'REFUNDED'));