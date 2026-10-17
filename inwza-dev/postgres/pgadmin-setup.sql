-- Run in pgAdmin: PostgreSQL 17 → database "postgres" → Query Tool → Execute (F5)
-- Spring apps connect as user inwza / password inwza (not postgres).

CREATE ROLE inwza WITH LOGIN PASSWORD 'inwza';

CREATE DATABASE inwza_item OWNER inwza;
CREATE DATABASE inwza_auth OWNER inwza;
CREATE DATABASE inwza_player OWNER inwza;
CREATE DATABASE inwza_inventory OWNER inwza;
CREATE DATABASE inwza_payment OWNER inwza;

GRANT ALL PRIVILEGES ON DATABASE inwza_item TO inwza;
GRANT ALL PRIVILEGES ON DATABASE inwza_auth TO inwza;
GRANT ALL PRIVILEGES ON DATABASE inwza_player TO inwza;
GRANT ALL PRIVILEGES ON DATABASE inwza_inventory TO inwza;
GRANT ALL PRIVILEGES ON DATABASE inwza_payment TO inwza;

-- If a database already exists (e.g. you created inwza_auth manually), drop the CREATE line
-- or ignore "already exists" errors and keep the GRANT / CREATE ROLE parts.
