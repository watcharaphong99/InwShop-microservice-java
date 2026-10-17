# Inwza microservices — shared standards

Applies to all Spring Boot services under this folder. Goal: match Go `Inwza-shop` behaviour where it matters for clients and ops.

## HTTP error JSON

All error responses use a single field:

```json
{ "message": "error: something went wrong" }
```

| Situation | HTTP status | Message style |
|-----------|-------------|---------------|
| Validation (`@Valid`) | 400 | `field: constraint` (first error) |
| Domain / business failure | 400 (or 404/409 where defined) | `error: ...` |
| Missing/invalid Bearer token | 401 | `error: access token is required` or `error: token is invalid` |
| Auth login failure | 401 | `error: ...` |

Success payloads stay resource-specific (arrays, objects). Only **errors** use `MessageResponse`.

## JWT (HS256)

Aligned with Go `pkg/jwtauth`:

| Token | `sub` claim | Signing secret env |
|-------|-------------|-------------------|
| Access | `access-token` | `JWT_ACCESS_SECRET_KEY` |
| Refresh | `refresh-token` | `JWT_REFRESH_SECRET_KEY` (auth only) |
| gRPC metadata `auth` | `api-key` | `JWT_API_SECRET_KEY` |

Common claims: `iss` = `Iwpza.com`, `aud` = `Iwpza.com`, `player_id`, `role_code` (integer).

- Secrets must be **≥ 32 characters** for HS256.
- **Local dev:** use the same access/api secrets on auth, player, inventory, and payment so one login works everywhere.

### HTTP authorization

```
Authorization: Bearer <access-token>
```

`player_id` for protected routes comes from the token, **not** from the request body.

### gRPC service-to-service

Metadata key **`auth`**: value = JWT with `sub: api-key` (long-lived; built from `JWT_API_SECRET_KEY`).

## Correlation ID

| Item | Value |
|------|--------|
| Request header | `X-Correlation-Id` (optional; client may send) |
| Response header | Same value echoed |
| Generated if missing | UUID |
| Logs | MDC key `correlationId` |
| Request attribute | `correlation_id` (for handlers if needed) |

Use the correlation id when tracing buy/sell across payment → Kafka → player/inventory.

## Environment variables

Each service repo has `.env.example`. Shared blocks:

```bash
# HTTP
SERVER_PORT=...

# PostgreSQL (inwza-dev)
DB_HOST=localhost
DB_PORT=5433
DB_NAME=inwza_<service>
DB_USER=postgres
DB_PASSWORD=root

# JWT (local: same values across services)
JWT_ACCESS_SECRET_KEY=01234567890123456789012345678901
JWT_API_SECRET_KEY=01234567890123456789012345678903
```

Auth additionally: `JWT_REFRESH_SECRET_KEY`, `JWT_ACCESS_DURATION`, `JWT_REFRESH_DURATION`, Redis, `GRPC_PLAYER_ADDRESS`.

Kafka services: `KAFKA_BOOTSTRAP_SERVERS=localhost:9092`.

gRPC clients: `GRPC_*_ADDRESS=static://localhost:5005x`.

Copy `.env.example` → `.env` only for local overrides; defaults in `application.yml` match `inwza-dev`.

## Service ports (local)

| Service | HTTP | gRPC |
|---------|------|------|
| auth | 8081 | 50051 |
| item | 8082 | 50052 |
| player | 8083 | 50053 |
| inventory | 8084 | 50054 |
| payment | 8085 | — |

## API path prefix

`<domain>_v1/...` (e.g. `/auth_v1/auth/login`, `/payment_v1/payment/buy`).

## E2E tests

Full buy/sell flow: [inwza-e2e-tests](../inwza-e2e-tests/README.md) (`mvn verify`). Requires Docker. Skip with `-De2e.skip=true`.

Each service JAR for E2E is repackaged with classifier **`boot`** (`*-boot.jar`); the default artifact remains the thin library jar.
