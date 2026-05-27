# FulfillX - Warehouse Fulfillment Platform
FulfillX is a B2B warehouse fulfillment platform where vendors supply stock, warehouses store and manage it, and sellers place bulk orders which are picked, packed, shipped, and tracked, all across multiple tenants on a single distributed system.

---
### Tech Stack
##### Backend
* Java 21
* Spring Boot, Spring Security, Spring Data JPA
* Hibernate

##### Architecture
- Microservices
- REST APIs
- Event Driven Communication (RabbitMQ)

##### Infrastructure

- Docker Compose
- PostgreSQL
- Redis
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

| Sr | Services             | Accountability                                               |
|----|----------------------|--------------------------------------------------------------|
| 0  | Common | Roles, **JWT**, Security Filter ( Every microservice scans this package) |
| 1  | Auth Service         | Multi-tenant auth (Users Register, logout , login) , **Redis blacklist tokens** |
| 2  | Catalogue Service    | SKU master, product listings per seller                      |
| 3  | Inbound Service      | Stock receiving, GRN creation, barcode scanning, **RabbitMQ Producer** |
| 4  | Inventory Service    | Real-time stock, reservations                  |
| 5  | Order Service        | Bulk order lifecycle management                              |
| 6  | Shipment Service     | Courier allocation                             |

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

| Event | Producer | Consumer | Action |
| --- | --- | --- | --- |
| `STOCK_RECEIVED` | Inbound | Inventory | Update stock ✅ Done |
| `ORDER_CREATED` | Order | Inventory | Reserve stock |
| `STOCK_RESERVED` | Inventory | Order | Confirm order |
| `ORDER_CONFIRMED` | Order | Shipment | Create shipment |
| `ORDER_DELIVERED` | Shipment | Order | Close order |