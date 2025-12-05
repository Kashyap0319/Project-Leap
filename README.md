# Project Leap - Log Management System

A comprehensive log management system built with Spring Boot + Kotlin backend and Next.js frontend. The system handles high-volume concurrent writes, implements JWT authentication, rate limiting, and intelligent alerting.

## 🏗️ Architecture Overview

### Tech Stack

**Backend:**
- Spring Boot 3.2.0 with Kotlin
- Dual MongoDB databases (Logs + Metadata)
- JWT Authentication
- Bucket4j Rate Limiting
- Optimistic Locking
- Kotlin Coroutines for concurrent handling

**Frontend:**
- Next.js 15 with TypeScript
- Tailwind CSS
- Axios for API communication
- Client-side routing

## 📊 Dual MongoDB Architecture

The application uses **two separate MongoDB databases** for optimal performance and separation of concerns:

### 1. Logs Database (`project-leap-logs`)
**Purpose:** High-volume write operations for log entries

**Collections:**
- `logs` - Main log entries with optimistic locking (@Version)

**Why Separate:**
- High write throughput without impacting metadata operations
- Independent scaling capabilities
- Better query performance isolation
- Simpler backup/retention policies for time-series data

**Configuration:**
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/project-leap-logs
```

### 2. Metadata Database (`project-leap-metadata`)
**Purpose:** System metadata, users, alerts, and configuration

**Collections:**
- `users` - User accounts with encrypted passwords
- `alerts` - System alerts with resolution tracking
- `metadata` - System configuration and metadata
- `rate_limit_stats` - Rate limiting statistics

**Why Separate:**
- Lower volume, higher consistency requirements
- Different backup and retention needs
- Clearer security boundary for sensitive data
- Independent optimization strategies

**Configuration:**
```properties
mongodb.metadata.uri=mongodb://localhost:27017/project-leap-metadata
```

## 🚦 Rate Limiting Implementation

### Strategy: Token Bucket Algorithm (Bucket4j)

The rate limiter uses a **token bucket algorithm** for smooth rate limiting:

**Configuration:**
```properties
ratelimit.capacity=100          # Max tokens in bucket
ratelimit.refill-tokens=10      # Tokens added per interval
ratelimit.refill-duration-seconds=1  # Refill interval
```

**How It Works:**
1. Each user + endpoint combination gets a unique bucket
2. Each request consumes 1 token from the bucket
3. Bucket refills at configured rate (10 tokens/second)
4. When bucket is empty, requests are rejected with HTTP 429

**Implementation Details:**
- Per-user, per-endpoint buckets using ConcurrentHashMap
- Key format: `{userId}:{endpoint}`
- Graceful degradation with clear error messages
- Stats tracking in metadata database

**Benefits:**
- Prevents API abuse
- Protects MongoDB from overload
- Fair resource allocation per user
- Smooth traffic handling (no sudden bursts)

## ⚡ Concurrent Write Handling

The system handles **50+ concurrent writes** using:

### 1. Semaphore-based Concurrency Control
```kotlin
private val writeSemaphore = Semaphore(50)
```
- Limits concurrent database operations
- Prevents MongoDB connection pool exhaustion
- Provides backpressure to clients

### 2. Kotlin Coroutines
```kotlin
suspend fun createLogsBulk(logEntries: List<LogEntry>): List<LogEntry>
```
- Efficient async/await patterns
- Non-blocking I/O operations
- Better resource utilization

### 3. Optimistic Locking
```kotlin
@Version
val version: Long? = null
```
- Prevents lost updates in concurrent scenarios
- Automatic retry logic for version conflicts
- Clear error messages for conflict resolution

## 🔐 Security Features

### JWT Authentication
- Token-based stateless authentication
- Secure HMAC-SHA256 signing
- Configurable expiration (default: 24 hours)
- Authorization header: `Bearer {token}`

### Password Security
- BCrypt password hashing
- Strong password requirements
- Secure user registration flow

### CORS Configuration
- Configurable allowed origins
- Credentials support for JWT cookies
- Secure headers enforcement

## 📈 Alert System

### Automated Alert Generation
The system monitors error rates and creates alerts when thresholds are exceeded:

**Configuration:**
```properties
alert.threshold.error-rate=10        # Errors before alert
alert.threshold.time-window-minutes=5  # Time window
```

**Alert Types:**
- `ERROR_RATE` - High error/fatal log count
- `RATE_LIMIT` - Rate limit violations
- `SYSTEM` - System-level issues
- `CUSTOM` - User-defined alerts

**Severity Levels:**
- CRITICAL (>3x threshold)
- HIGH (>2x threshold)
- MEDIUM (>1x threshold)
- LOW

### Alert Resolution Workflow
1. Alert created automatically or manually
2. User acknowledges and investigates
3. User resolves with resolution notes
4. Or dismisses if false positive
5. Full audit trail maintained

## 🚀 Setup Instructions

### Prerequisites
- Java 17+
- Node.js 18+
- MongoDB 4.4+
- Gradle 8.5 (or use wrapper)

### Backend Setup

1. **Start MongoDB:**
```bash
# Using Docker
docker run -d -p 27017:27017 --name mongodb mongo:latest

