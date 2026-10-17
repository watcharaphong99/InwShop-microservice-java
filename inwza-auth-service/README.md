# inwza-auth-service

Login, refresh, logout + gRPC (`AccessTokenSearch`, `RolesCount`).

## Dependencies

- PostgreSQL `inwza_auth`
- Redis (access-token cache, roles count)
- **Player gRPC** (`GRPC_PLAYER_ADDRESS`) for credential check — available after `inwza-player-service` (step 5)

## Build

```bash
cd ../inwza-api-contracts && mvn install
cd ../inwza-auth-service
mvn test
mvn spring-boot:run
```

## HTTP

| Method | Path |
|--------|------|
| GET | `/auth_v1` |
| POST | `/auth_v1/auth/login` |
| POST | `/auth_v1/auth/refresh-token` |
| POST | `/auth_v1/auth/logout` |

## gRPC

- Port `50051` (default)
- Metadata header `auth`: API-key JWT (same as Go `jwtauth.SetApiKeyInContext`)
