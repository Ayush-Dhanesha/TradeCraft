# TradeCraft - Paper Trading Platform

A microservices-based paper trading platform built with Spring Boot 3, Java 17, PostgreSQL, Redis, and Kafka.

## 🎯 Project Status

✅ **Step 1: Local Infrastructure + Spring Configuration** - COMPLETE  
✅ **Step 2: Core Domain + Flyway Migration** - COMPLETE  
⏳ Step 3: Service Layer (Coming Next)  
⏳ Step 4: REST Controllers  
⏳ Step 5: Kafka Integration  
⏳ Step 6: Testing

## 🚀 Quick Start

### Prerequisites
- Java 17+ (Currently using Java 20)
- Maven 3.6+
- Docker Desktop (for Postgres + Redis)

### Start Infrastructure
```bash
docker compose up -d
```

### Run Application
```bash
# Option 1: Use convenience script
./run-app.bat

# Option 2: Use Maven directly
mvn spring-boot:run

# Option 3: Build and run JAR
mvn clean package
java -jar target/Trade_Craft-0.0.1-SNAPSHOT.jar
```

Application will start on **http://localhost:8081**

## 📚 Documentation

- **[QUICK_START.md](QUICK_START.md)** - Get up and running in 2 minutes
- **[docs/SETUP_COMPLETE.md](docs/SETUP_COMPLETE.md)** - Comprehensive setup guide
- **[docs/SYSTEM_SPECIFICATION.md](docs/SYSTEM_SPECIFICATION.md)** - System architecture
- **[docs/logs.md](docs/logs.md)** - Development log

## 🏗️ Architecture

### Technology Stack
- **Backend:** Spring Boot 3.0, Java 17
- **Database:** PostgreSQL 15
- **Cache:** Redis 7
- **Build:** Maven
- **Migrations:** Flyway
- **ORM:** Hibernate (JPA)

### Project Structure
```
Trade_Craft/
├── docker-compose.yml          # Infrastructure (Postgres + Redis)
├── pom.xml                     # Maven dependencies
├── run-app.bat                 # Convenience startup script
├── docs/                       # Documentation
│   ├── logs.md                 # Development log
│   ├── SETUP_COMPLETE.md       # Setup guide
│   └── SYSTEM_SPECIFICATION.md # Architecture docs
└── src/main/
    ├── java/
    │   └── com/
    │       ├── example/Trade_Craft/
    │       │   └── TradeCraftApplication.java
    │       └── tradecraft/trading/domain/
    │           ├── Order.java           # JPA Entity
    │           ├── OrderSide.java       # Enum: BUY, SELL
    │           ├── OrderType.java       # Enum: MARKET, LIMIT
    │           ├── OrderStatus.java     # Enum: NEW, FILLED, etc.
    │           └── OrderRepository.java # Spring Data JPA
    └── resources/
        ├── application.yml              # Configuration
        └── db/migration/
            └── V1__init_trading.sql     # Initial schema
```

## 🗄️ Database Schema

### Tables Created
- **users** - User accounts and authentication
- **orders** - Trading orders with lifecycle tracking
- **flyway_schema_history** - Migration tracking

### Key Features
- UUID primary keys (distributed-system friendly)
- PostgreSQL ENUMs for type safety
- Indexed columns for performance
- Timestamp tracking (created_at, updated_at)
- BigDecimal for precise monetary calculations

## 🔧 Configuration

### Application Settings
```yaml
Server Port: 8081
Database: jdbc:postgresql://localhost:5432/tradecraft
Redis: localhost:6379
Flyway: Enabled
JPA DDL Auto: validate (Flyway manages schema)
```

### Docker Services
```yaml
Postgres:
  - Image: postgres:15
  - Port: 5432
  - Database: tradecraft
  - User: tradecraft
  - Password: tradecraft

Redis:
  - Image: redis:7
  - Port: 6379
```

## 🧪 Verification

### Check Docker Containers
```bash
docker ps
# Should show tradecraft-postgres and tradecraft-redis (healthy)
```

### Verify Database Tables
```bash
docker exec -it tradecraft-postgres psql -U tradecraft -d tradecraft
\dt
# Should show: users, orders, flyway_schema_history
\q
```

### Check Application Logs
Look for these indicators of successful startup:
```
✅ Flyway Community Edition 11.14.1
✅ Successfully applied 1 migration to schema "public"
✅ Started TradeCraftApplication in X.XXX seconds
✅ Tomcat started on port(s): 8081 (http)
```

## 🛠️ Development

### Building
```bash
mvn clean compile
```

### Running Tests
```bash
mvn test
```

### Packaging
```bash
mvn clean package
```

## 📝 API (Coming in Step 4)

REST endpoints will be available at:
- `POST /api/orders` - Place new order
- `GET /api/orders` - List user orders
- `GET /api/orders/{id}` - Get order details
- `PUT /api/orders/{id}/cancel` - Cancel order

## 🔐 Security (Coming in Step 3)

- JWT-based authentication
- Password hashing (BCrypt)
- Role-based access control

## 📊 Monitoring (Future)

- Spring Boot Actuator endpoints
- Prometheus metrics
- Health checks

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Kill existing Java processes
taskkill /F /IM java.exe  # Windows
# Or change port in application.yml
```

### Database Connection Issues
```bash
# Restart Docker containers
docker compose down
docker compose up -d
```

### Flyway Migration Errors
```bash
# Reset database (CAUTION: Deletes all data)
docker compose down -v
docker compose up -d
```

## 📜 License

This project is for educational purposes.

## 👥 Contributing

This is a learning project. Steps 1-2 are complete, next steps coming soon!

---

**Status:** ✅ **Steps 1 & 2 Complete - Ready for Development!**

Run `./run-app.bat` to start the application and begin building features.

