# inwza-inventory-service

Player inventory (PostgreSQL), Kafka consumer on `inventory`, gRPC catalog via Item service.

## Run

```bash
mvn spring-boot:run
```

- HTTP: http://localhost:8084/inventory_v1
- gRPC: localhost:50054
- Requires **item-service** at `GRPC_ITEM_ADDRESS` for listing inventory with item details

## Kafka

| Key | Action |
|-----|--------|
| `buy` | Add item → reply `payment` / `buy` |
| `sell` | Soft-remove item → reply `payment` / `sell` |
| `radd` | Rollback add |
| `rremove` | Rollback remove |

## HTTP

`GET /inventory_v1/inventory/{player_id}?limit=2` — JWT required; path `player_id` must match token.
