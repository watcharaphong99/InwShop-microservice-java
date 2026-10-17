# Inwza — Spring Boot microservices

Java ecosystem for the shop game backend. **Separate from** `golang/Inwza-shop`.

## Repositories (folders on disk)

| Folder | Purpose |
|--------|---------|
| [inwza-dev](./inwza-dev/) | Docker: PostgreSQL, Redis, Kafka |
| inwza-api-contracts | gRPC `.proto` + published Maven artifact |
| inwza-auth-service | Auth HTTP + gRPC (**step 4 done**) |
| inwza-item-service | Catalog HTTP + gRPC (**step 3 done**) |
| inwza-player-service | Player + wallet (Kafka) (**step 5 done**) |
| inwza-inventory-service | Inventory (Kafka) (**step 6 done**) |
| inwza-payment-service | Buy/sell saga (**step 7 done**) |
| [inwza-e2e-tests](./inwza-e2e-tests/) | E2E buy/sell (Testcontainers + boot JARs) (**step 9**) |

## Quick start

1. `cd inwza-dev && docker compose up -d`
2. Build services in order: contracts → item → auth → player/inventory → payment

**IntelliJ:** open repo root (aggregator `pom.xml`), PostgreSQL 5432 + [docs/INTELLIJ.md](./docs/INTELLIJ.md), run configs in [`.run/`](./.run/).

## Conventions

Shared HTTP errors, JWT claims, correlation id, and env vars: [docs/STANDARDS.md](./docs/STANDARDS.md).

## CI & Docker

[docs/CI.md](./docs/CI.md) — GitHub Actions (monorepo + per-service), GHCR images, local `docker build`.

## Stack

- Java 21, Spring Boot 3.3+
- PostgreSQL (one DB per service)
- Redis (auth)
- Kafka (player, inventory, payment)
