-- One PostgreSQL instance, one database per microservice (Spring Boot apps).

CREATE DATABASE inwza_auth;
CREATE DATABASE inwza_item;
CREATE DATABASE inwza_player;
CREATE DATABASE inwza_inventory;
CREATE DATABASE inwza_payment;

GRANT ALL PRIVILEGES ON DATABASE inwza_auth TO inwza;
GRANT ALL PRIVILEGES ON DATABASE inwza_item TO inwza;
GRANT ALL PRIVILEGES ON DATABASE inwza_player TO inwza;
GRANT ALL PRIVILEGES ON DATABASE inwza_inventory TO inwza;
GRANT ALL PRIVILEGES ON DATABASE inwza_payment TO inwza;
