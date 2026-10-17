# Payment SAGA (buy / sell)

Payment orchestrates **Player** and **Inventory** over Kafka and resolves catalog prices from **Item** (gRPC). Replies arrive on topic `payment`; each HTTP request registers waiters keyed by `event_id` (8s step timeout).

## Persistence (PostgreSQL)

| Table | Purpose |
|-------|---------|
| `order_workflows` | Running/completed/failed workflows + `steps` JSONB |
| `kafka_consumer_offset` | Last processed offset on topic `payment` |

Stale workflows (`status = running`, `updated_at` older than 45s) are compensated on startup and every 60s.

## Buy (`POST /payment_v1/payment/buy`)

1. **Pricing** — `FindItemsInIds` (gRPC); client `price` is overwritten.
2. **Per line — capture payment** — Kafka `player` / key `buy` → wait reply on `payment`.
3. **Per paid line — fulfill** — Kafka `inventory` / key `buy` → wait reply.
4. On failure — compensate completed steps (`rtransaction`, `radd`).

## Sell (`POST /payment_v1/payment/sell`)

1. **Pricing** — same as buy.
2. **Per line — remove item** — Kafka `inventory` / key `sell`.
3. **Per removed line — add money** — payout = `round(price * 50%)`; Kafka `player` / key `sell`.
4. On failure — compensate (`rtransaction`, `rremove`).

## Kafka keys (payment → others)

| Action | Topic | Key |
|--------|-------|-----|
| Capture payment | `player` | `buy` |
| Add money (sell) | `player` | `sell` |
| Rollback transaction | `player` | `rtransaction` |
| Add item | `inventory` | `buy` |
| Remove item | `inventory` | `sell` |
| Rollback add | `inventory` | `radd` |
| Rollback remove | `inventory` | `rremove` |

Reply payload: `PaymentTransferMessage` (`event_id`, `transaction_id`, `inventory_id`, `player_id`, `item_id`, `amount`, `error`).
