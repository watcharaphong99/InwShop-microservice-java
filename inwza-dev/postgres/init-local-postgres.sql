-- Run once against your local PostgreSQL on port 5432 (superuser), e.g.:
--   /Library/PostgreSQL/16/bin/psql -U postgres -h localhost -f init-local-postgres.sql
--
-- Same databases as docker init-databases.sql; creates role inwza/inwza if missing.

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'inwza') THEN
        CREATE ROLE inwza LOGIN PASSWORD 'inwza';
    END IF;
END
$$;

ALTER ROLE inwza WITH LOGIN PASSWORD 'inwza';

SELECT 'CREATE DATABASE ' || quote_ident(datname) || ' OWNER inwza'
FROM (VALUES
    ('inwza_auth'),
    ('inwza_item'),
    ('inwza_player'),
    ('inwza_inventory'),
    ('inwza_payment')
) AS d(datname)
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = d.datname)
\gexec

GRANT ALL PRIVILEGES ON DATABASE inwza_auth TO inwza;
GRANT ALL PRIVILEGES ON DATABASE inwza_item TO inwza;
GRANT ALL PRIVILEGES ON DATABASE inwza_player TO inwza;
GRANT ALL PRIVILEGES ON DATABASE inwza_inventory TO inwza;
GRANT ALL PRIVILEGES ON DATABASE inwza_payment TO inwza;
