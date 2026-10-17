# Mock data ผ่าน pgAdmin (ไม่ต้องใช้ IntelliJ)

ใช้กับ **PostgreSQL 17** (port **5433** บนเครื่องคุณ) หลังมี database + user `inwza` แล้ว (`pgadmin-setup.sql`)

## วิธีรัน

1. pgAdmin → เลือก database (เช่น **`inwza_item`**) → **Query Tool**
2. เปิดไฟล์ `bootstrap-inwza_<name>.sql` ที่ตรงกับ database → **Execute (F5)**
3. ทำซ้ำทุก DB ที่ต้องการ mock

| ไฟล์ | Database |
|------|----------|
| `bootstrap-inwza_item.sql` | `inwza_item` |
| `bootstrap-inwza_auth.sql` | `inwza_auth` |
| `bootstrap-inwza_player.sql` | `inwza_player` |
| `bootstrap-inwza_inventory.sql` | `inwza_inventory` |
| `bootstrap-inwza_payment.sql` | `inwza_payment` |

## ข้อมูลทดสอบ (หลัง bootstrap player)

| Email | Password | Wallet |
|-------|----------|--------|
| `player001@Iwpshop.com` | `123456` | 1000 |
| `player002@Iwpshop.com` | `123456` | 1000 |
| `player003@Iwpshop.com` | `123456` | 1000 |
| `admin001@Iwpshop.com` | `123456` | 1000 (roles: player + admin) |

Player id ตัวอย่าง: `player:33333333-3333-4333-8333-333333333301` (player001)

Item seed ตรงกับ Flyway / E2E: Diamond / Iron / Wooden Sword (Wooden = `item:11111111-1111-4111-8111-111111111103`)

## คำเตือน

- สคริปต์ `bootstrap-*` สร้าง **table + ข้อมูล** เหมือนตอน Flyway รันครั้งแรก  
- ถ้ารัน bootstrap แล้วค่อยรัน Spring Boot บน DB เดิม Flyway อาจฟ้องว่า table มีแล้ว — ใช้ DB เปล่าแล้วเลือก **อย่างใดอย่างหนึ่ง**: bootstrap มือ **หรือ** ให้ service migrate เอง
