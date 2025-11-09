# TradeCraft — System Specification

## Purpose

This document defines the high-level system specification for TradeCraft, a production-grade paper trading platform supporting real-time market data, a low-latency matching engine, social features, and analytics. It describes the architecture, component responsibilities, event flows, storage choices, non-functional requirements, deployment considerations, and initial development tasks.

> Note: This spec follows the microservices architecture and the diagram provided by the project owner.

---

## 1. Objectives & Scope

Primary objective:
- Provide a reliable, observable, and extensible paper trading platform where users can place orders, see real-time market and portfolio updates, share trades socially, and analyze performance.

Scope (initial MVP):
- Core services: Authentication, Trading API, Matching Engine, Market Data ingestion, Portfolio, Wallet, WebSocket broadcaster, Notifications, Social and Analytics.
- Persistence: PostgreSQL (primary data) + TimescaleDB (time-series) + Redis (cache/session) + Kafka (event bus).
- Expose REST APIs for all CRUD workflows and WebSocket for real-time streaming.
- Focus on correctness, observability and resilient event-driven flows.

---

## 2. High-Level Architecture

The platform follows a microservices architecture with clear separation of concerns across layers:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         PAPER TRADING PLATFORM                           │
│                        (Microservices Architecture)                      │
└─────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│                           CLIENT LAYER                                    │
├──────────────────────────────────────────────────────────────────────────┤
│  Web App (React/Next.js)  │  Mobile (React Native - Future)              │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                         API GATEWAY LAYER                                 │
├──────────────────────────────────────────────────────────────────────────┤
│  NGINX / Kong / AWS API Gateway                                          │
│  ├─ Rate Limiting                                                        │
│  ├─ Authentication (JWT Validation)                                      │
│  ├─ Request Routing                                                      │
│  └─ Load Balancing                                                       │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                        MICROSERVICES LAYER                                │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  ┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐ │
│  │   Auth Service     │  │   Trading Service  │  │  Portfolio Service │ │
│  │                    │  │                    │  │                    │ │
│  │ ├─ User Management │  │ ├─ Order Validation│  │ ├─ Holdings Track │ │
│  │ ├─ JWT Tokens      │  │ ├─ Order Placement │  │ ├─ P&L Calculation│ │
│  │ ├─ Session Mgmt    │  │ ├─ Order Status    │  │ ├─ Performance    │ │
│  │ └─ Permissions     │  │ └─ User Orders API │  │ └─ Risk Metrics   │ │
│  └────────────────────┘  └────────────────────┘  └────────────────────┘ │
│                                                                            │
│  ┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐ │
│  │  Matching Engine   │  │   Market Data      │  │  Analytics Service │ │
│  │     Service        │  │     Service        │  │                    │ │
│  │                    │  │                    │  │ ├─ Trade Stats     │ │
│  │ ├─ Order Book Mgmt │  │ ├─ BBO Calculation │  │ ├─ User Rankings   │ │
│  │ ├─ Matching Logic  │  │ ├─ L2 Order Book   │  │ ├─ Market Insights │ │
│  │ ├─ Trade Execution │  │ ├─ Price Charts    │  │ └─ Reports         │ │
│  │ └─ Event Publisher │  │ └─ Historical Data │  │                    │ │
│  └────────────────────┘  └────────────────────┘  └────────────────────┘ │
│                                                                            │
│  ┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐ │
│  │   Social Service   │  │ Notification Svc   │  │  Wallet Service    │ │
│  │                    │  │                    │  │                    │ │
│  │ ├─ User Profiles   │  │ ├─ Email Alerts    │  │ ├─ Virtual Balance │ │
│  │ ├─ Follow/Friends  │  │ ├─ Push Notifs     │  │ ├─ Transactions   │ │
│  │ ├─ Leaderboards    │  │ ├─ Trade Alerts    │  │ ├─ Fund Mgmt      │ │
│  │ ├─ Trade Sharing   │  │ └─ WebSocket Push  │  │ └─ Balance History│ │
│  │ └─ Comments/Likes  │  │                    │  │                    │ │
│  └────────────────────┘  └────────────────────┘  └────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                         MESSAGE BROKER LAYER                              │
├──────────────────────────────────────────────────────────────────────────┤
│  Apache Kafka / RabbitMQ                                                 │
│  ├─ order.placed         (Trading → Matching Engine)                    │
│  ├─ order.matched        (Matching Engine → Portfolio)                  │
│  ├─ trade.executed       (Matching Engine → All Services)               │
│  ├─ portfolio.updated    (Portfolio → Analytics/Notification)           │
│  └─ market.data.update   (Market Data → WebSocket Server)               │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                         DATA LAYER                                        │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌──────────────────┐ │
│  │   PostgreSQL        │  │   Redis Cluster     │  │  TimescaleDB     │ │
│  │  (Primary Store)    │  │   (Cache/Session)   │  │  (Time-Series)   │ │
│  │                     │  │                     │  │                  │ │
│  │ ├─ Users            │  │ ├─ Active Orders   │  │ ├─ Trade History│ │
│  │ ├─ Orders           │  │ ├─ Order Books     │  │ ├─ Price Data   │ │
│  │ ├─ Trades           │  │ ├─ BBO Cache       │  │ ├─ Analytics    │ │
│  │ ├─ Portfolios       │  │ ├─ Sessions        │  │ └─ Metrics      │ │
│  │ ├─ Social Data      │  │ ├─ Leaderboards    │  │                  │ │
│  │ └─ User Profiles    │  │ └─ Pub/Sub         │  │                  │ │
│  └─────────────────────┘  └─────────────────────┘  └──────────────────┘ │
│                                                                            │
│  ┌─────────────────────┐  ┌─────────────────────┐                        │
│  │   Elasticsearch     │  │   S3 / Object Store │                        │
│  │  (Search/Logs)      │  │   (File Storage)    │                        │
│  │                     │  │                     │                        │
│  │ ├─ User Search      │  │ ├─ Profile Images   │                        │
│  │ ├─ Trade Search     │  │ ├─ Reports (PDF)    │                        │
│  │ ├─ Audit Logs       │  │ ├─ Backups          │                        │
│  │ └─ App Logs         │  │ └─ Documents        │                        │
│  └─────────────────────┘  └─────────────────────┘                        │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                    MONITORING & OBSERVABILITY                             │
├──────────────────────────────────────────────────────────────────────────┤
│  Prometheus + Grafana  │  ELK Stack  │  Sentry  │  Jaeger (Tracing)     │
└──────────────────────────────────────────────────────────────────────────┘
```

Key components:
- API Gateway: authentication, rate-limiting, TLS termination, routing.
- Microservices: Auth, Trading, Matching Engine, Market Data, Portfolio, Wallet, Social, Analytics, Notification, WebSocket/Realtime.
- Message Broker: Kafka for durable event streams and pub/sub between services.
- Data Stores: PostgreSQL, TimescaleDB, Redis, Elasticsearch, S3.
- Observability: Prometheus, Grafana, Jaeger, ELK, Sentry.

---

## 3. Component Responsibilities

Auth Service
- User registration, login, JWT issuance and revocation, password hashing, session management (Redis), role/permission checks.
- Exposes: /auth/* endpoints and a public health endpoint.

Trading Service
- Order entry API, validation, persistence of orders, balance locks via Wallet Service, producing `order.placed` events.
- Exposes: POST /orders, GET /orders, GET /orders/{id}, DELETE /orders/{id}.

Matching Engine
- In-memory order books per symbol, price-time priority matching, trade execution creation, audit logging, and reliable publication of `trade.executed` events.
- Single responsibility: deterministic matching logic; persist audit logs to DB.

Market Data
- Connects to external exchanges (Binance), normalizes feeds, writes raw ticks to TimescaleDB and caches last prices in Redis; publishes `market.data.update` events.

Portfolio Service
- Consumes `trade.executed`, applies FIFO cost basis or configured cost method, updates positions, computes realized/unrealized P&L, writes portfolio snapshots.

Wallet Service
- Virtual balance accounting (double-entry), lock/unlock funds for orders, transaction history and audit; prevents negative balances and supports concurrency-safe operations.

WebSocket / Real-time Broadcaster
- Subscribes to Redis/Kafka channels and forwards messages to authenticated clients; supports subscription channels (orderbook, trades, ticker, user channels).

Notification Service (with Worker)
- Routes events to delivery channels (in-app, email via SendGrid, push). Uses Celery-like worker for async tasks and retries.

Analytics & Leaderboard
- Batch + incremental calculations: Sharpe, Sortino, Drawdown, leaderboards in Redis sorted sets, exposes metrics endpoints.

Social Service
- User profiles, follow/followers, shared trades, likes and comments, feed generation (mix of chronological and engagement-ranked). Stores metadata in PostgreSQL and uses Redis for counters.

---

## 4. Event Topics (Kafka)

Core topics:
- order.placed — carries order metadata (trading → matching engine)
- order.cancelled — cancel requests
- order.matched / order.updated — updates on order state
- trade.executed — single source of truth for trade executions (consumed by Portfolio, Wallet, Analytics, Notification, Social)
- portfolio.updated — snapshot or delta after trades
- market.data.update — normalized price ticks and BBO updates
- user.notification — notifications to send

Message contract guidelines:
- Use JSON with explicit schema versioning and a small header {"schema_version": "v1", "event_type": "trade.executed"}.
- Include trace identifiers for distributed tracing (trace_id, span_id).
- Keep messages idempotent with unique event_id and sequence numbers where necessary.

---

## 5. Data Model (high-level)

Primary relational tables (Postgres):
- users (id, email, username, hashed_password, created_at, ...)
- orders (id, user_id, symbol, side, type, price, quantity, filled_qty, status, created_at, updated_at)
- trades (id, buy_order_id, sell_order_id, symbol, price, qty, taker_order_id, maker_order_id, executed_at)
- portfolios (id, user_id, snapshot_time, total_value, cash_balance)
- positions (id, user_id, symbol, quantity, avg_price, realized_pnl)
- wallet_transactions (id, user_id, amount, currency, type, ref_id, created_at)
- shared_trades, likes, comments, achievements

Time-series (TimescaleDB):
- market_trades (time, symbol, price, qty, side, source)
- ohlcv_<interval> (continuous aggregates)
- orderbook_snapshots (time, symbol, bids, asks)

Redis usage:
- sessions and JWT revocations
- latest prices and BBO (fast lookup)
- leaderboards (sorted sets)
- pub/sub for low-latency broadcasting

Elasticsearch:
- trade search, user search, feed text search, audit logs indexing

S3 / Object store:
- profile images, export reports, backups

---

## 6. APIs (high-level endpoints)

Auth:
- POST /auth/register
- POST /auth/login
- POST /auth/refresh
- POST /auth/logout

Orders:
- POST /orders
- GET /orders?user_id=...
- GET /orders/{id}
- DELETE /orders/{id}

Market Data:
- GET /market/{symbol}/ticker
- GET /market/{symbol}/orderbook?depth=10
- GET /market/{symbol}/trades
- GET /market/{symbol}/candles?interval=1m

Portfolio:
- GET /portfolio
- GET /portfolio/positions
- GET /portfolio/performance

Social:
- POST /trades/{trade_id}/share
- GET /feed
- POST /shares/{id}/like
- POST /shares/{id}/comment

WebSocket:
- /ws?token=... (subscribe channels: orderbook:{symbol}, trades:{symbol}, user:orders)

Analytics:
- GET /analytics/metrics
- GET /leaderboard

Security: all user-sensitive endpoints protected behind JWT and role checks.

---

## 7. Non-functional Requirements

Performance
- Trading API P95 < 100ms for simple CRUD operations.
- Matching engine: process target throughput 500+ orders/sec for MVP; microsecond-level BBO lookups in memory.

Scalability
- Stateless services behind auto-scaling; stateful components (matching engine) scaled by symbol partitioning or sharding.
- Use Kafka for fan-out and decoupling; Redis cluster for hot data.

Reliability & Durability
- Kafka guarantees (acks=all) for critical events; write-ahead logging in DB for recovery.
- Backups: daily DB backups to S3; incremental backups for TimescaleDB.

Consistency
- Eventual consistency across services; critical balances and order state reconciled via trade.executed authoritative events.

Security
- TLS everywhere, JWT for auth, rate-limits at API gateway, strong password hashing (bcrypt/argon2), secrets in AWS Secrets Manager or parameter store.
- Audit logs for order and wallet operations stored immutably.

Observability
- Metrics exported via Prometheus, dashboards in Grafana; distributed tracing via Jaeger; logs to ELK and errors to Sentry.

Compliance & Privacy
- Store only required PII; provide data-retention policy and deletion endpoints.

---

## 8. Deployment & Infrastructure (high-level)

Target: AWS (managed services recommended):
- EKS / ECS for containerized microservices
- RDS (Postgres) with extension TimescaleDB (self-managed or RDS-compatible) or hosted Timescale
- MSK (Managed Kafka) or self-hosted Kafka on EC2
- ElastiCache (Redis) cluster
- S3 for object storage
- ALB/GWLB and Route 53 for ingress; API Gateway optional
- IAM roles for service-to-service access; Secrets Manager for secrets

CI/CD
- GitHub Actions / GitLab CI pipeline: build, test, image publish to ECR, progressive rollout to staging then production.

---

## 9. Testing & QA Strategy

- Unit tests for service logic (80% target for critical services like matching engine, wallet).
- Integration tests using ephemeral docker-compose test environment (Postgres, Redis, Kafka) for end-to-end flows.
- Contract tests for event schemas (Kafka topics).
- Load tests with Locust to validate throughput and latency targets.
- Chaos testing for resilience (network partitions, broker failures).

---

## 10. Security & Operational Notes

- Apply RBAC and least privilege per service account in AWS.
- Rotate secrets regularly and maintain strong logging for suspicious access.
- Rate-limit authentication endpoints and apply CAPTCHAs for suspicious sign-ups.
- Implement automated alerts for metric thresholds (e.g., lag, error rate, high latency).

---

## 11. Data Retention & GDPR Considerations

- Default retention for raw market ticks: 90 days hot + compressed cold storage.
- Trades and user transactions: keep for at least 7 years for auditability (configurable), with export/erase per privacy requirements.
- Provide data export & deletion endpoints for users.

---

## 12. Initial Developer Onboarding / Minimal Implementation Steps

1. Initialize repository and create `docker-compose.yml` for local dev with: Postgres (with Timescale), Redis, Kafka + Zookeeper, and a simple Nginx gateway.
2. Create `.env.example` listing DB, Redis, Kafka connection strings and secrets placeholders.
3. Scaffold minimal FastAPI health-check service and a simple producer/consumer for `trade.executed` (end-to-end smoke test).
4. Add pre-commit hooks (black, flake8, mypy) and CI pipeline skeleton.
5. Add observability: local Prometheus and Grafana dashboards for dev.

---

## 13. Roadmap & Next Milestones (first 4 weeks)

- Week 1: Dev environment, Docker Compose, initial services scaffolding (Auth, Trading API, Matching Engine skeleton).
- Week 2: Database schema and migrations; implement order book data structures.
- Week 3: Authentication flows and session management; basic Wallet service.
- Week 4: Matching engine integration, produce `trade.executed` and basic Portfolio consumer.

---

## 14. Open Decisions / Risks

- Matching Engine scaling model: single-process per symbol vs multi-tenant multi-process — needs benchmark-driven decision.
- Choice of managed vs self-hosted Kafka/Timescale for production cost/performance trade-offs.
- Trade execution guarantees: eventual vs strong consistency between Wallet and Portfolio — require careful transactional design.

---

## 15. Appendices

- Event schema examples and API contracts should be added as separate documents in `docs/contracts/`.
- Network diagram and AWS architecture (detailed) to follow when moving to infra planning.

---

End of system specification (MVP). Additions: if you want, I can now:
- Generate `docs/contracts/` with example Kafka event JSON schemas and minimal API OpenAPI stubs, or
- Add the recommended `docker-compose.yml` for the exact dev stack.

Which should I do next?