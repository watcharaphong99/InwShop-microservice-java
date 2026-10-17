# Run Inwza in IntelliJ IDEA

## 1. Open project

1. **File → Open** → เลือกโฟลเดอร์ `inwza` (root ที่มี `pom.xml` aggregator)
2. อย่าเปิดแค่ `inwza-item-service` — จะไม่เห็น run config ร่วมและ module อื่น
3. รอ Maven import จบ → ตั้ง **Project SDK = Java 21**  
   **File → Project Structure → Project → SDK**

## 2. PostgreSQL (16 / 17 / pgAdmin ได้หมด)

Inwza ใช้ **instance เดียว** — แยกแค่ชื่อ database (`inwza_item`, …) ไม่ผูก major version ( **PostgreSQL 17 ใช้ได้** )

### เช็คพอร์ตของ server ที่จะใช้

ใน **pgAdmin** → คลิกขวา server (เช่น **PostgreSQL 17**) → **Properties → Connection** → ดู **Port**  
ใส่ค่านี้ใน run config ทุก service ที่ env **`DB_PORT`** (ใน `.run/` ตั้ง **`5433`** สำหรับ PG 17 คู่กับ PG 16 ที่ 5432 — แก้ถ้า port จริงไม่ตรง)

### Migrate (Flyway) — ไม่ต้องรันใน pgAdmin

สร้าง database เปล่าใน pgAdmin **พอแล้ว** — **ยังไม่มี table เป็นเรื่องปกติ**

เมื่อรัน Spring Boot service ครั้งแรก (ต่อ DB ด้วย `inwza` ได้แล้ว) **Flyway จะ migrate ให้อัตโนมัติ** จาก `src/main/resources/db/migration/` เช่น Item: `V1__create_items.sql`, `V2__seed_items.sql`

ใน log จะเห็นประมาณ `Migrating schema ... to version "1"`. ใน pgAdmin เปิด `inwza_item` → **Schemas → public → Tables** หลัง Item service ขึ้นสำเร็จ

แต่ละ service migrate **คนละ database** — รัน Item แล้ว migrate แค่ `inwza_item`; Auth รันแล้ว migrate `inwza_auth` ฯลฯ

### สร้าง user + database (ครั้งเดียว)

**ทาง pgAdmin (แนะนำถ้า 16 ลืมรหัส แต่ 17 connect ได้):**

1. ขยาย **PostgreSQL 17** → คลิกขวา database **`postgres`** → **Query Tool**
2. เปิดไฟล์ **`inwza-dev/postgres/pgadmin-setup.sql`** → **Execute** (F5)  
   (สร้าง login **`inwza` / `inwza`** + database ครบ — แค่สร้าง `inwza_auth` อย่างเดียวใน pgAdmin **ไม่พอ** สำหรับ Item service)
3. Refresh **Databases** — ต้องมี **`inwza_item`** (Item), `inwza_auth` (Auth), …

**ทาง psql** (เปลี่ยน `-p` ให้ตรง server):

```bash
psql -U postgres -h localhost -p <PORT ของ PG 17> \
  -f inwza-dev/postgres/init-local-postgres.sql
```

ถ้ามีทั้ง PG 16 และ 17 รันพร้อมกัน มักคนละพอร์ต — อย่าเดา 5432 ให้ดูจาก pgAdmin

ถ้ามี Docker `inwza-postgres` บน 5434 แล้วไม่ใช้แล้ว:

```bash
docker stop inwza-postgres && docker rm inwza-postgres
```

## 3. Redis + Kafka (auth / player / inventory / payment)

```bash
cd inwza-dev && docker compose up -d redis kafka
```

(item service ไม่ต้องใช้ Redis/Kafka)

## 4. Build API contracts (ครั้งแรก / หลังแก้ `.proto`)

ใช้ run config **`api-contracts (mvn install)`** หรือ:

```bash
cd inwza-api-contracts && mvn install -DskipTests
```

## 5. Run configurations

โฟลเดอร์ [`.run/`](../.run/) มี Spring Boot config ต่อ service — env ตรง `.env.example` (profile `local` อยู่แล้วใน `application.yml`)

| Config | HTTP | หมายเหตุ |
|--------|------|----------|
| Item Service | 8082 | รันก่อน / คู่กับ inventory, payment |
| Auth Service | 8081 | ต้อง Redis + player gRPC ถ้า login เต็ม |
| Player Service | 8083 | Kafka + Redis |
| Inventory Service | 8084 | Kafka + item gRPC |
| Payment Service | 8085 | Kafka + item gRPC |

ลำ run แนะนำ: **contracts → item → player → auth → inventory → payment**

Compound **`Inwza (HTTP stack)`** สตาร์ททุก service พร้อมกัน — ใช้เมื่อ Docker พร้อมแล้ว

## 6. ทดสอบเร็ว

- Item: http://localhost:8082/item_v1  
- Auth health: http://localhost:8081/actuator/health  

## Troubleshooting

| อาการ | แก้ |
|--------|-----|
| `password authentication failed for user "inwza"` | ยังไม่รัน `init-local-postgres.sql` หรือชี้ผิด port |
| `Could not find artifact inwza-api-contracts` | รัน `api-contracts (mvn install)` |
| พอร์ตชน | ปิด process เก่า หรือเช็ค `SERVER_PORT` ใน run config |
| **pom.xml / plugin สีแดง** (`os.detected.classifier`, protobuf) | มี `.mvn/extensions.xml` แล้ว → **Maven tool window → Reload All Maven Projects**; ตั้ง **Project SDK = 21** (ไม่ใช้ 24) |
| import `com.inwza.contracts.*` แดง | รัน **`api-contracts (mvn install)`** หรือ `mvn -pl inwza-api-contracts compile` แล้ว reload Maven |

Env เพิ่มเติม: copy `.env.example` → `.env` ในแต่ละ service แล้วใส่ใน run config เอง หรือติดตั้ง plugin **EnvFile** (optional)
