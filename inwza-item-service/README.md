# inwza-item-service

Catalog HTTP API + Item gRPC (`FindItemsInIds`).

## Prerequisites

- Java 21, Maven
- `inwza-api-contracts` installed: `cd ../inwza-api-contracts && mvn install`
- PostgreSQL database `inwza_item` (see `../inwza-dev`)

## Run

```bash
mvn spring-boot:run
```

- HTTP: http://localhost:8082/item_v1
- gRPC: localhost:50052

## Tests

```bash
mvn test
```

Uses Testcontainers PostgreSQL (Docker required for tests).
