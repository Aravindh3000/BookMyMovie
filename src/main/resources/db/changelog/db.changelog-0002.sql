-- liquibase formatted sql

-- changeset aravindh:0002-update-users

ALTER TABLE users
ALTER COLUMN role TYPE VARCHAR(20);

ALTER TABLE users
ALTER COLUMN role SET NOT NULL;

ALTER TABLE users
ADD CONSTRAINT role_check CHECK (role IN ('USER', 'ADMIN'));

--rollback ALTER TABLE users DROP CONSTRAINT IF EXISTS role_check;
--rollback ALTER TABLE users ALTER COLUMN role DROP NOT NULL;
--rollback ALTER TABLE users ALTER COLUMN role TYPE VARCHAR(32);
--rollback ALTER TABLE users ALTER COLUMN role SET NOT NULL;
