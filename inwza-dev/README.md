# inwza-dev

Local infrastructure for Inwza Spring microservices (not tied to the Go project).

## Prerequisites

- Docker Desktop (or Docker Engine + Compose v2)

## Start

```bash
cd inwza-dev
docker compose up -d
```

## Endpoints

| Service    | Host              | Notes                          |
|------------|-------------------|--------------------------------|
| PostgreSQL | `localhost:5432`  | user/pass `inwza` / `inwza`    |
| Redis      | `localhost:6379`  | no password (local only)       |
| Kafka      | `localhost:9092`  | PLAINTEXT, advertised localhost |
| Kafka UI   | http://localhost:9091 | cluster `inwza-local`      |

## JDBC URLs (per service)

```properties
# auth
jdbc:postgresql://localhost:5432/inwza_auth

# item
jdbc:postgresql://localhost:5432/inwza_item

# player
jdbc:postgresql://localhost:5432/inwza_player

# inventory
jdbc:postgresql://localhost:5432/inwza_inventory

# payment
jdbc:postgresql://localhost:5432/inwza_payment
```

## Kafka topics (create when services start, or manually)

- `player`
- `inventory`
- `payment`

## Stop

```bash
docker compose down
```

Remove volumes (reset DB/Kafka data):

```bash
docker compose down -v
```