# Or install MongoDB locally
```

2. **Configure Application:**
Edit `backend/src/main/resources/application.properties`:
```properties
# Update MongoDB URIs if needed
spring.data.mongodb.uri=mongodb://localhost:27017/project-leap-logs
mongodb.metadata.uri=mongodb://localhost:27017/project-leap-metadata

# Update JWT secret (production)
jwt.secret=your-super-secret-key-change-in-production-minimum-256-bits
```

3. **Build and Run:**
```bash
cd backend
./gradlew build
./gradlew bootRun
```

Backend will start on `http://localhost:8080`

### Frontend Setup

1. **Install Dependencies:**
```bash
cd frontend
npm install
```

2. **Configure API URL:**
Create `.env.local`:
```bash
NEXT_PUBLIC_API_URL=http://localhost:8080
```

3. **Run Development Server:**
```bash
npm run dev
```

Frontend will start on `http://localhost:3000`

## 📡 API Documentation

### Authentication

**POST** `/api/auth/register`
```json
{
  "username": "user",
  "password": "password",
  "email": "user@example.com"
}
```

**POST** `/api/auth/login`
```json
{
  "username": "user",
  "password": "password"
}
```

Response:
```json
{
  "token": "jwt-token",
  "username": "user",
  "email": "user@example.com"
}
```

### Logs

**GET** `/api/logs?level=ERROR&source=app&startTime=...&endTime=...`
- Query parameters: level, source, startTime, endTime (all optional)

**POST** `/api/logs`
```json
{
  "level": "ERROR",
  "message": "Error occurred",
  "source": "application",
  "metadata": {"key": "value"},
  "tags": ["production"]
}
```

**POST** `/api/logs/bulk`
```json
{
  "logs": [
    {
      "level": "INFO",
      "message": "Log message",
      "source": "app"
    }
  ]
}
```

### Alerts

**GET** `/api/alerts?status=OPEN`

**POST** `/api/alerts/{id}/resolve`
```json
{
  "resolution": "Fixed by restarting service"
}
```

**POST** `/api/alerts/{id}/dismiss`

## 🧪 Testing

### Backend Tests
```bash
cd backend
./gradlew test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## 📦 Production Deployment

### Backend

1. **Build JAR:**
```bash
./gradlew bootJar
```

2. **Run with production profile:**
```bash
java -jar build/libs/project-leap-backend-1.0.0.jar \
  --spring.profiles.active=production \
  --jwt.secret=$JWT_SECRET \
  --spring.data.mongodb.uri=$MONGODB_LOGS_URI \
  --mongodb.metadata.uri=$MONGODB_METADATA_URI
```

### Frontend

1. **Build:**
```bash
npm run build
```

2. **Start:**
```bash
npm start
```

Or deploy to Vercel/Netlify.

## 🔧 Configuration Reference

### Backend (application.properties)

| Property | Default | Description |
|----------|---------|-------------|
| `spring.data.mongodb.uri` | `mongodb://localhost:27017/project-leap-logs` | Logs database URI |
| `mongodb.metadata.uri` | `mongodb://localhost:27017/project-leap-metadata` | Metadata database URI |
| `jwt.secret` | - | JWT signing secret (256+ bits) |
| `jwt.expiration` | `86400000` | Token expiration (ms) |
| `ratelimit.capacity` | `100` | Rate limit bucket capacity |
| `ratelimit.refill-tokens` | `10` | Tokens per refill |
| `ratelimit.refill-duration-seconds` | `1` | Refill interval |
| `alert.threshold.error-rate` | `10` | Error count before alert |
| `alert.threshold.time-window-minutes` | `5` | Alert time window |
| `app.max-concurrent-writes` | `50` | Max concurrent writes |

## 🎯 Key Features

- ✅ Dual MongoDB databases for optimal performance
- ✅ JWT-based authentication
- ✅ Token bucket rate limiting
- ✅ Handles 50+ concurrent writes
- ✅ Optimistic locking for data consistency
- ✅ Automated error-rate alerting
- ✅ Interactive dashboard with filters
- ✅ Alert resolution workflow
- ✅ Modular architecture
- ✅ Production-ready security

## 📝 License

MIT

## 🤝 Contributing

Contributions welcome! Please read CONTRIBUTING.md for guidelines.