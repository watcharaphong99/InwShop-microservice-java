# inwza-e2e-tests

End-to-end buy/sell flow across all five Spring services (Testcontainers + bootable JARs).

## Prerequisites

- Docker (Testcontainers)
- Java 21, Maven
- `inwza-api-contracts` installed: `mvn -f ../inwza-api-contracts install`

## Run

```bash
cd inwza-e2e-tests
mvn verify
```

This packages sibling services, starts Postgres/Kafka/Redis, launches services on ports `18081–18085`, then runs `BuySellE2EIT`:

1. Login `player001@Iwpshop.com` / `123456`
2. Buy Wooden Sword (`item:11111111-…-103`, price 100)
3. Assert wallet −100 and inventory has item
4. Sell same item (50% payout)
5. Assert wallet −50 net vs start and inventory empty

Skip E2E (CI unit-test only):

```bash
mvn verify -De2e.skip=true
```

Service logs on failure: temp files printed at startup (`Started inwza-*-service pid=… log=…`).
