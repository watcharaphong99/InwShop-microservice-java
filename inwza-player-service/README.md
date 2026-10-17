# inwza-player-service

Player profiles, wallets, Kafka consumer on topic `player`, gRPC for auth.

## Prerequisites

- `inwza-api-contracts` installed locally
- `inwza-dev` (Postgres, Redis, Kafka)

## Run

```bash
mvn spring-boot:run
```

- HTTP: http://localhost:8083/player_v1
- gRPC: localhost:50053 (metadata `auth` = API key JWT, same secret as auth service)

## Seed (profile `local`)

Users `player001@Iwpshop.com` … `admin001@Iwpshop.com`, password `123456`, balance 1000.

## Kafka

| Key | Action |
|-----|--------|
| `buy` | Deduct money → reply topic `payment` key `buy` |
| `sell` | Credit money → reply `payment` / `sell` |
| `rtransaction` | Rollback by `event_id` or `transaction_id` |

## Auth integration

Point auth service to `GRPC_PLAYER_ADDRESS=static://localhost:50053` and use the same `JWT_API_SECRET_KEY`.
