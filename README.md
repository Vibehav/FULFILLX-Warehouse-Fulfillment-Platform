# FulfillX - Warehouse Fulfillment Platform
FulfillX is a B2B warehouse fulfillment platform where vendors supply stock, warehouses store and manage it, and sellers place bulk orders which are picked, packed, shipped, and tracked, all across multiple tenants on a single distributed system.

---
## Physical Flow

```
Vendor sends Truck → Inbound Operator Scans & Receives Stock (GRN Created)
        ↓
Catalogue SKU referenced → Inventory Updated via RabbitMQ


Seller places Bulk Order
        ↓
Inventory Reserves Quantity  / If Quantity Exceeds
        ↓                               ↓
        ↓                     Insufficient Stock Order failed
        ↓
        ↓
Order Confirmed → Courier Allocated → Shipped
        ↓
Delivered → Order Closed
```
### Tech Stack
##### Backend
* Java 21
* Spring Boot, Spring Security, Spring Data JPA
* Hibernate

##### Architecture
- Microservices
- REST APIs
- Event Driven Communication (RabbitMQ)
- Dead Letter Exchange (DLX) + Dead Letter Queue (DLQ)
- Quorum Queues

##### Infrastructure

- Docker Compose
- PostgreSQL ( Database per service)
- Redis (Token blacklisting + Stock caching)
- RabbitMQ

##### Tools

- Maven
- Swagger / OpenAPI
- JUnit 5 + Mockito
- Flyway
- Lombok
---

## Microservices

### ✅ Core Services (Current Scope)

| Sr | Services             | Accountability                                                           |
|----|----------------------|--------------------------------------------------------------------------|
| 0  | Common | Roles, **JWT**, Security Filter ( Every microservice scans this package) |
| 1  | Auth Service         | Multi-tenant auth (Users Register, logout , login) , **Redis blacklist tokens** |
| 2  | Catalogue Service    | SKU master, product listings per seller                                  |
| 3  | Inbound Service      | Stock receiving, GRN creation, barcode scanning, **RabbitMQ Producer**   |
| 4  | Inventory Service    | Real-time stock, reservations, optimistic locking, Redis cache           |
| 5  | Order Service        | Bulk order lifecycle management, stock failure handling                  |
| 6  | Shipment Service     | Courier allocation, mock tracking                                        |

---

## Roles

| Role | Responsibility |
| --- | --- |
| `ADMIN` | Full platform control |
| `SELLER` | Bulk orders, products, inventory visibility |
| `WAREHOUSE_MANAGER` | Main warehouse operations |
| `INBOUND_OPERATOR` | Scan & receive incoming stock |
| `PICKER_PACKER` | Pick & pack orders |
| `INVENTORY_AUDITOR` | Read-only stock visibility |
| `CATALOGUE_MANAGER` | Manages SKU master data |

### RabbitMQ Events

| Event             | Producer | Consumer | Action                          |
|-------------------| --- | --- |---------------------------------|
| `STOCK_RECEIVED`  | Inbound | Inventory | Update stock                    |
| `ORDER_CREATED`   | Order | Inventory | Reserve stock                   |
| `STOCK_RESERVED`  | Inventory | Order | Confirm order                   |
| `STOCK_FAILED`    | Inventory | Order | Fail order - Insufficient stock |
| `ORDER_CONFIRMED` | Order | Shipment | Create shipment                 |
| `ORDER_DELIVERED` | Shipment | Order | Close order                     |

---
![RabbitMQ](/RabbitMQ.png)

### Dead Letter Queue (DLQ) Architecture
Every critical queue is backed by a DLX + DLQ to prevent message loss on processing failures.

```
                                Main Queue
         ___________________________|_______________________
        |                                                   |
(Transient Failures - 3x)                       Terminal Failures - 0x
        ↓                                                   ↓
Dead Letter Exchange (DLX)                  DLX (Same flow as of Transient)
        ↓
Dead Letter Queue (DLQ) ← message safely stored
        ↓
Developer investigates → replays message → no data loss
```
---
### Quorum Queues

All queues are implemented as **Quorum Queues** for high availability and data safety:

- Replicated across multiple RabbitMQ nodes
- No message loss on node failure
- Strong consistency guarantees
- Automatic leader election on failure

### Retry Policy
1. Attempt 1 → fails → wait 150ms
2. Attempt 2 → fails → wait 300ms
3. Attempt 3 → fails → wait 600ms
4. Attempt 4 → fails → bubbles up to RabbitMQ consumer → basicNack → DLQ
---
### Multi-Tenancy
Each seller accesses only their own products, inventory, and orders via `tenantId` embedded in JWT claims.

### Redis Caching
- Token blacklisting on logout — O(1) lookup vs O(n) DB query
- Inventory stock level cache — 15min TTL, **evicted on stock change**
---
## Security Architecture

```
Request → JwtAuthFilter (common)
        ↓
Token blacklisted? (Redis) → 401
        ↓
Token valid? (JWT signature) → 401
        ↓
Authenticated? (SecurityConfig) → 401
        ↓
Correct role? (@PreAuthorize) → 403
        ↓
Controller executes → 200
```

| Layer | What it checks |
|-------|---------------|
| `JwtAuthFilter` | Token validity + Redis blacklist |
| `SecurityConfig` | Authentication |
| `@PreAuthorize` | Role-based authorization |
---
### API Documentation
```
http://localhost:{port}/swagger-ui.html
```

| Service | Port | Swagger |
|---------|------|---------|
| Auth | 8081 | http://localhost:8081/swagger-ui.html |
| Catalogue | 8082 | http://localhost:8082/swagger-ui.html |
| Inbound | 8083 | http://localhost:8083/swagger-ui.html |
| Inventory | 8084 | http://localhost:8084/swagger-ui.html |
| Order | 8085 | http://localhost:8085/swagger-ui.html |
| Shipment | 8086 | http://localhost:8086/swagger-ui.html |

---
## Future Roadmap

- [ ] Vendor Service — supplier & truck management
- [ ] WMS Service — bin locations, picking, packing
- [ ] Invoice Service — auto GST invoice generation
- [ ] Returns Service — reverse logistics & refunds
- [ ] Notification Service — real-time alerts via WebSocket
- [ ] Analytics Service — seller dashboards & KPIs
- [ ] API Gateway — centralized routing & rate limiting
---

> Built to demonstrate backend engineering depth — inspired by real B2B fulfillment platforms like Easyecom and Unicommerce.