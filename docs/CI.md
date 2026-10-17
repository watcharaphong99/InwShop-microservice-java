# CI and Docker

## Monorepo (all folders under `inwza/` in one GitHub repo)

Workflow: [`.github/workflows/ci.yml`](../.github/workflows/ci.yml)

| Job | What it does |
|-----|----------------|
| `contracts` | `mvn install` on `inwza-api-contracts` |
| `test-services` | Matrix: `mvn test` on each `inwza-*-service` |
| `e2e` | `mvn verify` in `inwza-e2e-tests` (Testcontainers) |
| `docker` | On push to `main`: build & push images to GHCR |

Images: `ghcr.io/<owner>/inwza-<service>-service:latest`

## Split repos (5 apps + contracts)

Each service folder includes [`.github/workflows/ci.yml`](../inwza-auth-service/.github/workflows/ci.yml):

1. Checkout this repo
2. Checkout `inwza-api-contracts` (repo variable `INWZA_CONTRACTS_REPOSITORY` optional)
3. `mvn install` contracts → `mvn test` here

Build Docker images from a **full monorepo checkout** (see below), or run the monorepo workflow.

## Docker build (local)

From the **ecosystem root** (`inwza/`):

```bash
docker build -f inwza-item-service/Dockerfile -t inwza-item-service:local .
docker build -f inwza-payment-service/Dockerfile -t inwza-payment-service:local .
```

Runtime expects env vars from [STANDARDS.md](./STANDARDS.md) / `.env.example` and infrastructure from [inwza-dev](../inwza-dev/).

## Payment saga doc

[inwza-payment-service/docs/SAGA.md](../inwza-payment-service/docs/SAGA.md)
