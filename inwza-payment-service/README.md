# inwza-payment-service

Orchestrates buy/sell sagas via Kafka (`player`, `inventory`) and consumes replies on `payment`.

## Prerequisites

- Java 21, Maven
- `inwza-api-contracts` installed locally (`mvn install`)
- `inwza-dev` stack (Postgres `inwza_payment`, Kafka)
- **item**, **player**, and **inventory** services running with their Kafka consumers

## Run (local)

```bash
cp .env.example .env   # optional; defaults match inwza-dev
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

HTTP `:8085` — `GET /payment_v1`, `POST /payment_v1/payment/buy`, `POST /payment_v1/payment/sell` (Bearer access token).

## Docs

See [docs/SAGA.md](docs/SAGA.md) for workflow steps and compensation.

## Docker

From ecosystem root (`inwza/`):

```bash
docker build -f inwza-payment-service/Dockerfile -t inwza-payment-service:local .
```
