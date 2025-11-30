# TradeCraft – Development Log

A running log of what I implemented on which day, for my own tracking and future case-study.

> Format:
> - Date (YYYY-MM-DD)
> - Service / area
> - Changes
> - Notes / learnings

---

## 2025-11-30

### Area: Infrastructure & Configuration (trading-service)

- [ ] Added `docker-compose.yml` with:
  - Postgres 15 (DB: `tradecraft`, user: `tradecraft`)
  - Redis 7 for caching/sessions
- [ ] Verified containers start with `docker compose up -d`
- [ ] Confirmed Postgres is reachable from host machine

**Notes / Learnings:**
- Understood how Docker Compose helps mirror production infra locally.
- Practiced connecting a Spring Boot app to services running in containers.

---

### Area: Application Configuration (trading-service)

- [ ] Configured `application.yml`:
  - Datasource (Postgres `tradecraft` DB)
  - JPA settings (`ddl-auto=validate`, show SQL)
  - Redis host/port
  - Logging levels for root and `com.tradecraft` package
- [ ] Ran the app and confirmed it starts without connection errors.

**Notes / Learnings:**
- Learned why we use `ddl-auto=validate` when we rely on Flyway migrations.
- Saw how logging levels can be tuned per package.

---

## 2025-12-01

### Area: Database Schema & Migrations (trading-service)

- [x] Added Flyway migration `V1__init_trading.sql`:
  - `users` table with email, username, hashed_password
  - ENUMs: `order_side`, `order_type`, `order_status`
  - `orders` table linked to `users` with ON DELETE CASCADE
  - Indexes for common query patterns (user_id, symbol, status, created_at)
- [x] Started the app and verified Flyway applied migrations.
- [x] Confirmed `users`, `orders`, and `flyway_schema_history` tables exist in Postgres.

**Notes / Learnings:**
- Understood how Flyway keeps schema changes versioned and repeatable.
- Saw how ENUM types are defined and used in Postgres.
- Learned importance of indexes for performance on frequently queried columns.
- Used NUMERIC(18,8) for precise decimal calculations (never float for money!).

---

### Area: Domain Model & Repository (trading-service)

- [x] Created Java enums: `OrderSide`, `OrderType`, `OrderStatus`
  - Match Postgres ENUMs exactly
- [x] Created `Order` JPA entity in `com.tradecraft.trading.domain`
  - Mapped all fields to database columns with appropriate annotations
  - Used `@Enumerated(EnumType.STRING)` for enum mapping
  - Used UUID, BigDecimal, OffsetDateTime for proper type safety
  - Added `@PrePersist` and `@PreUpdate` lifecycle callbacks for timestamps
- [x] Created `OrderRepository` extending `JpaRepository<Order, UUID>`
  - Added method `findByUserIdOrderByCreatedAtDesc(UUID userId)`
  - Added method `findBySymbol(String symbol)`
  - Added method `findByUserIdAndStatus(UUID userId, OrderStatus status)`

**Notes / Learnings:**
- Learned how JPA annotations map Java classes to DB tables.
- Saw how Spring Data builds queries from method names (derived queries).
- Understood @PrePersist/@PreUpdate for automatic timestamp management.
- Used Lombok annotations (@Data, @Builder) to reduce boilerplate code.

---

---

## 2025-11-30 (Continued)

### Area: Setup & Configuration Fixes (trading-service)

- [x] Fixed Docker authentication issues (web-based login)
- [x] Resolved PostgreSQL timezone configuration conflicts
  - Removed MySQL-specific parameters from JDBC URL
  - Configured Hibernate timezone to UTC
  - Set `allow_jdbc_metadata_access: false` to bypass metadata queries
- [x] Changed application port from 8080 to 8081 to avoid conflicts
- [x] Added component scanning for `com.tradecraft` package
- [x] Created convenience scripts:
  - `run-app.bat` for easy startup
  - `QUICK_START.md` with verification steps
  - `SETUP_COMPLETE.md` with comprehensive guide

**Notes / Learnings:**
- Docker registry authentication required web-based flow on Windows
- PostgreSQL JDBC driver doesn't use `serverTimezone` parameter (that's MySQL)
- Hibernate 7.x requires explicit dialect when `allow_jdbc_metadata_access=false`
- JVM timezone settings can conflict with database connection initialization
- Background process management in PowerShell requires careful cleanup
- Setting `@SpringBootApplication(scanBasePackages={...})` ensures all packages are scanned

**Troubleshooting Done:**
- Timezone mismatch between JVM ("Asia/Calcutta") and PostgreSQL
- Port conflicts from multiple startup attempts
- PowerShell background job limitations with Maven commands

---

## Future Entries

Use this structure for future work:

### YYYY-MM-DD

### Area: <Service or Feature Name>

- [ ] Changes made
- [ ] More changes...

**Notes / Learnings:**
- Key takeaways...
- Gotchas, issues, decisions...

